# Soluciones puntuales (spikes)

**Responsable:** Daniel Mera (Consultor) · **Práctica XP:** Soluciones puntuales (spikes) · **Defienden en S2:** Sebastián Jibaja (spikes 1 y 5), Daniel Mera (2), Leonel Morales (3) y Kened Lugmaña (4)

Un **spike** es una investigación corta para reducir un riesgo técnico antes de estimar o programar una historia. Cada uno responde una pregunta, tiene un tiempo máximo y termina con una **decisión**. Aquí se resumen los cinco spikes del proyecto.

| Spike | Historia | Responsable | Pregunta | Decisión |
|---|---|---|---|---|
| 1 | HU-13, XP-05 | Sebastián Jibaja | ¿Se puede usar JavaFX sin Maven? | Sí: VS Code compila con los `.jar` de JavaFX para Windows en `lib/`. |
| 2 | HU-13 | Daniel Mera | ¿Se puede ver una interfaz gráfica dentro de Docker? | Sí: Xvfb + x11vnc + noVNC en el puerto 6080. |
| 3 | HU-12 | Leonel Morales | ¿SQLite o CSV para el hormiguero? | Primero CSV; revisado: SQLite (`datos/hormiguero/hormiguero.db`) con el mismo modelo ER. |
| 4 | XP-05 | Kened Lugmaña | ¿JUnit o un ejecutor de pruebas propio? | Ejecutor propio con `@Prueba` y `@Historia`. |
| 5 | HU-06 | Sebastián Jibaja | ¿Cómo construir el AFD mínimo? | Subconjuntos + minimización de Moore. |

---

## Spike 1 · JavaFX sin Maven

- **Historias:** HU-13 (aplicación dockerizada) y XP-05 (pruebas). **Tiempo máximo:** 1 hora.
- **Pregunta:** el proyecto no usa Maven y se trabaja en Windows con VS Code. ¿Cómo se compila y ejecuta una aplicación JavaFX con solo un JDK?
- **Intentos:**
  - Instalar el SDK de JavaFX en cada computadora y pasar `--module-path`: cada integrante tendría una ruta distinta.
  - Una herramienta de construcción propia que descargue JavaFX: funciona, pero es código extra que el enunciado no pide.
- **Solución:** proyecto Java de VS Code sin herramienta de construcción (`.vscode/settings.json`):
  1. las cuatro carpetas de fuentes (`framework-xp/src`, `framework-xp/pruebas`, `antdron/src`, `antdron/pruebas`) se compilan juntas en `bin/`;
  2. `lib/` trae `javafx-base`, `javafx-graphics` y `javafx-controls` 21.0.5 para Windows (las DLL vienen dentro de los `.jar`);
  3. `ec.edu.epn.antdron.App` no hereda de `Application`, así JavaFX arranca desde el classpath sin `--module-path` (Java solo muestra el aviso «Unsupported JavaFX configuration», que no afecta).
- **Decisión:** `.vscode/launch.json` trae las configuraciones «AntCiberDron (JavaFX)», «AntCiberDron (consola)», «AntCiberDron (autómata de cédulas)», «Pruebas + reporte de aceptación» y «Framework XP (comando)».
- **Consecuencias:** solo se instala un JDK 17+ y VS Code con el Extension Pack for Java; no hace falta internet. Como todo se compila junto, `IndependenciaTest` comprueba que `antdron/src` no importe el framework, y Docker compila además la aplicación sola.

## Spike 2 · Interfaz gráfica en Docker con Xvfb y noVNC

- **Historia:** HU-13. **Tiempo máximo:** 1 hora.
- **Pregunta:** el profesor debe probar la aplicación «sin instalar Java». ¿Cómo se muestra una ventana JavaFX desde un contenedor?
- **Intentos:**
  - Reenviar un servidor X al anfitrión: depende del sistema operativo de cada persona.
  - Una pantalla virtual dentro del contenedor, visible desde el navegador.
- **Solución:** la imagen parte de `eclipse-temurin:21-jdk` e instala `xvfb`, `x11vnc`, `novnc` y `websockify` más las bibliotecas gráficas que necesita JavaFX (GTK, `libgl1`). `docker/iniciar.sh`:
  1. arranca **Xvfb** (pantalla virtual `:99`, 1280×800);
  2. arranca **x11vnc** sobre esa pantalla;
  3. arranca **websockify/noVNC** en el puerto **6080**;
  4. ejecuta la aplicación y la vuelve a abrir si la ventana se cierra.
- **Detalle técnico:** sin tarjeta gráfica se usa el renderizado por software de JavaFX (`-Dprism.order=sw`). La imagen descarga los `.jar` de JavaFX para Linux, porque los de `lib/` son para Windows.
- **Decisión:** `docker compose up --build` y abrir `http://localhost:6080/vnc.html`. La consola funciona con `docker compose run --rm consola` y las pruebas con `docker compose run --rm antdron probar`.
- **Consecuencias:** `config/` y `datos/` se montan como volúmenes, así que el equipo edita las cédulas en su máquina y pulsa «Recargar configuración» dentro de la ventana. La imagen ejecuta las pruebas al construirse.

## Spike 3 · Persistencia: CSV o SQLite

- **Historia:** HU-12. **Tiempo máximo:** 1 hora.
- **Pregunta:** el enunciado pide guardar los hormigueros. ¿Se usa una base de datos?
- **Intentos:**
  - **SQLite:** necesita el controlador JDBC (un `.jar` externo). Sin Maven habría que descargarlo, versionarlo y agregarlo al classpath en la compilación, en Docker y en las pruebas.
  - **CSV:** solo `java.nio.file`; cada tabla es un archivo con cabecera.
- **Comparación:**

| Criterio | SQLite | CSV |
|---|---|---|
| Dependencias | Controlador JDBC externo | Ninguna |
| Consultas | SQL | Recorrer filas en Java (pocos datos) |
| Transacciones y concurrencia | Sí | Solo dentro del proceso (`synchronized`) |
| Ver y corregir los datos | Con una herramienta | Con cualquier editor o Excel |
| Pruebas | Base temporal | Carpeta temporal |

- **Decisión inicial:** **CSV**, con el mismo modelo entidad-relación (`docs/diagramas/modelo-er.md`): `usuarios`, `hormigueros`, `hormigas` y `alimentaciones` en `datos/hormiguero/`. Las claves se guardan con sal y SHA-256.
- **Revisión (decisión vigente):** **SQLite**. Al revisar HU-12 se comprobó que las hormigas no se guardaban en una base de datos, así que se agregó el controlador `lib/sqlite-jdbc-3.54.1.0.jar` (un solo `.jar`, sin dependencias obligatorias, con SQLite para Windows, Linux y Mac) al classpath de VS Code, Docker y las pruebas. Las tablas `usuario`, `hormiguero`, `hormiga` y `alimentacion` viven en `datos/hormiguero/hormiguero.db`; el esquema está en `persistencia/esquema.sql`.
- **Consecuencias:** la base valida claves foráneas, valores permitidos y nombres de usuario únicos; registrar y alimentar se guardan en una transacción. Los CSV de la versión anterior se importan una vez de forma automática. Para ver los datos se usa una herramienta como DB Browser for SQLite.

## Spike 4 · Ejecutor de pruebas propio o JUnit

- **Historia:** XP-05. **Tiempo máximo:** 1 hora.
- **Pregunta:** sin Maven, ¿cómo se ejecutan las pruebas unitarias y se sabe qué historias están aceptadas?
- **Intentos:**
  - **JUnit:** habría que descargar sus `.jar` y la consola de ejecución, y no sabe qué historia cubre cada prueba.
  - **Ejecutor propio:** reflexión de Java sobre las clases de prueba.
- **Solución:** el framework incluye:
  - anotaciones `@Prueba`, `@Historia("HU-nn")`, `@AntesDeCada` y `@DespuesDeCada`;
  - `Afirmar` (`igual`, `verdadero`, `falso`, `lanza`, `contiene`, ...);
  - `EjecutorPruebas`, que busca las clases de prueba, las ejecuta y guarda `build/pruebas/resultados.csv`;
  - `ReporteAceptacion`, que cruza ese resultado con el backlog y marca cada historia como **ACEPTADA**, **RECHAZADA** o **SIN_PRUEBAS** (`xp aceptacion`).
- **Decisión:** ejecutor propio (el framework lo pide de todas formas: «Pruebas de aceptación» es una directriz XP). «Pruebas + reporte de aceptación» en VS Code lo ejecuta para el framework y para la aplicación.
- **Consecuencias:** hay menos funciones que en JUnit (no hay pruebas parametrizadas ni ejecución paralela), pero son suficientes y el reporte por historia es nativo.

## Spike 5 · AFD mínimo con subconjuntos y Moore

- **Historia:** HU-06. **Tiempo máximo:** 2 horas.
- **Pregunta:** dado el conjunto de lenguajes de las cédulas del equipo (por ejemplo `{abcdt+, ab*}`), ¿cómo se obtiene el autómata, la gramática y el grafo?
- **Intentos:**
  - Construirlo a mano para cada combinación de dígitos: son 10 lenguajes y miles de combinaciones posibles.
  - Un algoritmo general: expresión regular → AFN-ε → AFD → AFD mínimo.
- **Solución:** `ConstructorAutomata`:
  1. Cada expresión (`x`, `x*`, `x+` concatenados) se traduce a un AFN-ε.
  2. La unión se hace con un estado inicial nuevo y transiciones ε.
  3. **Subconjuntos:** se calcula el AFD con clausuras ε.
  4. **Moore:** se refinan las particiones de estados equivalentes, se elimina el sumidero y se numeran los estados `q0..qn` en orden BFS.
  5. `Gramatica` deriva la gramática regular por la derecha y `ExportadorGrafo` produce el Mermaid y el DOT.
- **Verificación:** `AutomataTest` compara cada autómata y la unión con la expresión regular equivalente en todas las palabras de longitud 0 a 7 sobre `{a,b,c,d,t}` (97 656 palabras). El ejemplo del enunciado (cédulas terminadas en 5 y 9) da `L = {abcdt+, ab*}` con 7 estados.
- **Decisión:** se usa el algoritmo general. Esto permite que al escribir las cédulas en `config/integrantes.csv` el programa regenere el autómata, la gramática y el grafo (`datos/automata-grupo##.md`) sin editar código.
