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

Usuarios de ejemplo: el profesor `patmic` con clave `123`, y los integrantes `integrante1` a `integrante6` con clave `1234`.

## Cédulas del equipo: lo único que hay que editar

Abra `config/integrantes.csv` y escriba la cédula de cada integrante en la **primera columna** (empiezan vacías). También puede cambiar nombres, usuarios y claves. El número y el nombre del grupo están en `config/equipo.properties`.

```
cedula;nombre;usuario;clave;rol
1754265385;Integrante 1;integrante1;1234;ENTRENADOR
;Integrante 2;integrante2;1234;CLIENTE
...
```

Después pulse **«Recargar configuración»** en la aplicación (o vuelva a abrirla). El programa hace todo lo demás:

- genera `datos/Grupo##.csv` con las coordenadas de las cédulas;
- lo lee y lo muestra con su animación de carga;
- construye la bomba con el último dígito de cada cédula, su autómata, su gramática y su grafo;
- exporta `datos/automata-grupo##.md` (definición formal, gramática, Mermaid y Graphviz) para la documentación;
- prepara al AntCiberDron con la hormiga del grupo (1-2 HSoldado, 3-4 HZángano, 5 HRastreadora).

Si faltan cédulas, la aplicación no falla: usa las que haya y muestra un aviso. Los archivos se pueden editar con VS Code, el Bloc de notas o Excel (guardando como CSV con `;`).

## Ejecutar con Docker (aplicación dockerizada)

Con Docker Desktop abierto, en una terminal dentro de la carpeta del repositorio:

```powershell
docker compose up --build          # luego abra http://localhost:6080/vnc.html
docker compose run --rm consola    # versión de consola
docker compose run --rm antdron verificar   # cualquier comando del framework XP
```

La imagen compila el proyecto y ejecuta todas las pruebas al construirse: si alguna falla, no se construye. `config/` y `datos/` se montan desde su carpeta, así que puede editar las cédulas y pulsar «Recargar configuración».

## Documentación

| Documento | Contenido |
|---|---|
| [docs/division-tareas.md](docs/division-tareas.md) | Roles, reparto de historias, contratos y plan de 2 días |
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
