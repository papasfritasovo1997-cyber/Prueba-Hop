package ec.edu.epn.antdron.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ec.edu.epn.antdron.hormiga.TipoHormiga;
import ec.edu.epn.antdron.persistencia.BaseDatos;
import ec.edu.epn.antdron.persistencia.HormigueroServicio;
import ec.edu.epn.xp.pruebas.Historia;

@Historia("HU-11")
class ServidorWebTest {

    @TempDir
    Path dir;

    private ServidorWeb servidor;
    private String base;
    private final HttpClient cliente = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

    @BeforeEach
    void iniciar() throws IOException, SQLException {
        BaseDatos bd = new BaseDatos(dir.resolve("web.db"));
        bd.inicializar();
        servidor = new ServidorWeb(new HormigueroServicio(bd, TipoHormiga.SOLDADO), "Grupo 01 · <Transformers>");
        servidor.iniciar(0);
        base = "http://localhost:" + servidor.puerto();
    }

    @AfterEach
    void detener() {
        servidor.detener();
    }

    private HttpResponse<String> get(String ruta, String cookie) throws IOException, InterruptedException {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + ruta)).GET();
        if (cookie != null) {
            b.header("Cookie", cookie);
        }
        return cliente.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String ruta, Map<String, String> campos, String cookie)
            throws IOException, InterruptedException {
        String cuerpo = campos.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + ruta))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo));
        if (cookie != null) {
            b.header("Cookie", cookie);
        }
        return cliente.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String cookie(HttpResponse<?> r) {
        return r.headers().firstValue("Set-Cookie").orElseThrow().split(";")[0];
    }

    @Test
    void sinSesionRedirigeAlLogin() throws Exception {
        assertEquals("/login", get("/", null).headers().firstValue("Location").orElseThrow());
        assertEquals("/login", get("/hormiguero", null).headers().firstValue("Location").orElseThrow());
        assertEquals("/login", post("/hormigas", Map.of("sexo", "HEMBRA"), null).headers()
                .firstValue("Location").orElseThrow());
        HttpResponse<String> login = get("/login", null);
        assertEquals(200, login.statusCode());
        assertTrue(login.body().contains("Grupo 01 · &lt;Transformers&gt;"), "el texto se escapa");
        assertEquals(404, get("/nada", null).statusCode());
        assertEquals("OK", get("/salud", null).body());
    }

    @Test
    void registroCrearYAlimentarHormigas() throws Exception {
        HttpResponse<String> registro = post("/registro", Map.of("usuario", "kgd_user", "clave", "clave1"), null);
        assertEquals(303, registro.statusCode());
        String sesion = cookie(registro);
        assertTrue(registro.headers().firstValue("Set-Cookie").orElseThrow().contains("HttpOnly"));

        String pagina = get("/hormiguero", sesion).body();
        assertTrue(pagina.contains("Tu hormiguero está vacío"));
        assertTrue(pagina.contains("se transforma en <b>HSoldado</b>"));

        post("/hormigas", Map.of("sexo", "HEMBRA"), sesion);
        pagina = get("/hormiguero", sesion).body();
        assertTrue(pagina.contains("Nació la HLarva #1"));
        assertTrue(pagina.contains("<td>HLarva</td>"));

        post("/hormigas/alimentar", Map.of("id", "1", "alimento", "Carnívoro"), sesion);
        pagina = get("/hormiguero", sesion).body();
        assertTrue(pagina.contains("se transformó en HSoldado"));
        assertTrue(pagina.contains("<td>HSoldado</td>"));
        assertFalse(get("/hormiguero", sesion).body().contains("se transformó"), "el aviso se muestra una vez");

        post("/hormigas/alimentar", Map.of("id", "1", "alimento", "Néctar"), sesion);
        pagina = get("/hormiguero", sesion).body();
        assertTrue(pagina.contains("murió"));
        assertTrue(pagina.contains("class=\"MUERTA\""));

        post("/hormigas/alimentar", Map.of("id", "abc", "alimento", "Néctar"), sesion);
        assertTrue(get("/hormiguero", sesion).body().contains("Error: Identificador inválido"));
    }

    @Test
    void loginLogoutYErrores() throws Exception {
        post("/registro", Map.of("usuario", "ana", "clave", "clave1"), null);
        assertEquals(401, post("/login", Map.of("usuario", "ana", "clave", "mala"), null).statusCode());
        assertEquals(400, post("/registro", Map.of("usuario", "ana", "clave", "clave1"), null).statusCode());

        HttpResponse<String> login = post("/login", Map.of("usuario", "ana", "clave", "clave1"), null);
        assertEquals("/hormiguero", login.headers().firstValue("Location").orElseThrow());
        String sesion = cookie(login);
        assertEquals(200, get("/hormiguero", sesion).statusCode());

        post("/logout", Map.of(), sesion);
        assertEquals("/login", get("/hormiguero", sesion).headers().firstValue("Location").orElseThrow());
    }
}
