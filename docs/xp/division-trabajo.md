# División del trabajo (6 integrantes)

Cada integrante tiene **un rol XP**, **una pieza del framework** (parte 1), **historias de la aplicación** (parte 2) y **un diagrama o documento**. La carga se equilibró en puntos de historia (8 a 10 por persona) y todos programan en parejas que rotan cada iteración (`xp parejas`).

| # | Rol XP | Framework (Iteración 1) | Aplicación (Iteraciones 2-4) | Puntos | Diagrama / documento |
|---|---|---|---|---|---|
| 1 | **Entrenador (Coach)** | `cli/XpCli` + integración continua (XP-06) | Autómata y bomba (HU-06) | 8 | Grafo con gramática del autómata, estándar de código y DoD |
| 2 | **Cliente** | `planificacion`: `HistoriaUsuario`, `LectorBacklog` (XP-01) | Autenticación (HU-01), presentar equipo (HU-02), vuelo y fuente de poder (HU-09), `App` | 8 | Diagrama de casos de uso, historias de usuario |
| 3 | **Encargado de pruebas** | `pruebas`: `@Historia`, `TrazabilidadExtension`, `ReporteAceptacion` (XP-05) | Generar `Grupo##.csv` (HU-03), leer con loading (HU-04) | 9 | Reporte de pruebas de aceptación |
| 4 | **Encargado de seguimiento** | `planificacion`: `Iteracion`, `PlanEntregas`, `Velocidad` (XP-02) | Hormigas y alimentos (HU-05), idiomas (HU-10) | 8 | Diagrama de clases, plan de entregas |
| 5 | **Consultor** | `diseno`: `@TarjetaCRC`, `GeneradorCRC` (XP-04) | IA buscar (HU-07), armas (HU-08), Docker (HU-13) | 9 | Diagrama de arquitectura, tarjetas CRC |
| 6 | **Gestor** | `desarrollo`: `Equipo`, `LectorEquipo`, `RotacionParejas` (XP-03) | Pantallas web (HU-11), persistencia SQLite (HU-12) | 9 | Modelo entidad-relación |

## Archivos por integrante

| # | Código principal | Pruebas |
|---|---|---|
| 1 | `xp-framework/.../cli/XpCli.java`, `antdron/.../automata/*`, `antdron/.../bomba/Bomba.java`, `.github/workflows/ci.yml` | `XpCliTest`, `AutomataTest`, `BombaTest` |
| 2 | `.../planificacion/{HistoriaUsuario,LectorBacklog,Prioridad,EstadoHistoria}.java`, `antdron/.../auth/*`, `App.java`, `FuentePoder`, `TurboReactor` | `LectorBacklogTest`, `AutenticadorTest`, `AppTest` |
| 3 | `xp-framework/.../pruebas/*`, `antdron/.../sistema/*` | `ReporteAceptacionTest`, `SistemaRusoTest` |
| 4 | `.../planificacion/{Iteracion,PlanEntregas,Velocidad}.java`, `antdron/.../hormiga/*`, `alimento/*`, `pkAntCiberDronContract/IHormiga`, `Idioma`, `AspectoLinguistico`, `ExpertoParlante`, `ModuloIdiomas` | `PlanEntregasTest`, `HormigaTest`, `AntCiberDronTest` (HU-10) |
| 5 | `xp-framework/.../diseno/*`, `AntCiberDron`, `Arma`, `Metralleta`, `Laser`, `pkAntCiberDronContract/IIA`, `Dockerfile`, `docker-compose.yml` | `GeneradorCRCTest`, `AntCiberDronTest` (HU-07, HU-08), `DockerizacionTest` |
| 6 | `xp-framework/.../desarrollo/*`, `antdron/.../persistencia/*`, `antdron/.../web/*` | `EquipoYParejasTest`, `HormigueroServicioTest`, `ServidorWebTest` |

> El Integrante 1 sube su parte por su cuenta (no está en este commit inicial). Hasta que lo haga, el módulo `antdron` no compila porque `AntCiberDron` y `App` usan `Bomba` y el autómata, y la CI no existe todavía. Es la integración planificada del final de la iteración 3.

## Parejas por iteración (rotación del framework)

Generado con `xp parejas` usando el orden de `config/integrantes.csv`:

| Iteración | Pareja A | Pareja B | Pareja C |
|---|---|---|---|
| 1 | 1 – 6 | 2 – 5 | 3 – 4 |
| 2 | 1 – 5 | 6 – 4 | 2 – 3 |
| 3 | 1 – 4 | 5 – 3 | 6 – 2 |
| 4 | 1 – 3 | 4 – 2 | 5 – 6 |

Cada pareja trabaja en las historias de sus dos integrantes; el dueño de la historia conduce (driver) y el otro revisa (navigator), y cambian cada 30 minutos.
