package ec.edu.epn.antdron.persistencia;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
 * HLarva) y alimentación aplicando las reglas del dominio. Guarda las tablas del modelo ER en la base
 * SQLite {@value #ARCHIVO} dentro del directorio indicado.
 */
public final class HormigueroServicio {

    public static final String ARCHIVO = "hormiguero.db";

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final BaseDatos bd;
    private final TipoHormiga tipoDelGrupo;

    public HormigueroServicio(Path directorio, TipoHormiga tipoDelGrupo) {
        this.tipoDelGrupo = Objects.requireNonNull(tipoDelGrupo);
        this.bd = new BaseDatos(directorio.resolve(ARCHIVO));
        importarCsvAnteriores(directorio);
    }

    public TipoHormiga getTipoDelGrupo() {
        return tipoDelGrupo;
    }

    /** Archivo SQLite con los usuarios, hormigueros, hormigas y alimentaciones. */
    public Path getArchivoBaseDatos() {
        return bd.getArchivo();
    }

    // ------------------------------------------------------------ Usuarios

    public synchronized int registrar(String usuario, String clave) {
        if (usuario == null || !usuario.matches("[A-Za-z0-9_]{3,20}")) {
            throw new IllegalArgumentException("El usuario debe tener de 3 a 20 letras, números o _");
        }
        if (clave == null || clave.length() < 4) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres");
        }
        return bd.enTransaccion(c -> {
            if (buscarUsuario(c, usuario).isPresent()) {
                throw new IllegalArgumentException("El usuario '" + usuario + "' ya existe");
            }
            int id = insertar(c, "INSERT INTO usuario (nombre_usuario, clave_hash, creado_en) VALUES (?, ?, ?)",
                    usuario, hashConSal(clave), ahora());
            insertar(c, "INSERT INTO hormiguero (usuario_id, nombre, creado_en) VALUES (?, ?, ?)",
                    id, "Hormiguero de " + usuario, ahora());
            return id;
        });
    }

    public Optional<Integer> autenticar(String usuario, String clave) {
        if (usuario == null || clave == null) {
            return Optional.empty();
        }
        return bd.enTransaccion(c -> buscarUsuario(c, usuario.trim()))
                .filter(u -> verificar(clave, u.claveHash())).map(Usuario::id);
    }

    public String nombreUsuario(int usuarioId) {
        return bd.enTransaccion(c -> {
            try (PreparedStatement ps = c.prepareStatement("SELECT nombre_usuario FROM usuario WHERE id = ?")) {
                ps.setInt(1, usuarioId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Usuario inexistente");
                    }
                    return rs.getString(1);
                }
            }
        });
    }

    private record Usuario(int id, String claveHash) {
    }

    private static Optional<Usuario> buscarUsuario(Connection c, String usuario) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id, clave_hash FROM usuario WHERE nombre_usuario = ?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(new Usuario(rs.getInt(1), rs.getString(2))) : Optional.empty();
            }
        }
    }

    // ------------------------------------------------------------ Hormigas

    public int crearHormiga(int usuarioId, String sexo) {
        Hormiga larva = new HLarva(0, sexo, tipoDelGrupo);
        return bd.enTransaccion(c -> insertar(c,
                "INSERT INTO hormiga (hormiguero_id, tipo, sexo, estado, creada_en) VALUES (?, ?, ?, ?, ?)",
                hormigueroId(c, usuarioId), larva.getTipo(), larva.getSexo(), larva.getEstado(), ahora()));
    }

    public List<HormigaRegistro> listar(int usuarioId) {
        return bd.enTransaccion(c -> listar(c, usuarioId));
    }

    private static List<HormigaRegistro> listar(Connection c, int usuarioId) throws SQLException {
        String sql = "SELECT h.id, h.tipo, h.sexo, h.estado, h.creada_en FROM hormiga h"
                + " JOIN hormiguero hg ON hg.id = h.hormiguero_id WHERE hg.usuario_id = ? ORDER BY h.id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                List<HormigaRegistro> hormigas = new ArrayList<>();
                while (rs.next()) {
                    hormigas.add(new HormigaRegistro(rs.getInt(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getString(5)));
                }
                return hormigas;
            }
        }
    }

    /** Aplica {@link Hormiga#comer}, guarda el nuevo tipo/estado y el historial. Devuelve el mensaje para la pantalla. */
    public synchronized String alimentar(int usuarioId, int hormigaId, String nombreAlimento) {
        Alimento alimento = FabricaAlimentos.desde(nombreAlimento);
        return bd.enTransaccion(c -> {
            HormigaRegistro r = listar(c, usuarioId).stream().filter(h -> h.id() == hormigaId).findFirst()
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
            try (PreparedStatement ps = c.prepareStatement("UPDATE hormiga SET tipo = ?, estado = ? WHERE id = ?")) {
                ps.setString(1, tipo);
                ps.setString(2, estado);
                ps.setInt(3, r.id());
                ps.executeUpdate();
            }
            insertar(c, "INSERT INTO alimentacion (hormiga_id, alimento, resultado, fecha) VALUES (?, ?, ?, ?)",
                    r.id(), alimento.getNombre(), estado, ahora());
            return mensaje;
        });
    }

    private Hormiga reconstruir(HormigaRegistro r) {
        if ("HLarva".equals(r.tipo())) {
            return new HLarva(r.id(), r.sexo(), tipoDelGrupo);
        }
        return TipoHormiga.porNombre(r.tipo())
                .orElseThrow(() -> new IllegalStateException("Tipo de hormiga desconocido: " + r.tipo()))
                .crear(r.id(), r.sexo());
    }

    private static int hormigueroId(Connection c, int usuarioId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id FROM hormiguero WHERE usuario_id = ?")) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("El usuario no tiene hormiguero");
                }
                return rs.getInt(1);
            }
        }
    }

    // ------------------------------------------ Datos de la versión con CSV

    /** Archivo CSV anterior, tabla destino y columnas en el orden del archivo. */
    private static final String[][] TABLAS_CSV = {
        {"usuarios.csv", "usuario", "id", "nombre_usuario", "clave_hash", "creado_en"},
        {"hormigueros.csv", "hormiguero", "id", "usuario_id", "nombre", "creado_en"},
        {"hormigas.csv", "hormiga", "id", "hormiguero_id", "tipo", "sexo", "estado", "creada_en"},
        {"alimentaciones.csv", "alimentacion", "id", "hormiga_id", "alimento", "resultado", "fecha"},
    };

    /**
     * La versión anterior guardaba el hormiguero en CSV. Si la base no tiene usuarios y existen esos archivos,
     * se copian una sola vez conservando los id; los CSV no se modifican.
     */
    private void importarCsvAnteriores(Path directorio) {
        if (!Files.exists(directorio.resolve(TABLAS_CSV[0][0]))) {
            return;
        }
        bd.enTransaccion(c -> {
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM usuario")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return null;
                }
            }
            for (String[] tabla : TABLAS_CSV) {
                Path csv = directorio.resolve(tabla[0]);
                if (Files.exists(csv)) {
                    importar(c, csv, tabla[1], List.of(tabla).subList(2, tabla.length));
                }
            }
            return null;
        });
    }

    private static void importar(Connection c, Path csv, String tabla, List<String> columnas) throws SQLException {
        String sql = "INSERT INTO " + tabla + " (" + String.join(", ", columnas) + ") VALUES ("
                + "?, ".repeat(columnas.size() - 1) + "?)";
        List<String> lineas;
        try {
            lineas = Files.readAllLines(csv, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + csv, e);
        }
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            // La primera línea es la cabecera.
            for (String linea : lineas.subList(Math.min(1, lineas.size()), lineas.size())) {
                String[] f = linea.split(";", -1);
                if (linea.isBlank() || f.length != columnas.size()) {
                    continue;
                }
                ps.setInt(1, Integer.parseInt(f[0].strip()));
                for (int i = 1; i < f.length; i++) {
                    ps.setString(i + 1, f[i]);
                }
                ps.executeUpdate();
            }
        }
    }

    // ---------------------------------------------------------- Utilidades

    /** Ejecuta un INSERT con los valores dados y devuelve el id asignado por SQLite. */
    private static int insertar(Connection c, String sql, Object... valores) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < valores.length; i++) {
                ps.setObject(i + 1, valores[i]);
            }
            ps.executeUpdate();
        }
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
            rs.next();
            return rs.getInt(1);
        }
    }

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
