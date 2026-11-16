package ec.edu.epn.antdron.persistencia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Base de datos SQLite del hormiguero virtual (ver docs/diagramas/modelo-er.md). */
public final class BaseDatos {

    private static final String[] ESQUEMA = {
        """
        CREATE TABLE IF NOT EXISTS usuario_kgd (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre_usuario TEXT NOT NULL UNIQUE,
            clave_hash TEXT NOT NULL,
            creado_en TEXT NOT NULL)""",
        """
        CREATE TABLE IF NOT EXISTS hormiguero (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            usuario_id INTEGER NOT NULL UNIQUE REFERENCES usuario_kgd(id),
            nombre TEXT NOT NULL,
            creado_en TEXT NOT NULL)""",
        """
        CREATE TABLE IF NOT EXISTS hormiga (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            hormiguero_id INTEGER NOT NULL REFERENCES hormiguero(id),
            tipo TEXT NOT NULL,
            sexo TEXT NOT NULL,
            estado TEXT NOT NULL,
            creada_en TEXT NOT NULL)""",
        """
        CREATE TABLE IF NOT EXISTS alimentacion (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            hormiga_id INTEGER NOT NULL REFERENCES hormiga(id),
            alimento TEXT NOT NULL,
            resultado TEXT NOT NULL,
            fecha TEXT NOT NULL)"""
    };

    private final String url;

    public BaseDatos(Path archivo) throws IOException {
        Path padre = archivo.toAbsolutePath().getParent();
        if (padre != null) {
            Files.createDirectories(padre);
        }
        this.url = "jdbc:sqlite:" + archivo.toAbsolutePath();
    }

    public Connection conectar() throws SQLException {
        Connection c = DriverManager.getConnection(url);
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
        }
        return c;
    }

    public void inicializar() throws SQLException {
        try (Connection c = conectar(); Statement s = c.createStatement()) {
            for (String sql : ESQUEMA) {
                s.execute(sql);
            }
        }
    }
}
