# ANTDRON · Proyecto XP (Metodologías Ágiles · EPN)

Proyecto en dos partes independientes, desarrollado con Extreme Programming:

| Parte | Carpeta | Descripción |
|---|---|---|
| 1. Framework XP | `framework-xp/` | Dicta las directrices de XP y las verifica: backlog, plan, velocidad, parejas, tarjetas CRC, pruebas por historia. |
| 2. Aplicación AntCiberDron | `antdron/` | Lo especificado en el enunciado, con pantallas **JavaFX** y dockerizada. Funciona sin el framework. |

**No usa Maven.** Solo necesita un **JDK 17 o superior** (por ejemplo, Temurin 21). La primera vez descarga JavaFX automáticamente en `.javafx/`.

## Ejecutar en Windows

En PowerShell o en CMD, desde la carpeta del repositorio clonado:

```powershell
.\antdron.cmd              # abre la aplicación JavaFX
.\antdron.cmd consola      # versión de consola del enunciado
.\antdron.cmd probar       # pruebas del framework y de la aplicación + reporte de aceptación
.\antdron.cmd xp ayuda     # comandos del framework XP
```

En Linux o macOS, use `./antdron.sh` en lugar de `.\antdron.cmd`.

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

Si faltan cédulas, la aplicación no falla: usa las que haya y muestra un aviso.

## Ejecutar con Docker

```bash
docker compose up --build          # luego abra http://localhost:6080/vnc.html
docker compose run --rm consola    # versión de consola
```

La imagen ejecuta todas las pruebas al construirse. `config/` y `datos/` se montan desde su carpeta, así que puede editar las cédulas y pulsar «Recargar configuración».

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
antdron.cmd / antdron.sh      lanzadores (Windows / Linux-macOS)
herramientas/Construir.java   construcción sin Maven: descarga JavaFX, compila, prueba y ejecuta
framework-xp/                 Parte 1: src/, pruebas/, recursos/, docs/
antdron/                      Parte 2: src/, pruebas/, recursos/ (estilos JavaFX)
config/                       equipo.properties e integrantes.csv (cédulas)
datos/                        se genera: Grupo##.csv, automata-grupo##.md, hormiguero/
xp/                           artefactos XP que revisa el framework
docker/, Dockerfile, docker-compose.yml
```
