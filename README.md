# ANTDRON · Proyecto XP (Metodologías Ágiles · EPN)

Proyecto en dos partes independientes, desarrollado con Extreme Programming:

| Parte | Carpeta | Descripción |
|---|---|---|
| 1. Framework XP | `framework-xp/` | Dicta las directrices de XP y las verifica: backlog, plan, velocidad, parejas, tarjetas CRC y pruebas por historia. |
| 2. Aplicación AntCiberDron | `antdron/` | Lo que pide el enunciado, con pantallas **JavaFX** y dockerizada. Funciona sin el framework. |

Se trabaja en **Windows con VS Code**. No usa Maven ni ninguna herramienta de construcción: VS Code compila todo, y JavaFX 21 y el controlador de SQLite ya vienen en `lib/`.

## Requisitos (Windows)

1. **JDK 17 o superior**, por ejemplo [Temurin 21](https://adoptium.net).
2. **VS Code** con la extensión **Extension Pack for Java**. VS Code la sugiere al abrir la carpeta.
3. Para la versión dockerizada: **Docker Desktop**.

## Ejecutar en VS Code

1. **Archivo → Abrir carpeta…** y elija la carpeta raíz del repositorio, donde están `.vscode/` y `lib/`.
2. Espere a que VS Code termine de cargar el proyecto Java (abajo a la derecha). Compila solo cada vez que guarda.
3. Abra **Ejecutar y depurar** (Ctrl+Shift+D), elija una configuración y pulse **F5**:

| Configuración | Qué hace |
|---|---|
| **AntCiberDron (JavaFX)** | Abre la aplicación. |
| **AntCiberDron (consola)** | Versión de consola del enunciado, en la terminal. |
| **AntCiberDron (autómata de cédulas)** | Imprime el autómata, la gramática y el grafo de las cédulas que escriba. |
| **Pruebas + reporte de aceptación** | Ejecuta todas las pruebas y actualiza `xp/pruebas-aceptacion.md`. |
| **Framework XP (comando)** | Pide un comando del framework: `verificar`, `backlog`, `plan`, `velocidad`, `parejas 2`, `crc`, `ayuda`... |

Usuarios: el profesor `patmic` con clave `123`, y los integrantes `sjibaja`, `kmosquera`, `klugmana`, `djumbo`, `dmera` y `lmorales` con clave `1234`.

## Pestañas de la aplicación

Después de ingresar: **Equipo**, **Coordenadas ucranianas**, **Bomba y autómata**, **AntCiberDron**, **Hormiguero virtual** (registro de usuarios KGD, hormigas y alimentación, guardados en SQLite) y **Tablero XP**.

El **Tablero XP** es el dashboard de trabajo del equipo: muestra las tareas de la división por fases (`xp/tareas.csv`) en columnas *Por hacer*, *En curso* y *Hecha*, con filtros por integrante y sesión, más el avance por integrante, las 21 historias con sus pruebas y las 17 prácticas XP. Una tarea pasa sola a *Hecha* cuando su captura está en `docs/evidencias/`, y a *En curso* cuando su artefacto ya existe. Para tareas sin captura (videos, PR, comentarios en GitHub) cambie la última columna de `xp/tareas.csv` a `HECHA` y pulse **Actualizar**. El índice de evidencias para el profesor está en `docs/evidencias.md`.

## Cédulas del equipo

Las cédulas del Grupo 4 ya están en la primera columna de `config/integrantes.csv`; el número y el nombre del grupo, en `config/equipo.properties`. No las capture ni las grabe, y no edite el archivo con Excel.

```
cedula;nombre;usuario;clave;rol
<cédula>;Sebastián Jibaja;sjibaja;1234;ENTRENADOR
<cédula>;Karen Mosquera;kmosquera;1234;CLIENTE
...
```

Si cambia algo, pulse **«Recargar configuración»** en la aplicación (o vuelva a abrirla). El programa hace todo lo demás:

- genera `datos/Grupo##.csv` con las coordenadas de las cédulas;
- lo lee y lo muestra con su animación de carga;
- construye la bomba con el último dígito de cada cédula, su autómata, su gramática y su grafo;
- exporta `datos/automata-grupo##.md` (definición formal, gramática, Mermaid y Graphviz) para la documentación;
- prepara al AntCiberDron con la hormiga del grupo (1-2 HSoldado, 3-4 HZángano, 5 HRastreadora).

Si faltan cédulas, la aplicación no falla: usa las que haya y muestra un aviso. Los archivos se editan con VS Code o el Bloc de notas.

## Ejecutar con Docker (aplicación dockerizada)

Con Docker Desktop abierto, en una terminal dentro de la carpeta del repositorio:

```powershell
docker compose up --build          # luego abra http://localhost:6080/vnc.html
docker compose run --rm consola    # versión de consola
docker compose run --rm antdron verificar   # cualquier comando del framework XP
```

La imagen compila el proyecto y ejecuta todas las pruebas al construirse: si alguna falla, no se construye. `config/` y `datos/` se montan desde su carpeta, así que puede editar las cédulas y pulsar «Recargar configuración». La base SQLite del hormiguero queda en `datos/hormiguero/hormiguero.db` de su equipo, por lo que las hormigas se conservan al detener y volver a levantar el contenedor. `xp/` y `docs/` también se montan: al agregar una captura en `docs/evidencias/` o cambiar `xp/tareas.csv`, pulse **Actualizar** en el Tablero XP.

## Documentación

| Documento | Contenido |
|---|---|
| [docs/division-tareas.md](docs/division-tareas.md) | Roles, reparto de historias, contratos y plan de 2 días |
| [docs/evidencias.md](docs/evidencias.md) | Índice de evidencias por práctica XP (capturas en `docs/evidencias/`) |
| [xp/tareas.csv](xp/tareas.csv) | Tareas por fase y por integrante que muestra el Tablero XP |
| [xp/funcionalidad-minima.md](xp/funcionalidad-minima.md) | Historias imprescindibles y por qué (Cliente) |
| [framework-xp/docs/guia-xp.md](framework-xp/docs/guia-xp.md) | Directrices XP del framework |
| [framework-xp/docs/estandar-codigo.md](framework-xp/docs/estandar-codigo.md) | Estándar de código |
| [docs/diagramas/clases.md](docs/diagramas/clases.md) | Diagrama de clases |
| [docs/diagramas/casos-uso.md](docs/diagramas/casos-uso.md) | Diagrama de casos de uso |
| [docs/diagramas/grafo-automata.md](docs/diagramas/grafo-automata.md) | Grafo con gramática del autómata |
| [docs/diagramas/modelo-er.md](docs/diagramas/modelo-er.md) | Modelo entidad-relación |
| [docs/diagramas/arquitectura.md](docs/diagramas/arquitectura.md) | Diagrama de arquitectura |
| [xp/](xp/) | Backlog, plan de entregas, reuniones, metáfora, tarjetas CRC, spikes y reporte de aceptación |

## Estructura

```
.vscode/        carpetas de fuentes, JavaFX y configuraciones de ejecución para VS Code
lib/            JavaFX 21.0.5 para Windows (base, graphics, controls) y sqlite-jdbc 3.54.1.0
framework-xp/   Parte 1: src/, pruebas/, docs/
antdron/        Parte 2: src/, pruebas/
config/         equipo.properties e integrantes.csv (cédulas)
datos/          se genera: Grupo##.csv, automata-grupo##.md, hormiguero/hormiguero.db (SQLite)
xp/             artefactos XP que revisa el framework
docker/, Dockerfile, docker-compose.yml
```
