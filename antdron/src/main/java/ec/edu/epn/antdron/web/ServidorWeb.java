package ec.edu.epn.antdron.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import ec.edu.epn.antdron.alimento.Alimento;
import ec.edu.epn.antdron.alimento.FabricaAlimentos;
import ec.edu.epn.antdron.hormiga.Hormiga;
import ec.edu.epn.antdron.persistencia.HormigaRegistro;
import ec.edu.epn.antdron.persistencia.HormigueroServicio;
import ec.edu.epn.xp.diseno.TarjetaCRC;

/** Pantallas web del hormiguero virtual para los usuarios de la KGD (servidor HTTP del JDK). */
@TarjetaCRC(responsabilidades = {"Mostrar login, registro y hormiguero", "Manejar la sesión del usuario KGD"},
        colaboradores = {"HormigueroServicio", "Html"})
public final class ServidorWeb {

    private static final String COOKIE = "SESION";
    private static final int MAX_CUERPO = 16 * 1024;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private record Sesion(int usuarioId, String[] aviso) {
    }

    private final HormigueroServicio servicio;
    private final String equipo;
    private final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();
    private HttpServer servidor;
    private ExecutorService hilos;

    public ServidorWeb(HormigueroServicio servicio, String equipo) {
        this.servicio = servicio;
        this.equipo = equipo;
    }

    public void iniciar(int puerto) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress(puerto), 0);
        servidor.createContext("/", this::atender);
        hilos = Executors.newFixedThreadPool(8);
        servidor.setExecutor(hilos);
        servidor.start();
    }

    public int puerto() {
        return servidor.getAddress().getPort();
    }

    public void detener() {
        if (servidor != null) {
            servidor.stop(0);
            hilos.shutdownNow();
        }
    }

    // ------------------------------------------------------------- Rutas

    private void atender(HttpExchange ex) {
        try {
            String ruta = ex.getRequestURI().getPath();
            String metodo = ex.getRequestMethod();
            Optional<String> token = token(ex);
            Sesion sesion = token.map(sesiones::get).orElse(null);
            switch (metodo + " " + ruta) {
                case "GET /" -> redirigir(ex, sesion == null ? "/login" : "/hormiguero");
                case "GET /salud" -> enviar(ex, 200, "text/plain", "OK");
                case "GET /login" -> enviarHtml(ex, 200, Html.login(equipo, null));
                case "POST /login" -> login(ex);
                case "GET /registro" -> enviarHtml(ex, 200, Html.registro(equipo, null));
                case "POST /registro" -> registro(ex);
                case "POST /logout" -> {
                    token.ifPresent(sesiones::remove);
                    ex.getResponseHeaders().add("Set-Cookie", COOKIE + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Strict");
                    redirigir(ex, "/login");
                }
                case "GET /hormiguero" -> {
                    if (sesion == null) {
                        redirigir(ex, "/login");
                    } else {
                        hormiguero(ex, sesion);
                    }
                }
                case "POST /hormigas" -> conSesion(ex, sesion, () -> {
                    Map<String, String> f = formulario(ex);
                    int id = servicio.crearHormiga(sesion.usuarioId(), f.getOrDefault("sexo", ""));
                    return "Nació la HLarva #" + id + ". Aliméntala con néctar para que viva o con "
                            + servicio.getTipoDelGrupo().alimento() + " para que se transforme.";
                });
                case "POST /hormigas/alimentar" -> conSesion(ex, sesion, () -> {
                    Map<String, String> f = formulario(ex);
                    return servicio.alimentar(sesion.usuarioId(), entero(f.get("id")), f.getOrDefault("alimento", ""));
                });
                default -> enviarHtml(ex, 404, Html.pagina("No encontrado", "<p>La página no existe.</p>"));
            }
        } catch (Exception e) {
            try {
                enviarHtml(ex, 500, Html.pagina("Error", "<p>Ocurrió un error interno. Intente de nuevo.</p>"));
            } catch (IOException ignorada) {
                // La conexión ya se cerró; no hay a quién responder.
            }
        } finally {
            ex.close();
        }
    }

    @FunctionalInterface
    private interface Accion {
        String ejecutar() throws IOException, SQLException;
    }

    /** Ejecuta la acción y vuelve al hormiguero mostrando el resultado o el error de validación. */
    private void conSesion(HttpExchange ex, Sesion sesion, Accion accion) throws IOException, SQLException {
        if (sesion == null) {
            redirigir(ex, "/login");
            return;
        }
        try {
            sesion.aviso()[0] = accion.ejecutar();
        } catch (IllegalArgumentException e) {
            sesion.aviso()[0] = "Error: " + e.getMessage();
        }
        redirigir(ex, "/hormiguero");
    }

    private void login(HttpExchange ex) throws IOException, SQLException {
        Map<String, String> f = formulario(ex);
        Optional<Integer> id = servicio.autenticar(f.get("usuario"), f.get("clave"));
        if (id.isEmpty()) {
            enviarHtml(ex, 401, Html.login(equipo, "Usuario o contraseña incorrectos."));
            return;
        }
        abrirSesion(ex, id.get());
    }

    private void registro(HttpExchange ex) throws IOException, SQLException {
        Map<String, String> f = formulario(ex);
        try {
            abrirSesion(ex, servicio.registrar(f.get("usuario"), f.get("clave")));
        } catch (IllegalArgumentException e) {
            enviarHtml(ex, 400, Html.registro(equipo, e.getMessage()));
        }
    }

    private void abrirSesion(HttpExchange ex, int usuarioId) throws IOException {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sesiones.put(token, new Sesion(usuarioId, new String[1]));
        ex.getResponseHeaders().add("Set-Cookie", COOKIE + "=" + token + "; Path=/; HttpOnly; SameSite=Strict");
        redirigir(ex, "/hormiguero");
    }

    private void hormiguero(HttpExchange ex, Sesion sesion) throws IOException, SQLException {
        String aviso = sesion.aviso()[0];
        sesion.aviso()[0] = null;
        List<HormigaRegistro> hormigas = servicio.listar(sesion.usuarioId());
        List<String> alimentos = FabricaAlimentos.todos().stream().map(Alimento::getNombre).toList();
        enviarHtml(ex, 200, Html.hormiguero(equipo, servicio.nombreUsuario(sesion.usuarioId()),
                servicio.getTipoDelGrupo(), hormigas, alimentos, aviso, Hormiga.MUERTA));
    }

    // ---------------------------------------------------------- Utilidades

    private static Optional<String> token(HttpExchange ex) {
        List<String> cookies = ex.getRequestHeaders().get("Cookie");
        if (cookies == null) {
            return Optional.empty();
        }
        for (String linea : cookies) {
            for (String par : linea.split(";")) {
                String[] kv = par.trim().split("=", 2);
                if (kv.length == 2 && kv[0].equals(COOKIE) && !kv[1].isEmpty()) {
                    return Optional.of(kv[1]);
                }
            }
        }
        return Optional.empty();
    }

    private static Map<String, String> formulario(HttpExchange ex) throws IOException {
        ByteArrayOutputStream cuerpo = new ByteArrayOutputStream();
        try (InputStream in = ex.getRequestBody()) {
            byte[] buffer = new byte[4096];
            int leidos;
            while ((leidos = in.read(buffer)) != -1) {
                cuerpo.write(buffer, 0, leidos);
                if (cuerpo.size() > MAX_CUERPO) {
                    throw new IllegalArgumentException("Formulario demasiado grande");
                }
            }
        }
        Map<String, String> campos = new HashMap<>();
        for (String par : cuerpo.toString(StandardCharsets.UTF_8).split("&")) {
            if (par.isEmpty()) {
                continue;
            }
            String[] kv = par.split("=", 2);
            campos.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        return campos;
    }

    private static int entero(String texto) {
        try {
            return Integer.parseInt(texto == null ? "" : texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Identificador inválido");
        }
    }

    private static void redirigir(HttpExchange ex, String destino) throws IOException {
        ex.getResponseHeaders().add("Location", destino);
        ex.sendResponseHeaders(303, -1);
    }

    private static void enviarHtml(HttpExchange ex, int estado, String html) throws IOException {
        ex.getResponseHeaders().add("Content-Security-Policy", "default-src 'self'; style-src 'unsafe-inline'");
        enviar(ex, estado, "text/html", html);
    }

    private static void enviar(HttpExchange ex, int estado, String tipo, String cuerpo) throws IOException {
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", tipo + "; charset=utf-8");
        ex.getResponseHeaders().add("X-Content-Type-Options", "nosniff");
        ex.sendResponseHeaders(estado, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }
}
