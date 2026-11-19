package ec.edu.epn.antdron.persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Archivo SQLite del hormiguero virtual. Cada operación abre su propia conexión y corre en una transacción,
 * así el archivo no queda bloqueado entre operaciones (importante en Windows).
 */
final class BaseDatos {

    static final String CONTROLADOR = "lib/sqlite-jdbc-3.54.1.0.jar";

    @FunctionalInterface
    interface Trabajo<T> {
        T ejecutar(Connection conexion) throws SQLException;
    }

    private final Path archivo;
    private final String url;

    BaseDatos(Path archivo) {
        this.archivo = archivo.toAbsolutePath();
        this.url = "jdbc:sqlite:" + this.archivo;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No se encontró el controlador de SQLite: agregue " + CONTROLADOR
                    + " al classpath", e);
        }
        try {
            Files.createDirectories(this.archivo.getParent());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear la carpeta de " + this.archivo, e);
        }
        String esquema = leerEsquema();
        enTransaccion(c -> {
            try (Statement st = c.createStatement()) {
                for (String sentencia : esquema.split(";")) {
                    if (!sentencia.isBlank()) {
                        st.executeUpdate(sentencia);
                    }
                }
            }
            return null;
        });
    }

    Path getArchivo() {
        return archivo;
    }

    /** Ejecuta {@code trabajo} en una transacción: si algo falla no se guarda nada. */
    <T> T enTransaccion(Trabajo<T> trabajo) {
        try (Connection c = DriverManager.getConnection(url)) {
            try (Statement st = c.createStatement()) {
                // SQLite solo valida las claves foráneas si se activan en cada conexión.
                st.execute("PRAGMA foreign_keys = ON");
                st.execute("PRAGMA busy_timeout = 5000");
            }
            c.setAutoCommit(false);
            try {
                T resultado = trabajo.ejecutar(c);
                c.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error en la base de datos " + archivo.getFileName() + ": "
                    + e.getMessage(), e);
        }
    }

    private static String leerEsquema() {
        try (InputStream in = BaseDatos.class.getResourceAsStream("esquema.sql")) {
            if (in == null) {
                throw new IllegalStateException("No se encontró esquema.sql junto a " + BaseDatos.class.getName());
            }
            StringBuilder sql = new StringBuilder();
            for (String linea : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                if (!linea.strip().startsWith("--")) {
                    sql.append(linea).append('\n');
                }
            }
            return sql.toString();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer esquema.sql", e);
        }
    }
}
