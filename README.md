# ANTDRON · Proyecto XP (Metodologías Ágiles · EPN)

Proyecto en dos partes construido con **Extreme Programming**:

1. **`xp-framework/`**: framework que facilita trabajar con XP en sus cuatro fases (planificación, diseño, desarrollo y pruebas): backlog de historias, plan de entregas, velocidad, rotación de parejas, tarjetas CRC, trazabilidad de pruebas de aceptación por historia y una CLI.
2. **`antdron/`**: la aplicación del enunciado (AntCiberDron), construida sobre el framework y **dockerizada**.

| Documento | Contenido |
|---|---|
| [docs/xp/division-trabajo.md](docs/xp/division-trabajo.md) | Roles XP y reparto equitativo del trabajo entre los 6 integrantes |
| [docs/xp/proceso-xp.md](docs/xp/proceso-xp.md) | Prácticas XP, metáfora y cómo las soporta el framework |
| [docs/xp/historias-usuario.md](docs/xp/historias-usuario.md) | Historias de usuario y plan de entregas |
| [docs/xp/tarjetas-crc.md](docs/xp/tarjetas-crc.md) | Tarjetas CRC (generadas desde el código) |
| [docs/xp/estandar-codigo.md](docs/xp/estandar-codigo.md) | Estándar de código y definición de terminado |
| [docs/diagramas/clases.md](docs/diagramas/clases.md) | Diagrama de clases |
| [docs/diagramas/casos-uso.md](docs/diagramas/casos-uso.md) | Diagrama de casos de uso |
| [docs/diagramas/grafo-automata.md](docs/diagramas/grafo-automata.md) | Grafo con gramática del autómata |
| [docs/diagramas/modelo-er.md](docs/diagramas/modelo-er.md) | Modelo entidad-relación |
| [docs/diagramas/arquitectura.md](docs/diagramas/arquitectura.md) | Diagrama de arquitectura |

## Configurar el equipo (obligatorio antes de entregar)

Los datos de `config/` son **de ejemplo**. Reemplácelos por los reales:

- `config/equipo.properties`: `grupo` (define `Grupo##.csv` y el TIPO_HORMIGA: grupos 1-2 HSoldado, 3-4 HZángano, 5 HRastreadora), `nombre` y `metafora`.
- `config/integrantes.csv`: `cedula;nombre;usuario;claveSha256;rol`. El hash de una clave se obtiene con `java -jar antdron/target/antdron.jar hash MI_CLAVE`.

Luego regenere el archivo de coordenadas y el autómata:

```bash
java -jar antdron/target/antdron.jar generar-csv   # data/Grupo##.csv
java -jar antdron/target/antdron.jar automata      # definición formal, gramática y grafo
```

Credenciales de ejemplo: integrantes `integrante1` … `integrante6` con clave `1234`; profesor `patmic` / `123`.

## Ejecutar con Docker

```bash
docker compose run --rm consola        # login → equipo → coordenadas con loading → bomba → AntCiberDron
docker compose up --build web          # hormiguero virtual en http://localhost:8080
```

La construcción de la imagen ejecuta todas las pruebas; si alguna falla, la imagen no se crea.

## Ejecutar sin Docker (Java 17 + Maven)

```bash
mvn verify                                              # compila y prueba framework y aplicación
java -jar antdron/target/antdron.jar                    # consola
java -jar antdron/target/antdron.jar web                # pantallas web (puerto PORT, 8080 por defecto)
```

## CLI del framework XP

```bash
XP="java -cp antdron/target/antdron.jar ec.edu.epn.xp.cli.XpCli"
$XP equipo          # integrantes, roles y metáfora
$XP backlog         # historias de usuario
$XP plan            # plan de entregas por iteración (--proponer N para planificar con velocidad N)
$XP velocidad       # velocidad del proyecto
$XP parejas 2       # parejas de la iteración 2
$XP aceptacion      # estado de aceptación de cada historia (después de mvn verify)
```

## Estructura

```
xp-framework/   planificacion · diseno · desarrollo · pruebas · cli
antdron/        pkAntCiberDronContract · hormiga · alimento · sistema · automata · bomba
                antciberdron · auth · persistencia · web · App
config/         datos del equipo        xp/      backlog (historias.csv)
data/           Grupo##.csv             docs/    proceso XP y diagramas
```
