package ec.edu.epn.antdron.persistencia;

import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import ec.edu.epn.antdron.alimento.Alimento;
import ec.edu.epn.antdron.alimento.FabricaAlimentos;
import ec.edu.epn.antdron.config.Claves;
import ec.edu.epn.antdron.hormiga.HLarva;
import ec.edu.epn.antdron.hormiga.Hormiga;
import ec.edu.epn.antdron.hormiga.TipoHormiga;

/**
 * Hormiguero virtual de los usuarios de la KGD: registro e ingreso, creación de hormigas (nacen como
 * HLarva) y alimentación aplicando las reglas del dominio. Guarda las tablas del modelo ER en CSV.
 */
public final class HormigueroServicio {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final TablaCsv usuarios;
    private final TablaCsv hormigueros;
    private final TablaCsv hormigas;
    private final TablaCsv alimentaciones;
    private final TipoHormiga tipoDelGrupo;

    public HormigueroServicio(Path directorio, TipoHormiga tipoDelGrupo) {
        this.tipoDelGrupo = Objects.requireNonNull(tipoDelGrupo);
        usuarios = new TablaCsv(directorio.resolve("usuarios.csv"), "id", "nombre_usuario", "clave_hash", "creado_en");
        hormigueros = new TablaCsv(directorio.resolve("hormigueros.csv"), "id", "usuario_id", "nombre", "creado_en");
        hormigas = new TablaCsv(directorio.resolve("hormigas.csv"),
                "id", "hormiguero_id", "tipo", "sexo", "estado", "creada_en");
        alimentaciones = new TablaCsv(directorio.resolve("alimentaciones.csv"),
                "id", "hormiga_id", "alimento", "resultado", "fecha");
    }

    public TipoHormiga getTipoDelGrupo() {
        return tipoDelGrupo;
    }

    // ------------------------------------------------------------ Usuarios

    public synchronized int registrar(String usuario, String clave) {
        if (usuario == null || !usuario.matches("[A-Za-z0-9_]{3,20}")) {
            throw new IllegalArgumentException("El usuario debe tener de 3 a 20 letras, números o _");
        }
        if (clave == null || clave.length() < 4) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres");
        }
        if (buscarUsuario(usuario).isPresent()) {
            throw new IllegalArgumentException("El usuario '" + usuario + "' ya existe");
        }
        int id = usuarios.insertar(usuario, hashConSal(clave), ahora());
        hormigueros.insertar(String.valueOf(id), "Hormiguero de " + usuario, ahora());
        return id;
    }

    public Optional<Integer> autenticar(String usuario, String clave) {
        if (usuario == null || clave == null) {
            return Optional.empty();
        }
        return buscarUsuario(usuario.trim()).filter(f -> verificar(clave, f[2])).map(f -> Integer.parseInt(f[0]));
    }

    public String nombreUsuario(int usuarioId) {
        return usuarios.filas().stream().filter(f -> Integer.parseInt(f[0]) == usuarioId).map(f -> f[1])
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Usuario inexistente"));
    }

    private Optional<String[]> buscarUsuario(String usuario) {
        return usuarios.filas().stream().filter(f -> f[1].equals(usuario)).findFirst();
    }

    // ------------------------------------------------------------ Hormigas

    public int crearHormiga(int usuarioId, String sexo) {
        Hormiga larva = new HLarva(0, sexo, tipoDelGrupo);
        return hormigas.insertar(String.valueOf(hormigueroId(usuarioId)), larva.getTipo(), larva.getSexo(),
                larva.getEstado(), ahora());
    }

    public List<HormigaRegistro> listar(int usuarioId) {
        String hormiguero = String.valueOf(hormigueroId(usuarioId));
        return hormigas.filas().stream().filter(f -> f[1].equals(hormiguero))
                .map(f -> new HormigaRegistro(Integer.parseInt(f[0]), f[2], f[3], f[4], f[5])).toList();
    }

    /** Aplica {@link Hormiga#comer}, guarda el nuevo tipo/estado y el historial. Devuelve el mensaje para la pantalla. */
    public synchronized String alimentar(int usuarioId, int hormigaId, String nombreAlimento) {
        Alimento alimento = FabricaAlimentos.desde(nombreAlimento);
        HormigaRegistro r = listar(usuarioId).stream().filter(h -> h.id() == hormigaId).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La hormiga no existe en su hormiguero"));
        if (Hormiga.MUERTA.equals(r.estado())) {
            return "La hormiga #" + r.id() + " está muerta y ya no puede comer.";
        }
        Hormiga hormiga = reconstruir(r);
        boolean vive = hormiga.comer(alimento);
        String tipo = r.tipo();
        String estado = hormiga.getEstado();
        String mensaje;
        if (hormiga instanceof HLarva larva && larva.getEvolucion().isPresent()) {
            Hormiga adulta = larva.getEvolucion().get();
            tipo = adulta.getTipo();
            estado = adulta.getEstado();
            mensaje = "La HLarva #" + r.id() + " comió " + alimento + " y se transformó en " + tipo + ".";
        } else if (vive) {
            mensaje = "La " + tipo + " #" + r.id() + " comió " + alimento + " y sigue viva.";
        } else {
            mensaje = "La " + tipo + " #" + r.id() + " comió " + alimento + " y murió.";
        }
        String nuevoTipo = tipo;
        String nuevoEstado = estado;
        hormigas.actualizar(r.id(), f -> {
            f[2] = nuevoTipo;
            f[4] = nuevoEstado;
            return f;
        });
        alimentaciones.insertar(String.valueOf(r.id()), alimento.getNombre(), estado, ahora());
        return mensaje;
    }

    private Hormiga reconstruir(HormigaRegistro r) {
        if ("HLarva".equals(r.tipo())) {
            return new HLarva(r.id(), r.sexo(), tipoDelGrupo);
        }
        return TipoHormiga.porNombre(r.tipo())
                .orElseThrow(() -> new IllegalStateException("Tipo de hormiga desconocido: " + r.tipo()))
                .crear(r.id(), r.sexo());
    }

    private int hormigueroId(int usuarioId) {
        String u = String.valueOf(usuarioId);
        return hormigueros.filas().stream().filter(f -> f[1].equals(u)).map(f -> Integer.parseInt(f[0]))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("El usuario no tiene hormiguero"));
    }

    // ---------------------------------------------------------- Utilidades

    private static String hashConSal(String clave) {
        byte[] sal = new byte[16];
        ALEATORIO.nextBytes(sal);
        String salHex = HexFormat.of().formatHex(sal);
        return salHex + "$" + Claves.sha256(salHex + clave);
    }

    private static boolean verificar(String clave, String almacenado) {
        int separador = almacenado.indexOf('$');
        if (separador < 0) {
            return false;
        }
        String sal = almacenado.substring(0, separador);
        return Claves.coincide(sal + clave, Claves.PREFIJO_SHA256 + almacenado.substring(separador + 1));
    }

    private static String ahora() {
        return LocalDateTime.now().format(FECHA);
    }
}
