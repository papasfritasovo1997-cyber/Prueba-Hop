# División de tareas · Proyecto XP ANTDRON (6 integrantes)

El proyecto tiene **dos partes independientes**, cada una en su propia carpeta y con su propio `pom.xml`:

| Parte | Carpeta | Qué es | Relación |
|---|---|---|---|
| **1. Framework XP** | `framework-xp/` | Dicta las directrices de XP (fases, prácticas, roles, reglas, DoD) y trae una CLI para planificar, medir y **verificar** que se cumplen. | No sabe nada de la aplicación. |
| **2. Aplicación AntCiberDron** | `antdron/` | Lo que pide el enunciado: login, `Grupo##.csv`, autómata, hormigas, IA, pantallas web, Docker. | **Sigue** las directrices, pero no importa ni una clase del framework: compila y funciona sin él. |

La aplicación cumple las directrices produciendo artefactos (historias, tarjetas CRC, pruebas con `@Tag("HU-nn")`, CI, Dockerfile), y el framework los revisa con `xp verificar`. Las reglas completas están en `framework-xp/docs/guia-xp.md` (Integrante 1).

## 1. Resumen por integrante

Cada integrante tiene **un rol XP**, **una historia del framework**, **historias de la aplicación** y **un diagrama o documento**. La carga está equilibrada en **8 o 9 puntos** por persona. El proyecto dura **2 días**: 1 punto ≈ 1 hora ideal de trabajo en pareja.

| # | Nombre | Rol XP | Parte 1 · Framework | Parte 2 · Aplicación | Diagrama / documento | Pts |
|---|---|---|---|---|---|---|
| 1 | | Entrenador (Coach) | XP-01 Núcleo y directrices (3) | HU-06 Autómata (3) · HU-14 Bomba (2) | Grafo con gramática del autómata | 8 |
| 2 | | Cliente | XP-02 Historias de usuario (2) | HU-01 Autenticación (3) · HU-02 Equipo (1) · HU-09 Vuelo y fuente de poder (2) | Diagrama de casos de uso | 8 |
| 3 | | Encargado de pruebas | XP-05 Pruebas de aceptación (3) | HU-03 Generar `Grupo##.csv` (3) · HU-04 Leer con loading (3) | Reporte de pruebas de aceptación | 9 |
| 4 | | Encargado de seguimiento | XP-03 Plan de entregas y velocidad (3) | HU-05 Hormigas y alimentos (3) · HU-10 Idiomas (2) | Diagrama de clases | 8 |
| 5 | | Consultor | XP-04 Diseño: metáfora, CRC, spikes (2) | HU-07 IA buscar (3) · HU-08 Armas (2) · HU-13 Docker (2) | Diagrama de arquitectura | 9 |
| 6 | | Gestor | XP-06 Equipo, parejas, reuniones e integración continua (3) | HU-11 Pantallas del hormiguero (3) · HU-12 Persistencia (3) | Modelo entidad-relación | 9 |

Total: 16 puntos de framework + 35 de aplicación = 51 puntos.

## 2. Parte 1 · Framework XP (`framework-xp/`)

Paquete base `ec.edu.epn.xp`. El núcleo descubre los comandos de cada módulo con `ServiceLoader`: cada integrante implementa `ec.edu.epn.xp.ComandoXP` y agrega **una línea** con su clase en `framework-xp/src/main/resources/META-INF/services/ec.edu.epn.xp.ComandoXP`. Así nadie toca el código de otro.

| Historia | Responsable | Paquete / archivos | Comandos | Artefacto que produce para el proyecto |
|---|---|---|---|---|
| **XP-01** Núcleo y directrices | Integrante 1 | `ec.edu.epn.xp` (`ComandoXP`, `ContextoXP`, `XpCli`), `ec.edu.epn.xp.directrices` (`Fase`, `RolXP`, `Practica`), `docs/guia-xp.md`, `docs/estandar-codigo.md`, `pom.xml` de ambas partes, `xp.properties` | `ayuda`, `directrices`, `verificar` | Guía de directrices y estándar de código |
| **XP-02** Historias de usuario | Integrante 2 | `ec.edu.epn.xp.planificacion` (`HistoriaUsuario`, `Prioridad`, `EstadoHistoria`, `LectorBacklog`) | `backlog` | `xp/historias.csv` con todas las historias XP-nn y HU-nn |
| **XP-03** Plan de entregas y velocidad | Integrante 4 | `ec.edu.epn.xp.seguimiento` (`Iteracion`, `PlanEntregas`, `Velocidad`) | `plan [--proponer N]`, `velocidad` | `xp/plan-entregas.md` |
| **XP-04** Diseño | Integrante 5 | `ec.edu.epn.xp.diseno` (lectura de tarjetas CRC) | `crc <dir-fuentes>`: lista las clases sin tarjeta | `xp/metafora.md`, `xp/tarjetas-crc.md`, `xp/spikes.md` |
| **XP-05** Pruebas de aceptación | Integrante 3 | `ec.edu.epn.xp.pruebas` (lee `@Tag` de las pruebas y `target/surefire-reports`) | `aceptacion` | `xp/pruebas-aceptacion.md` |
| **XP-06** Desarrollo | Integrante 6 | `ec.edu.epn.xp.desarrollo` (`Integrante`, `Equipo`, `LectorEquipo`, `RotacionParejas`) | `equipo`, `parejas [ronda]` | `xp/equipo.csv`, `xp/reuniones.md`, `.github/workflows/ci.yml` |

**Contratos del framework**

- `ComandoXP`: `String nombre()`, `String descripcion()`, `int ejecutar(List<String> argumentos, ContextoXP contexto)`. Devolver 0 si todo bien y 1 si el uso es incorrecto; ante datos inválidos, lanzar `IllegalArgumentException` con un mensaje claro.
- Las rutas se leen con `contexto.ruta(clave, porDefecto)` o `contexto.artefacto(Practica.X)`; nunca con rutas fijas.
- Formato mínimo de `xp/historias.csv` (lo usan XP-02, XP-03 y XP-05): `id;titulo;como;quiero;para;prioridad;puntos;iteracion;estado;responsable;criterios`.
- Formato mínimo de `xp/equipo.csv` (lo usan XP-06 y `verificar`): `cedula;nombre;usuario;rol`, con `rol` tomado de `RolXP`.
- XP-03 lee las historias con `LectorBacklog.leer(Path)` de XP-02. XP-05 puede usarlo para mostrar títulos.
- La CI de XP-06 compila y prueba **por separado** `framework-xp` y `antdron`, y luego ejecuta `xp verificar`.

## 3. Parte 2 · Aplicación AntCiberDron (`antdron/`)

Paquete base `ec.edu.epn.antdron`. Java 17, JUnit 5. Cada clase de prueba lleva `@Tag("HU-nn")`, que es JUnit estándar y no depende del framework.

| Historia | Responsable | Paquete / archivos | Criterios de aceptación (del enunciado) |
|---|---|---|---|
| **HU-01** Autenticación | Integrante 2 | `auth` (`Autenticador`), `App` (consola), `config/integrantes.csv` | Usuario y contraseña de los integrantes y del profesor (`patmic`, `123`); máximo 3 intentos |
| **HU-02** Presentar equipo | Integrante 2 | `auth` (`PresentadorEquipo`) | `[+] TEAM [N]: nombre` y `Alumno i: cédula \| nombre` |
| **HU-03** Generar `Grupo##.csv` | Integrante 3 | `sistema` (`GeneradorCsv`, `CoordenadaUK`, `DiaSemana`), `data/Grupo##.csv` | Una coordenada por cada dígito de las cédulas, con el horario de la tabla OTAN y una palabra w del lenguaje de la coordenada en la última columna |
| **HU-04** Leer coordenadas con loading | Integrante 3 | `sistema` (`SistemaRuso`, `Loading`) | Por cada línea, un loading `\|/-` hasta 100%; tabla Loading, Geoposición, días y Tipo Arsenal |
| **HU-05** Hormigas y alimentos | Integrante 4 | `pkAntCiberDronContract.IHormiga`, `hormiga`, `alimento` | Todo TIPO_HORMIGA nace como HLarva, que vive con néctar; si come su ALIMENTO se transforma; el TIPO_HORMIGA vive solo con su ALIMENTO (1-2 HSoldado/Carnívoro, 3-4 HZángano/Omnívoro, 5 HRastreadora/Herbívoro) |
| **HU-06** Autómata | Integrante 1 | `automata` | Autómata con el último dígito de cada cédula: AFD mínimo, definición formal, gramática y grafo |
| **HU-07** IA buscar arsenal | Integrante 5 | `pkAntCiberDronContract.IIA`, `antciberdron` (`AntCiberDron`) | `buscar(tipoArsenal)` devuelve true o false según el autómata y presenta Geoposición, Tipo Arsenal y Acción |
| **HU-08** Armas | Integrante 5 | `antciberdron` (`Arma`, `Metralleta`, `Laser`) | Metralleta en la pata delantera derecha; láser en la izquierda, con potencia según la energía de la fuente de poder |
| **HU-09** Vuelo y fuente de poder | Integrante 2 | `antciberdron` (`FuentePoder`, `TurboReactor`) | Turbo reactor en la espalda que vuela con energía; fuente recargable y reemplazable bajo la asistencia de la IIA |
| **HU-10** Idiomas | Integrante 4 | `antciberdron` (`ModuloIdiomas`, `ExpertoParlante`, `Idioma`) | Aprende inglés y español; solo expertos parlantes entrenan léxico, gramática y fonética |
| **HU-11** Pantallas del hormiguero | Integrante 6 | `web` | Los usuarios de la KGD tienen su hormiguero virtual y crean sus hormigas |
| **HU-12** Persistencia | Integrante 6 | `persistencia` | El hormiguero de cada usuario se guarda; claves con hash |
| **HU-13** Aplicación dockerizada | Integrante 5 | `antdron/Dockerfile`, `docker-compose.yml` (raíz) | La aplicación se construye y ejecuta con Docker |
| **HU-14** Bomba | Integrante 1 | `bomba` | La bomba integra el autómata y explota solo en coordenadas objetivo con arsenal del lenguaje (destrucción exclusiva) |

**Contratos de la aplicación.** Cada responsable expone exactamente estas firmas, para que los demás programen contra ellas desde el primer día.

| Responsable | Contrato |
|---|---|
| Integrante 1 | `bomba.Bomba(Collection<String> cedulas)`: `boolean evaluar(String tipoArsenal)`, `boolean esObjetivo(String geoposicion)`, `boolean debeExplotar(String geoposicion, String tipoArsenal)`, `String lenguaje()`, `List<String> coordenadasObjetivo()`. `automata.LenguajesOTAN`: `expresion(int digito)`, `pertenece(int digito, String w)`, `digito("Coord-05")`. |
| Integrante 2 | `antciberdron.FuentePoder(double capacidad)`: `consumir(double)`, `recargar()`, `double porcentaje()`, `double getEnergia()`. `antciberdron.TurboReactor`: `String volar(int minutos, FuentePoder fuente)`. `App.main` ejecuta el flujo de consola. |
| Integrante 3 | `sistema.SistemaRuso`: `List<CoordenadaUK> readCoord(String fileName) throws IOException`. `sistema.CoordenadaUK`: `String getGeoposicion()`, `String getTipoArsenal()`, `String getHorario(DiaSemana dia)`. |
| Integrante 4 | `pkAntCiberDronContract.IHormiga`: `boolean comer(Alimento alimento)`. `hormiga.Hormiga` (abstracta; `getId()`, `getSexo()`, `getEstado()`, `getTipo()`, `estaViva()`, `getExtremidades()`). `hormiga.HLarva(TipoHormiga destino)` con `Optional<Hormiga> getEvolucion()`. `hormiga.TipoHormiga.paraGrupo(int grupo)`. `alimento.Alimento` (abstracta) y `ANectar`, `ACarnivoro`, `AOmnivoro`, `AHerbivoro`. |
| Integrante 5 | `pkAntCiberDronContract.IIA`: `boolean buscar(String tipoArsenal)`. `antciberdron.AntCiberDron(Hormiga piloto, Bomba bomba, List<CoordenadaUK> coordenadas, FuentePoder fuente, TurboReactor turbo)` implementa `IIA` y decide con `bomba.debeExplotar(c.getGeoposicion(), c.getTipoArsenal())`. |
| Integrante 6 | `persistencia.HormigueroServicio`: `registrar`, `autenticar`, `crearHormiga`, `alimentar`, `listar`; aplica las reglas de `Hormiga.comer` de HU-05. `web` atiende en el puerto `PORT` (8080 por defecto). |

## 4. Plan de iteraciones (2 días)

Cada día es una iteración. Una jornada tiene unas 6 horas de desarrollo: el día 1 cada integrante compromete hasta 6 puntos y el día 2 deja tiempo para integrar, documentar y aceptar. El horario de cada día está en `framework-xp/docs/guia-xp.md`, sección 6.

| # | Día 1 · Iteración 1: framework y base de la app | Pts | Día 2 · Iteración 2: AntCiberDron, integración y entrega | Pts |
|---|---|---|---|---|
| 1 | **Primera hora:** estructura de ambas partes y XP-01 · HU-06 Autómata | 6 | HU-14 Bomba (mañana) · Coach: revisión de PRs e integración · grafo con todas las cédulas | 2 |
| 2 | XP-02 · HU-01 Autenticación · HU-02 Equipo | 6 | HU-09 Fuente de poder (mañana) · integración de `App` · casos de uso | 2 |
| 3 | XP-05 · HU-03 `Grupo##.csv` (incluye `CoordenadaUK`) | 6 | HU-04 Lectura con loading · reporte de aceptación | 3 |
| 4 | XP-03 · HU-05 Hormigas y alimentos | 6 | HU-10 Idiomas · diagrama de clases | 2 |
| 5 | XP-04 · HU-13 Dockerfile y compose | 4 | HU-08 Armas · HU-07 IA buscar (tarde) · arquitectura | 5 |
| 6 | XP-06 · HU-12 Persistencia | 6 | HU-11 Pantallas del hormiguero · modelo ER | 3 |
| | **Total** | **34** | | **17** |

**Orden de dependencias del día 2:** las piezas que otros usan se hacen por la mañana (Bomba, FuentePoder, `SistemaRuso`). HU-07 se integra por la tarde, cuando ya están Bomba, `CoordenadaUK`, `Hormiga` y `FuentePoder`. Mientras tanto, todos programan contra las firmas de la sección 3.

**Día 1, primera hora:** el Integrante 1 sube la estructura (`pom.xml` de ambas partes, `xp.properties` y la guía) y los demás empiezan sobre ella. La velocidad medida al cierre del día 1 (`xp velocidad`) decide qué entra el día 2; lo que no quepa se negocia con el Cliente.

## 5. Parejas (programación por parejas)

Cuatro bloques de trabajo (mañana y tarde de cada día), con la rotación del método del círculo. El comando `xp parejas` (XP-06) la calcula.

| Bloque | Pareja A | Pareja B | Pareja C |
|---|---|---|---|
| Día 1 · mañana | 1 – 6 | 2 – 5 | 3 – 4 |
| Día 1 · tarde | 1 – 5 | 6 – 4 | 2 – 3 |
| Día 2 · mañana | 1 – 4 | 5 – 3 | 6 – 2 |
| Día 2 · tarde | 1 – 3 | 4 – 2 | 5 – 6 |

En cada pareja se avanzan las historias de los dos integrantes; el dueño de la historia conduce y el otro navega.

## 6. Reglas de entrega

1. Cada integrante sube **solo** los archivos de su sección, en una rama por historia (`hu-06-automata`, `xp-03-plan`) y por pull request.
2. Antes del pull request: `mvn verify` en la parte afectada (`framework-xp/` o `antdron/`) debe pasar.
3. Otra pareja revisa el pull request; la CI debe quedar en verde.
4. El Cliente (Integrante 2) marca la historia como `TERMINADA` en `xp/historias.csv` cuando `xp aceptacion` la muestra aceptada.
5. Si un contrato de la sección 3 debe cambiar, se acuerda en la reunión diaria y lo actualiza el dueño del contrato.
