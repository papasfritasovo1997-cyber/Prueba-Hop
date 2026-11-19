package ec.edu.epn.antdron.persistencia;


import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import ec.edu.epn.xp.pruebas.AntesDeCada;
import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Prueba;
import ec.edu.epn.xp.pruebas.Temporal;

import ec.edu.epn.antdron.config.Claves;
import ec.edu.epn.antdron.hormiga.TipoHormiga;
import ec.edu.epn.xp.pruebas.Historia;

@Historia({"HU-11", "HU-12"})
class HormigueroServicioTest {


    Path dir = Temporal.directorio();

    private HormigueroServicio servicio;

    @AntesDeCada
    void preparar() {
        servicio = new HormigueroServicio(dir.resolve("sub"), TipoHormiga.ZANGANO);
    }

    @Prueba
    void registraYAutenticaUsuariosConClaveConSal() {
        int id = servicio.registrar("kgd_1", "secreta");
        Afirmar.igual(Optional.of(id), servicio.autenticar("kgd_1", "secreta"));
        Afirmar.igual(Optional.empty(), servicio.autenticar("kgd_1", "otra"));
        Afirmar.igual(Optional.empty(), servicio.autenticar("nadie", "secreta"));
        Afirmar.igual("kgd_1", servicio.nombreUsuario(id));
        Afirmar.lanza(IllegalArgumentException.class, () -> servicio.registrar("kgd_1", "secreta"));
        Afirmar.lanza(IllegalArgumentException.class, () -> servicio.registrar("a", "secreta"));
        Afirmar.lanza(IllegalArgumentException.class, () -> servicio.registrar("valido", "123"));
        // La misma clave genera hashes distintos (sal aleatoria).
        int otro = servicio.registrar("kgd_2", "secreta");
        Afirmar.igual(Optional.of(otro), servicio.autenticar("kgd_2", "secreta"));
    }

    @Prueba
    void cicloDeVidaDeLaHormigaSePersiste() {
        int u = servicio.registrar("kgd", "clave");
        int h = servicio.crearHormiga(u, "MACHO");
        Afirmar.igual(List.of(new HormigaRegistro(h, "HLarva", "MACHO", "VIVA",
                servicio.listar(u).get(0).creadaEn())), servicio.listar(u));

        Afirmar.verdadero(servicio.alimentar(u, h, "nectar").contains("sigue viva"));
        Afirmar.verdadero(servicio.alimentar(u, h, "Omnívoro").contains("se transformó en HZángano"));
        Afirmar.igual("HZángano", servicio.listar(u).get(0).tipo());
        Afirmar.igual("VIVA", servicio.listar(u).get(0).estado());

        Afirmar.verdadero(servicio.alimentar(u, h, "omnivoro").contains("sigue viva"));
        Afirmar.verdadero(servicio.alimentar(u, h, "Carnívoro").contains("murió"));
        Afirmar.igual("MUERTA", servicio.listar(u).get(0).estado());
        Afirmar.verdadero(servicio.alimentar(u, h, "omnivoro").contains("está muerta"));
    }

    @Prueba
    void cadaUsuarioSoloVeYAlimentaSusHormigas() {
        int ana = servicio.registrar("ana", "clave");
        int luis = servicio.registrar("luis", "clave");
        int h = servicio.crearHormiga(ana, "HEMBRA");
        Afirmar.verdadero(servicio.listar(luis).isEmpty());
        Afirmar.lanza(IllegalArgumentException.class, () -> servicio.alimentar(luis, h, "nectar"));
        Afirmar.lanza(IllegalArgumentException.class, () -> servicio.alimentar(ana, h, "pizza"));
        Afirmar.lanza(IllegalArgumentException.class, () -> servicio.crearHormiga(ana, "OTRO"));
    }

    @Prueba
    void lasHormigasSeGuardanEnLaBaseSqlite() throws Exception {
        int u = servicio.registrar("kgd", "clave");
        int h = servicio.crearHormiga(u, "HEMBRA");
        servicio.alimentar(u, h, "Omnívoro");

        Path db = dir.resolve("sub").resolve("hormiguero.db");
        Afirmar.igual(db.toAbsolutePath(), servicio.getArchivoBaseDatos());
        Afirmar.igual("SQLite format 3", new String(Files.readAllBytes(db), 0, 15, StandardCharsets.US_ASCII));
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db);
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery("SELECT h.id, h.tipo, h.sexo, h.estado, u.nombre_usuario,"
                        + " (SELECT COUNT(*) FROM alimentacion a WHERE a.hormiga_id = h.id)"
                        + " FROM hormiga h JOIN hormiguero hg ON hg.id = h.hormiguero_id"
                        + " JOIN usuario u ON u.id = hg.usuario_id")) {
            Afirmar.verdadero(rs.next(), "la hormiga no está en la tabla hormiga");
            Afirmar.igual(h, rs.getInt(1));
            Afirmar.igual("HZángano", rs.getString(2));
            Afirmar.igual("HEMBRA", rs.getString(3));
            Afirmar.igual("VIVA", rs.getString(4));
            Afirmar.igual("kgd", rs.getString(5));
            Afirmar.igual(1, rs.getInt(6));
            Afirmar.falso(rs.next());
        }

        // Otra instancia (como al reiniciar la aplicación) lee los mismos datos.
        HormigueroServicio reabierto = new HormigueroServicio(dir.resolve("sub"), TipoHormiga.ZANGANO);
        Afirmar.igual(Optional.of(u), reabierto.autenticar("kgd", "clave"));
        Afirmar.igual(servicio.listar(u), reabierto.listar(u));
    }

    @Prueba
    void laBaseRechazaHormigasSinHormigueroOConDatosInvalidos() throws Exception {
        int u = servicio.registrar("kgd", "clave");
        servicio.crearHormiga(u, "MACHO");
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + servicio.getArchivoBaseDatos());
                Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            String insertar = "INSERT INTO hormiga (hormiguero_id, tipo, sexo, estado, creada_en) VALUES ";
            Afirmar.lanza(SQLException.class, () -> st.executeUpdate(insertar + "(99, 'HLarva', 'MACHO', 'VIVA', 'x')"));
            Afirmar.lanza(SQLException.class, () -> st.executeUpdate(insertar + "(1, 'HLarva', 'OTRO', 'VIVA', 'x')"));
            Afirmar.lanza(SQLException.class, () -> st.executeUpdate(insertar + "(1, 'HGato', 'MACHO', 'VIVA', 'x')"));
            Afirmar.lanza(SQLException.class,
                    () -> st.executeUpdate("INSERT INTO usuario (nombre_usuario, clave_hash, creado_en) VALUES ('kgd', 'h', 'x')"));
        }
        Afirmar.igual(1, servicio.listar(u).size());
    }

    @Prueba
    void importaUnaSolaVezLosCsvDeLaVersionAnterior() throws Exception {
        Path anterior = dir.resolve("anterior");
        Files.createDirectories(anterior);
        String hash = "abcd$" + Claves.sha256("abcd" + "clave");
        Files.writeString(anterior.resolve("usuarios.csv"),
                "id;nombre_usuario;clave_hash;creado_en\n3;viejo;" + hash + ";2026-11-16 10:15:02\n");
        Files.writeString(anterior.resolve("hormigueros.csv"),
                "id;usuario_id;nombre;creado_en\n5;3;Hormiguero de viejo;2026-11-16 10:15:02\n");
        Files.writeString(anterior.resolve("hormigas.csv"), "id;hormiguero_id;tipo;sexo;estado;creada_en\n"
                + "7;5;HZángano;HEMBRA;VIVA;2026-11-16 10:16:40\n8;5;HLarva;MACHO;VIVA;2026-11-16 10:17:05\n");
        Files.writeString(anterior.resolve("alimentaciones.csv"),
                "id;hormiga_id;alimento;resultado;fecha\n1;7;Omnívoro;VIVA;2026-11-16 10:16:50\n");

        HormigueroServicio migrado = new HormigueroServicio(anterior, TipoHormiga.ZANGANO);
        Afirmar.igual(Optional.of(3), migrado.autenticar("viejo", "clave"));
        Afirmar.igual(List.of(new HormigaRegistro(7, "HZángano", "HEMBRA", "VIVA", "2026-11-16 10:16:40"),
                new HormigaRegistro(8, "HLarva", "MACHO", "VIVA", "2026-11-16 10:17:05")), migrado.listar(3));

        // Los nuevos id continúan después de los importados y abrir otra vez no duplica nada.
        Afirmar.igual(9, migrado.crearHormiga(3, "HEMBRA"));
        Afirmar.igual(3, new HormigueroServicio(anterior, TipoHormiga.ZANGANO).listar(3).size());
    }
}
