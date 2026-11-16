# Proceso XP del proyecto

El framework `xp-framework` cubre las cuatro fases de XP del enunciado. Cada práctica tiene un artefacto en el repositorio y, cuando aplica, un componente del framework que la automatiza.

| Fase | Práctica | Artefacto en el repositorio | Soporte del framework |
|---|---|---|---|
| **I. Planificación** | Historias de usuario | `xp/historias.csv`, [historias-usuario.md](historias-usuario.md) | `HistoriaUsuario`, `LectorBacklog`, `xp backlog` |
| | Plan de entregas | [historias-usuario.md](historias-usuario.md#plan-de-entregas) | `PlanEntregas`, `xp plan [--proponer N]` |
| | Velocidad del proyecto | columna *puntos* + estado | `Velocidad` (clima de ayer y promedio), `xp velocidad` |
| | Iteraciones | columna *iteracion* (1 framework, 2-4 aplicación) | `Iteracion` (avance %) |
| | Rotaciones | [division-trabajo.md](division-trabajo.md#parejas-por-iteración-rotación-del-framework) | `RotacionParejas`, `xp parejas` |
| | Reuniones | [estandar-codigo.md](estandar-codigo.md#5-reuniones-y-comandos-del-framework) | comandos de la CLI en cada reunión |
| **II. Diseño** | Metáfora del sistema | `config/equipo.properties` (`metafora`) | `Equipo.getMetafora()`, `xp equipo` |
| | Tarjetas CRC | [tarjetas-crc.md](tarjetas-crc.md) | `@TarjetaCRC`, `GeneradorCRC`, `xp crc` |
| | Soluciones puntuales (spikes) | Prueba del servidor HTTP del JDK frente a frameworks web y de SQLite embebido antes de la HU-11 | — |
| | Funcionalidad mínima | Sin dependencias innecesarias: JDK + JUnit + SQLite | — |
| | Reciclaje (refactorización) | Pruebas en verde antes y después de cada cambio | `mvn verify` |
| **III. Desarrollo** | Disponibilidad del cliente | Integrante 2 (Cliente) prioriza y acepta historias | `@Historia` + reporte |
| | Unidad de pruebas | `*/src/test/java` (JUnit 5) | `TrazabilidadExtension` |
| | Programación por parejas | [division-trabajo.md](division-trabajo.md) | `RotacionParejas` |
| | Integración | `.github/workflows/ci.yml` (pruebas + Docker en cada push) | `xp aceptacion` en el resumen de la CI |
| **IV. Pruebas** | Implantación | `Dockerfile`, `docker-compose.yml` | — |
| | Pruebas de aceptación | `target/xp-aceptacion/resultados.csv` | `ReporteAceptacion`, `xp aceptacion` |

## Metáfora

> *Hormiguero de guerra*: cada módulo es una hormiga especializada que coopera para que el AntCiberDron cumpla su misión. El **SistemaRuso** es la hormiga exploradora que trae las coordenadas, la **Bomba** es la mandíbula que solo muerde lo que reconoce su autómata, la **IA** es la antena que busca el arsenal y el **hormiguero virtual** es la colonia de cada usuario de la KGD.

## Cómo se adapta el framework a la parte 2

- La aplicación lee el equipo con `LectorEquipo` del framework: las mismas cédulas generan `Grupo##.csv`, definen el autómata y autentican a los integrantes.
- Todas las pruebas de la aplicación están anotadas con `@Historia`, de modo que `xp aceptacion` muestra el estado de cada historia del backlog.
- Las clases clave llevan `@TarjetaCRC` y [tarjetas-crc.md](tarjetas-crc.md) se genera desde el código.
- Para otro proyecto basta con cambiar `config/` y `xp/historias.csv`: el framework no depende de la aplicación.
