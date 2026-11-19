# Plan de entregas · 2 días

**Responsable:** David Jumbo (Encargado de seguimiento) · **Práctica XP:** Plan de entregas, iteraciones y velocidad

El proyecto dura **dos días** y cada día es una iteración: **Día 1 = Iteración 1**, **Día 2 = Iteración 2**. Las historias, sus puntos, prioridades y responsables salen de `xp/historias.csv`; este documento solo las organiza. Para ver siempre los datos actualizados:

```text
xp ...klog
xp ...n
xp ...ocidad
```

## Estimación y velocidad

- **1 punto ≈ 1 hora ideal de trabajo en pareja.** «Ideal» significa sin interrupciones; los imprevistos se ven en la velocidad real.
- **Velocidad** = puntos de las historias terminadas en una iteración. La velocidad del Día 1 («clima de ayer») es la que se usa para planificar el Día 2.
- Las historias solo cuentan como terminadas cuando su estado en `xp/historias.csv` es `TERMINADA` (criterios cumplidos y pruebas pasando; ver `xp/pruebas-aceptacion.md`).
- Si al cierre del Día 1 la velocidad es menor que lo planeado, `xp plan --proponer N` (N = velocidad real) reparte de nuevo las historias pendientes por prioridad, sin pasarse de N puntos por día.

## Resumen por iteración

| Iteración | Día | Historias | Puntos | Enfoque |
|---|---|---|---:|---|
| 1 | Día 1 | XP-01 a XP-06, HU-01, HU-03, HU-05, HU-06, HU-08, HU-12, HU-13 | 35 | Framework XP, configuración, dominio base, autómata, persistencia y Docker. |
| 2 | Día 2 | HU-02, HU-04, HU-07, HU-09, HU-10, HU-11, HU-14, HU-15 | 18 | Coordenadas con loading, IA, bomba, vuelo, idiomas, hormiguero visible y configuración por archivo. |
| **Total** | | **21 historias** | **53** | 6 del framework (16 puntos) y 15 de la aplicación (37 puntos). |

## Iteración 1 · Día 1 (35 puntos)

| # | Historia | Responsable | Rol XP | Prioridad | Puntos |
|---|---|---|---|---|---:|
| XP-01 | Núcleo y directrices XP | Sebastián Jibaja | Entrenador | ALTA | 3 |
| XP-02 | Historias de usuario | Karen Mosquera | Cliente | ALTA | 2 |
| XP-03 | Plan de entregas y velocidad | David Jumbo | Encargado de seguimiento | ALTA | 3 |
| XP-04 | Diseño: metáfora y tarjetas CRC | Daniel Mera | Consultor | MEDIA | 2 |
| XP-05 | Pruebas unitarias y de aceptación | Kened Lugmaña | Encargado de pruebas | ALTA | 3 |
| XP-06 | Equipo, parejas e integración continua | Leonel Morales | Gestor | ALTA | 3 |
| HU-01 | Autenticación | Karen Mosquera | Cliente | ALTA | 3 |
| HU-03 | Generar `Grupo##.csv` | Kened Lugmaña | Encargado de pruebas | ALTA | 3 |
| HU-05 | Hormigas y alimentos | David Jumbo | Encargado de seguimiento | ALTA | 3 |
| HU-06 | Autómata | Sebastián Jibaja | Entrenador | ALTA | 3 |
| HU-08 | Armas en patas delanteras | Daniel Mera | Consultor | MEDIA | 2 |
| HU-12 | Persistencia | Leonel Morales | Gestor | MEDIA | 3 |
| HU-13 | Aplicación dockerizada | Daniel Mera | Consultor | ALTA | 2 |

## Iteración 2 · Día 2 (18 puntos)

| # | Historia | Responsable | Rol XP | Prioridad | Puntos |
|---|---|---|---|---|---:|
| HU-02 | Presentar equipo | David Jumbo | Encargado de seguimiento | ALTA | 1 |
| HU-04 | Leer coordenadas con loading | Kened Lugmaña | Encargado de pruebas | ALTA | 3 |
| HU-07 | IA buscar arsenal | Daniel Mera | Consultor | ALTA | 3 |
| HU-09 | Vuelo y fuente de poder | Karen Mosquera | Cliente | MEDIA | 2 |
| HU-10 | Idiomas | David Jumbo | Encargado de seguimiento | BAJA | 2 |
| HU-11 | Pantallas del hormiguero virtual | Leonel Morales | Gestor | ALTA | 3 |
| HU-14 | Bomba | Sebastián Jibaja | Entrenador | ALTA | 2 |
| HU-15 | Configuración por archivo | Karen Mosquera | Cliente | ALTA | 2 |

## Carga por integrante

| Integrante | Rol XP | Día 1 | Día 2 | Total | Historias |
|---|---|---|---|---:|---|
| Sebastián Jibaja | Entrenador | XP-01 (3), HU-06 (3) = 6 | HU-14 (2) = 2 | 8 | XP-01, HU-06, HU-14 |
| Karen Mosquera | Cliente | XP-02 (2), HU-01 (3) = 5 | HU-09 (2), HU-15 (2) = 4 | 9 | XP-02, HU-01, HU-09, HU-15 |
| Kened Lugmaña | Encargado de pruebas | XP-05 (3), HU-03 (3) = 6 | HU-04 (3) = 3 | 9 | XP-05, HU-03, HU-04 |
| David Jumbo | Encargado de seguimiento | XP-03 (3), HU-05 (3) = 6 | HU-02 (1), HU-10 (2) = 3 | 9 | XP-03, HU-05, HU-02, HU-10 |
| Daniel Mera | Consultor | XP-04 (2), HU-08 (2), HU-13 (2) = 6 | HU-07 (3) = 3 | 9 | XP-04, HU-08, HU-13, HU-07 |
| Leonel Morales | Gestor | XP-06 (3), HU-12 (3) = 6 | HU-11 (3) = 3 | 9 | XP-06, HU-12, HU-11 |
| **Total** | | **35** | **18** | **53** | 21 historias |

La carga quedó equilibrada en 8 o 9 puntos por persona. El Día 1 es más pesado porque en él se construyen el framework y la base de la aplicación; el Día 2 deja margen para integrar, corregir y preparar la entrega.

## Orden de trabajo del Día 2 (dependencias)

Cada historia del Día 2 necesita algo del Día 1 o de otra historia del mismo día. El orden recomendado es:

| Orden | Historia | Necesita | Por qué |
|---|---|---|---|
| 1 | HU-15 Configuración por archivo | HU-01, HU-03, HU-06 | Las demás historias se prueban con cédulas escritas en `config/integrantes.csv`. |
| 2 | HU-14 Bomba | HU-06 | La bomba usa el autómata y debe estar lista antes de la IA. |
| 3 | HU-04 Leer coordenadas con loading | HU-03, HU-15 | Lee el `Grupo##.csv` que se genera desde las cédulas. |
| 4 | HU-09 Vuelo y fuente de poder | HU-08 | Usa la fuente de poder que ya alimenta a las armas. |
| 5 | HU-07 IA buscar arsenal | HU-14, HU-04 | Necesita la bomba y las coordenadas para presentar Geoposición, Tipo Arsenal y Acción. |
| 6 | HU-10 Idiomas | HU-05 | El dron necesita un piloto (hormiga adulta). Es la historia de menor prioridad: si falta tiempo, es la primera que se mueve. |
| 7 | HU-11 Pantallas del hormiguero virtual | HU-12, HU-05 | La pantalla usa la persistencia y las reglas de alimentación. |
| 8 | HU-02 Presentar equipo | HU-15 | Muestra el equipo ya cargado desde el archivo. |

Mañana del Día 2: historias 1 a 4. Tarde: historias 5 a 8, la revisión final con «Pruebas + reporte de aceptación» en VS Code y `xp verificar`. Las parejas de cada bloque están en `xp/reuniones.md`.

## Riesgos

| Riesgo | Plan |
|---|---|
| El Día 1 termina con menos de 35 puntos | Ejecutar `xp velocidad` en la reunión de cierre y usar `xp plan --proponer N` para repartir lo pendiente. |
| Faltan cédulas al empezar el Día 2 | La aplicación funciona con las cédulas registradas y avisa de las que faltan; se completan en `config/integrantes.csv` y se pulsa «Recargar configuración». |
| Una historia de baja prioridad no cabe | HU-10 (BAJA) es la primera en moverse; ninguna historia ALTA se mueve. |
