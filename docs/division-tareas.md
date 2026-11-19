# División de tareas · Proyecto XP ANTDRON (6 integrantes, 2 días)

El proyecto tiene **dos partes independientes**:

| Parte | Carpeta | Qué es | Relación |
|---|---|---|---|
| **1. Framework XP** | `framework-xp/` | Dicta las directrices de XP (fases, prácticas, roles, reglas, DoD) y trae herramientas para planificar, medir, probar y **verificar** que se cumplen. | No sabe nada de la aplicación. |
| **2. Aplicación AntCiberDron** | `antdron/` | Lo que pide el enunciado, con pantallas **JavaFX**: login, `Grupo##.csv`, autómata, bomba, hormigas, IA, hormiguero virtual y Docker. | **Sigue** las directrices, pero su código no importa nada del framework: se compila sin él. Solo sus pruebas usan el ejecutor de pruebas del framework. |

No se usa Maven: se abre la carpeta en **VS Code** (Windows), que compila todo con los `.jar` de JavaFX de `lib/`; Docker compila lo mismo con `javac`. Las reglas completas están en `framework-xp/docs/guia-xp.md`.

## 1. Resumen por integrante

Cada integrante tiene **un rol XP**, **una historia del framework**, **historias de la aplicación** y **un diagrama o documento**. La carga está equilibrada en **8 o 9 puntos** (1 punto ≈ 1 hora ideal de trabajo en pareja). El backlog completo está en `xp/historias.csv`.

| # | Rol XP | Parte 1 · Framework | Parte 2 · Aplicación | Diagrama / documento | Pts |
|---|---|---|---|---|---|
| 1 | Entrenador (Coach) | XP-01 Núcleo y directrices (3) | HU-06 Autómata (3) · HU-14 Bomba (2) | Grafo con gramática del autómata, guía XP, estándar de código | 8 |
| Karen Mosquera | Cliente | XP-02 Historias de usuario (2) | HU-01 Autenticación (3) · HU-09 Vuelo y fuente de poder (2) · HU-15 Configuración por archivo (2) | Diagrama de casos de uso | 9 |
| Kened Lugmaña | Encargado de pruebas | XP-05 Pruebas y aceptación (3) | HU-03 Generar `Grupo##.csv` (3) · HU-04 Leer con loading (3) | Reporte de pruebas de aceptación | 9 |
| David Jumbo | Encargado de seguimiento | XP-03 Plan de entregas y velocidad (3) | HU-05 Hormigas y alimentos (3) · HU-10 Idiomas (2) · HU-02 Presentar equipo (1) | Diagrama de clases, plan de entregas | 9 |
| Daniel Mera | Consultor | XP-04 Diseño: metáfora y CRC (2) | HU-07 IA buscar (3) · HU-08 Armas (2) · HU-13 Docker (2) | Diagrama de arquitectura, tarjetas CRC, spikes | 9 |
| Leonel Morales | Gestor | XP-06 Equipo, parejas e integración continua (3) | HU-11 Pantallas del hormiguero (3) · HU-12 Persistencia (3) | Modelo entidad-relación, actas de reuniones | 9 |

Total: 16 puntos de framework + 37 de aplicación = 53 puntos.

## 2. Parte 1 · Framework XP (`framework-xp/`)

| Historia | Responsable | Paquete | Comandos |
|---|---|---|---|
| **XP-01** Núcleo y directrices | Sebastián Jibaja | `ec.edu.epn.xp` (`XpCli`, `ComandoXP`, `ContextoXP`), `ec.edu.epn.xp.directrices`, `docs/`, `.vscode/` | `ayuda`, `directrices`, `verificar` |
| **XP-02** Historias de usuario | Karen Mosquera | `ec.edu.epn.xp.planificacion` | `backlog` |
| **XP-03** Plan de entregas y velocidad | David Jumbo | `ec.edu.epn.xp.seguimiento` | `plan [--proponer N]`, `velocidad` |
| **XP-04** Diseño | Daniel Mera | `ec.edu.epn.xp.diseno` | `crc` |
| **XP-05** Pruebas y aceptación | Kened Lugmaña | `ec.edu.epn.xp.pruebas` (`@Prueba`, `@Historia`, `Afirmar`, `EjecutorPruebas`) | `aceptacion`, y «Pruebas + reporte de aceptación» en VS Code |
| **XP-06** Desarrollo | Leonel Morales | `ec.edu.epn.xp.desarrollo` | `equipo`, `parejas [ronda]` |

Cada comando implementa `ec.edu.epn.xp.ComandoXP` y se registra con una línea en `framework-xp/src/META-INF/services/ec.edu.epn.xp.ComandoXP`. Nadie toca el código de otro.

## 3. Parte 2 · Aplicación AntCiberDron (`antdron/`)

| Historia | Responsable | Paquete / clases | Pantalla JavaFX |
|---|---|---|---|
| **HU-01** Autenticación | Karen Mosquera | `auth.Autenticador`, `config` (`Equipo`, `Integrante`, `LectorConfiguracion`, `Claves`) | `VistaIngreso` |
| **HU-02** Presentar equipo | David Jumbo | `auth.PresentadorEquipo` | `PanelEquipo` |
| **HU-03** Generar `Grupo##.csv` | Kened Lugmaña | `sistema` (`GeneradorCsvGrupo`, `EstrategiaOTAN`, `CoordenadaUK`, `DiaSemana`) | — |
| **HU-04** Leer con loading | Kened Lugmaña | `sistema` (`SistemaRuso.readCoord`, `Loading`, `PresentadorCoordenadas`) | `PanelCoordenadas` |
| **HU-05** Hormigas y alimentos | David Jumbo | `pkAntCiberDronContract.IHormiga`, `hormiga`, `alimento` | — |
| **HU-06** Autómata | Sebastián Jibaja | `automata` (`Automata`, `ConstructorAutomata`, `Gramatica`, `ExportadorGrafo`, `LenguajesOTAN`) | `PanelAutomata`, `LienzoAutomata` |
| **HU-07** IA buscar arsenal | Daniel Mera | `pkAntCiberDronContract.IIA`, `antciberdron.AntCiberDron` | `PanelDron` (búsqueda) |
| **HU-08** Armas | Daniel Mera | `antciberdron` (`Arma`, `Metralleta`, `Laser`) | `PanelDron` (armas) |
| **HU-09** Vuelo y fuente de poder | Karen Mosquera | `antciberdron` (`FuentePoder`, `TurboReactor`) | `PanelDron` (energía) |
| **HU-10** Idiomas | David Jumbo | `antciberdron` (`ModuloIdiomas`, `ExpertoParlante`, `Idioma`, `AspectoLinguistico`) | `PanelDron` (idiomas) |
| **HU-11** Pantallas del hormiguero | Leonel Morales | `ui` (`VistaKgd`, `AntdronFx`, `VistaPrincipal`, `Ui`) | `VistaKgd` |
| **HU-12** Persistencia | Leonel Morales | `persistencia` (`HormigueroServicio`, `BaseDatos`, `esquema.sql`) | — |
| **HU-13** Docker | Daniel Mera | `Dockerfile`, `docker/iniciar.sh`, `docker-compose.yml` | GUI en el navegador (noVNC) |
| **HU-14** Bomba | Sebastián Jibaja | `bomba.Bomba` | `PanelAutomata` |
| **HU-15** Configuración por archivo | Karen Mosquera | `MotorAntdron`, `App` (consola) | «Recargar configuración» |

**Contratos entre secciones** (cada dueño los mantiene; un cambio se acuerda en la reunión de pie):

| Dueño | Contrato |
|---|---|
| Sebastián Jibaja | `new Bomba(List<String> cedulas)`: `debeExplotar(String geoposicion, String tipoArsenal)`, `evaluar(w)`, `lenguaje()`, `coordenadasObjetivo()`, `getAutomata()`. `LenguajesOTAN.expresion(d)`, `pertenece(d, w)`. |
| Karen Mosquera | `LectorConfiguracion.leer(Path)` → `Equipo` (`cedulas()`, `grupo()`, `integrantes()`). `Autenticador.intentar(usuario, clave)` → `ACEPTADO / RECHAZADO / BLOQUEADO`. `MotorAntdron.cargar(raiz)`. `FuentePoder`, `TurboReactor.volar(min, fuente)`. |
| Kened Lugmaña | `SistemaRuso.readCoord(String fileName)` → `List<CoordenadaUK>`; `GeneradorCsvGrupo.escribir(Equipo, Path)`. |
| David Jumbo | `IHormiga.comer(Alimento)`, `Hormiga`, `HLarva(TipoHormiga)` con `getEvolucion()`, `TipoHormiga.paraGrupo(int)`, `FabricaAlimentos.desde(nombre)`. |
| Daniel Mera | `IIA.buscar(String tipoArsenal)`; `AntCiberDron(piloto, bomba, coordenadas, fuente, turbo)` con `setPresentador(...)` y `buscarObjetivos()`. |
| Leonel Morales | `HormigueroServicio(Path, TipoHormiga)`: `registrar`, `autenticar`, `crearHormiga`, `listar`, `alimentar`. |

## 4. Plan de 2 días (una iteración por día)

| # | Día 1 · Iteración 1 | Pts | Día 2 · Iteración 2 | Pts |
|---|---|---|---|---|
| 1 | Primera hora: estructura y XP-01 · HU-06 Autómata | 6 | HU-14 Bomba (mañana) · Coach: revisión de PR e integración | 2 |
| 2 | XP-02 · HU-01 Autenticación y `config` | 5 | HU-09 Fuente de poder (mañana) · HU-15 `MotorAntdron` | 4 |
| 3 | XP-05 · HU-03 `Grupo##.csv` | 6 | HU-04 Lectura con loading · reporte de aceptación | 3 |
| 4 | XP-03 · HU-05 Hormigas y alimentos | 6 | HU-10 Idiomas · HU-02 Equipo · diagrama de clases | 3 |
| 5 | XP-04 · HU-08 Armas · HU-13 Docker | 6 | HU-07 IA buscar (tarde, cuando ya existen Bomba y FuentePoder) | 3 |
| 6 | XP-06 · HU-12 Persistencia | 6 | HU-11 Pantallas del hormiguero | 3 |
| | **Total** | **35** | | **18** |

El horario de cada día (planificación, reuniones de pie, bloques en pareja, aceptación y retrospectiva) está en la sección 6 de `framework-xp/docs/guia-xp.md`. Lo que no se termine el día 1 entra al plan del día 2 según la velocidad medida (`xp velocidad`).

## 5. Parejas

| Bloque | Pareja A | Pareja B | Pareja C |
|---|---|---|---|
| Día 1 · mañana | 1 – 6 | 2 – 5 | 3 – 4 |
| Día 1 · tarde | 1 – 5 | 6 – 4 | 2 – 3 |
| Día 2 · mañana | 1 – 4 | 5 – 3 | 6 – 2 |
| Día 2 · tarde | 1 – 3 | 4 – 2 | 5 – 6 |

`xp parejas` calcula la rotación con los nombres de `config/integrantes.csv`.

## 6. Reglas de entrega

1. Cada integrante sube **solo** los archivos de su sección, en una rama por historia y por pull request.
2. Antes del pull request, «Pruebas + reporte de aceptación» en VS Code debe terminar en «TODAS LAS PRUEBAS PASARON».
3. Otra pareja revisa el pull request y vuelve a ejecutar «Pruebas + reporte de aceptación» en VS Code antes de aprobarlo.
4. Cada integrante escribe **su propia cédula** en `config/integrantes.csv` (primera columna). La aplicación regenera `datos/Grupo##.csv`, la bomba y el autómata al pulsar «Recargar configuración».
