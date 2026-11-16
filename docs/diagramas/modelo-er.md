# Modelo entidad-relación

**Responsable:** Integrante 6 (Gestor). Base de datos SQLite del hormiguero virtual (`antdron/.../persistencia/BaseDatos.java`). En Docker se guarda en el volumen `hormiguero-db`.

```mermaid
erDiagram
    USUARIO_KGD ||--|| HORMIGUERO : "tiene"
    HORMIGUERO ||--o{ HORMIGA : "contiene"
    HORMIGA ||--o{ ALIMENTACION : "registra"

    USUARIO_KGD {
        INTEGER id PK
        TEXT nombre_usuario UK "3-20 letras, números o _"
        TEXT clave_hash "sal$sha256(sal+clave)"
        TEXT creado_en
    }
    HORMIGUERO {
        INTEGER id PK
        INTEGER usuario_id FK,UK
        TEXT nombre
        TEXT creado_en
    }
    HORMIGA {
        INTEGER id PK
        INTEGER hormiguero_id FK
        TEXT tipo "HLarva, HSoldado, HZángano, HRastreadora"
        TEXT sexo "HEMBRA o MACHO"
        TEXT estado "VIVA o MUERTA"
        TEXT creada_en
    }
    ALIMENTACION {
        INTEGER id PK
        INTEGER hormiga_id FK
        TEXT alimento "Néctar, Carnívoro, Omnívoro, Herbívoro"
        TEXT resultado "estado después de comer"
        TEXT fecha
    }
```

## Reglas

- Cada usuario KGD tiene exactamente un hormiguero, creado en la misma transacción del registro.
- Toda hormiga nace con `tipo = HLarva` y `estado = VIVA`. Al alimentarla se aplican las reglas del dominio (`Hormiga.comer`): si la larva come el alimento del TIPO_HORMIGA del grupo, `tipo` cambia a ese tipo; si come otra cosa distinta del néctar, `estado` pasa a `MUERTA`.
- Cada alimentación queda registrada en `ALIMENTACION` (historial).
- Un usuario solo puede ver y alimentar las hormigas de su hormiguero (se filtra por `hormiguero_id`).
- Las claves foráneas se activan con `PRAGMA foreign_keys = ON`.
