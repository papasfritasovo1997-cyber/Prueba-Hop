-- Esquema SQLite del hormiguero virtual (modelo ER en docs/diagramas/modelo-er.md).
-- Se ejecuta cada vez que se abre la base: solo crea lo que todavía no existe.

CREATE TABLE IF NOT EXISTS usuario (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_usuario TEXT    NOT NULL UNIQUE CHECK (length(nombre_usuario) BETWEEN 3 AND 20),
    clave_hash     TEXT    NOT NULL,
    creado_en      TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS hormiguero (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INTEGER NOT NULL UNIQUE REFERENCES usuario (id) ON DELETE CASCADE,
    nombre     TEXT    NOT NULL,
    creado_en  TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS hormiga (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    hormiguero_id INTEGER NOT NULL REFERENCES hormiguero (id) ON DELETE CASCADE,
    tipo          TEXT    NOT NULL CHECK (tipo IN ('HLarva', 'HSoldado', 'HZángano', 'HRastreadora')),
    sexo          TEXT    NOT NULL CHECK (sexo IN ('HEMBRA', 'MACHO')),
    estado        TEXT    NOT NULL CHECK (estado IN ('VIVA', 'MUERTA', 'TRANSFORMADA')),
    creada_en     TEXT    NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_hormiga_hormiguero ON hormiga (hormiguero_id);

CREATE TABLE IF NOT EXISTS alimentacion (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    hormiga_id INTEGER NOT NULL REFERENCES hormiga (id) ON DELETE CASCADE,
    alimento   TEXT    NOT NULL,
    resultado  TEXT    NOT NULL CHECK (resultado IN ('VIVA', 'MUERTA', 'TRANSFORMADA')),
    fecha      TEXT    NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_alimentacion_hormiga ON alimentacion (hormiga_id);
