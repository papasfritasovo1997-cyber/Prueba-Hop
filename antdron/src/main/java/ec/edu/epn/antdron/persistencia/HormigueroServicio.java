package ec.edu.epn.antdron.persistencia;

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
import ec.edu.epn.antdron.hormiga.HLarva;
import ec.edu.epn.antdron.hormiga.Hormiga;
import ec.edu.epn.antdron.hormiga.TipoHormiga;
import ec.edu.epn.xp.desarrollo.Credenciales;
import ec.edu.epn.xp.diseno.TarjetaCRC;

/**
 * Casos de uso del hormiguero virtual: registrar usuarios KGD, crear hormigas (nacen como HLarva)
 * y alimentarlas aplicando las reglas del dominio.
 */
@TarjetaCRC(responsabilidades = {"Registrar y autenticar usuarios KGD", "Crear hormigas en el hormiguero",
        "Alimentar hormigas y guardar el resultado"},
        colaboradores = {"BaseDatos", "HLarva", "TipoHormiga", "FabricaAlimentos"})
public final class HormigueroServicio {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final BaseDatos bd;
    private final TipoHormiga tipoDelGrupo;

    public HormigueroServicio(BaseDatos bd, TipoHormiga tipoDelGrupo) {
        this.bd = Objects.requireNonNull(bd);
        this.tipoDelGrupo = Objects.requireNonNull(tipoDelGrupo);
    }

    public TipoHormiga getTipoDelGrupo() {
        return tipoDelGrupo;
    }

    // ------------------------------------------------------------ Usuarios

    public int registrar(String usuario, String clave) throws SQLException {
        if (usuario == null || !usuario.matches("[A-Za-z0-9_]{3,20}")) {
            throw new IllegalArgumentException("El usuario debe tener de 3 a 20 letras, números o _");
        }
        if (clave == null || clave.length() < 4) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres");
        }
        try (Connection c = bd.conectar()) {
            c.setAutoCommit(false);
            try {
                if (buscarUsuarioId(c, usuario).isPresent()) {
                    throw new IllegalArgumentException("El usuario '" + usuario + "' ya existe");
                }
                int id;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO usuario_kgd(nombre_usuario, clave_hash, creado_en) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, usuario);
                    ps.setString(2, hashConSal(clave));
                    ps.setString(3, ahora());
                    ps.executeUpdate();
                    id = idGenerado(ps);
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO hormiguero(usuario_id, nombre, creado_en) VALUES (?, ?, ?)")) {
                    ps.setInt(1, id);
                    ps.setString(2, "Hormiguero de " + usuario);
                    ps.setString(3, ahora());
                    ps.executeUpdate();
                }
                c.commit();
                return id;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public Optional<Integer> autenticar(String usuario, String clave) throws SQLException {
        if (usuario == null || clave == null) {
            return Optional.empty();
        }
        try (Connection c = bd.conectar(); PreparedStatement ps = c.prepareStatement(
                "SELECT id, clave_hash FROM usuario_kgd WHERE nombre_usuario = ?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && verificar(clave, rs.getString("clave_hash"))) {
                    return Optional.of(rs.getInt("id"));
                }
            }
        }
        return Optional.empty();
    }

    public String nombreUsuario(int usuarioId) throws SQLException {
        try (Connection c = bd.conectar(); PreparedStatement ps = c.prepareStatement(
                "SELECT nombre_usuario FROM usuario_kgd WHERE id = ?")) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Usuario inexistente");
                }
                return rs.getString(1);
            }
        }
    }

    // ------------------------------------------------------------ Hormigas

    public int crearHormiga(int usuarioId, String sexo) throws SQLException {
        // Valida el sexo con las reglas del dominio antes de guardar.
        Hormiga larva = new HLarva(0, sexo, tipoDelGrupo);
        try (Connection c = bd.conectar(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO hormiga(hormiguero_id, tipo, sexo, estado, creada_en) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, hormigueroId(c, usuarioId));
            ps.setString(2, larva.getTipo());
            ps.setString(3, larva.getSexo());
            ps.setString(4, larva.getEstado());
            ps.setString(5, ahora());
            ps.executeUpdate();
            return idGenerado(ps);
        }
    }

    public List<HormigaRegistro> listar(int usuarioId) throws SQLException {
        List<HormigaRegistro> lista = new ArrayList<>();
        try (Connection c = bd.conectar(); PreparedStatement ps = c.prepareStatement(
                "SELECT id, tipo, sexo, estado, creada_en FROM hormiga WHERE hormiguero_id = ? ORDER BY id")) {
            ps.setInt(1, hormigueroId(c, usuarioId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new HormigaRegistro(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                            rs.getString(5)));
                }
            }
        }
        return lista;
    }

    /** Aplica {@link Hormiga#comer} y guarda el nuevo tipo/estado. Devuelve un mensaje para la pantalla. */
    public String alimentar(int usuarioId, int hormigaId, String nombreAlimento) throws SQLException {
        Alimento alimento = FabricaAlimentos.desde(nombreAlimento);
        try (Connection c = bd.conectar()) {
            HormigaRegistro r = buscarHormiga(c, usuarioId, hormigaId)
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
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO alimentacion(hormiga_id, alimento, resultado, fecha) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, r.id());
                ps.setString(2, alimento.getNombre());
                ps.setString(3, estado);
                ps.setString(4, ahora());
                ps.executeUpdate();
            }
            return mensaje;
        }
    }

    private Hormiga reconstruir(HormigaRegistro r) {
        if ("HLarva".equals(r.tipo())) {
            return new HLarva(r.id(), r.sexo(), tipoDelGrupo);
        }
        return TipoHormiga.porNombre(r.tipo())
                .orElseThrow(() -> new IllegalStateException("Tipo de hormiga desconocido: " + r.tipo()))
                .crear(r.id(), r.sexo());
    }

    private Optional<HormigaRegistro> buscarHormiga(Connection c, int usuarioId, int hormigaId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id, tipo, sexo, estado, creada_en FROM hormiga WHERE id = ? AND hormiguero_id = ?")) {
            ps.setInt(1, hormigaId);
            ps.setInt(2, hormigueroId(c, usuarioId));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(new HormigaRegistro(rs.getInt(1), rs.getString(2), rs.getString(3),
                                rs.getString(4), rs.getString(5)))
                        : Optional.empty();
            }
        }
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

    private static Optional<Integer> buscarUsuarioId(Connection c, String usuario) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id FROM usuario_kgd WHERE nombre_usuario = ?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getInt(1)) : Optional.empty();
            }
        }
    }

    private static int idGenerado(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (!rs.next()) {
                throw new SQLException("No se generó el id");
            }
            return rs.getInt(1);
        }
    }

    private static String hashConSal(String clave) {
        byte[] sal = new byte[16];
        ALEATORIO.nextBytes(sal);
        String salHex = HexFormat.of().formatHex(sal);
        return salHex + "$" + Credenciales.sha256(salHex + clave);
    }

    private static boolean verificar(String clave, String almacenado) {
        int separador = almacenado.indexOf('$');
        if (separador < 0) {
            return false;
        }
        String sal = almacenado.substring(0, separador);
        return Credenciales.coincide(sal + clave, almacenado.substring(separador + 1));
    }

    private static String ahora() {
        return LocalDateTime.now().format(FECHA);
    }
}
