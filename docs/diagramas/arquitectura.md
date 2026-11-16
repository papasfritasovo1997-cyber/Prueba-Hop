# Diagrama de arquitectura

**Responsable:** Integrante 5 (Consultor).

Aplicación Java 17 en dos módulos Maven. Una sola imagen Docker ofrece dos modos: **consola** (requerimientos de la KGD) y **web** (hormiguero virtual).

```mermaid
flowchart TB
    subgraph Usuarios
        prof["Profesor / integrantes<br/>(terminal)"]
        kgd["Usuarios KGD<br/>(navegador)"]
        dev["Equipo XP<br/>(CLI y GitHub)"]
    end

    subgraph Docker["Docker Compose · imagen antdron:latest (eclipse-temurin:17-jre)"]
        subgraph consola["Servicio consola · java -jar app.jar consola"]
            app["App<br/>(modo consola)"]
        end
        subgraph web["Servicio web · :8080"]
            http["ServidorWeb<br/>(HttpServer del JDK)"]
            servicio["HormigueroServicio"]
        end
    end

    subgraph antdron["Módulo antdron (aplicación)"]
        auth["auth<br/>Autenticador · PresentadorEquipo"]
        sistema["sistema<br/>SistemaRuso · GeneradorCsvGrupo · Loading"]
        bomba["bomba + automata<br/>Bomba · Automata · Gramatica"]
        dron["antciberdron<br/>AntCiberDron (IIA) · armas · energía · idiomas"]
        hormiga["hormiga + alimento<br/>Hormiga · HLarva · TIPO_HORMIGA"]
        contrato["pkAntCiberDronContract<br/>IHormiga · IIA"]
    end

    subgraph framework["Módulo xp-framework"]
        plan["planificacion"]
        diseno["diseno"]
        desarrollo["desarrollo<br/>Equipo · LectorEquipo"]
        pruebas["pruebas<br/>@Historia"]
        cli["cli<br/>XpCli"]
    end

    subgraph datos["Datos"]
        config[("config/<br/>equipo.properties<br/>integrantes.csv")]
        csv[("data/Grupo##.csv")]
        db[("SQLite<br/>volumen hormiguero-db")]
        backlog[("xp/historias.csv")]
    end

    gha["GitHub Actions<br/>mvn verify · reporte de aceptación · docker compose"]

    prof --> app
    kgd -->|HTTP| http
    dev --> cli
    dev --> gha

    app --> auth & sistema & bomba & dron & hormiga
    http --> servicio --> hormiga
    servicio --> db
    dron --> bomba
    dron --> contrato
    hormiga --> contrato
    auth --> desarrollo
    sistema --> csv
    desarrollo --> config
    cli --> plan & diseno & desarrollo & pruebas
    plan --> backlog
```

## Capas y decisiones

| Capa | Paquetes | Decisión |
|---|---|---|
| Presentación | `App` (consola), `web` (HTML generado en el servidor) | Sin frameworks web: el `HttpServer` del JDK basta para pocas pantallas (funcionalidad mínima). |
| Dominio | `hormiga`, `alimento`, `antciberdron`, `bomba`, `automata`, `sistema` | Reglas puras y probadas con JUnit; no dependen de la consola ni de la base de datos. |
| Contratos | `pkAntCiberDronContract` | Interfaces del enunciado (`IHormiga`, `IIA`). |
| Persistencia | `persistencia` | SQLite embebido (un archivo): no requiere otro contenedor. |
| Proceso XP | `xp-framework` | Independiente de la aplicación; la aplicación lo usa para el equipo y las pruebas de aceptación. |
| Despliegue | `Dockerfile` multi-etapa, `docker-compose.yml` | La etapa de build ejecuta todas las pruebas; la imagen final solo lleva el JRE y el jar, con un usuario sin privilegios. |
