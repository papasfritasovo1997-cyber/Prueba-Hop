# Reuniones y rotación de parejas

**Responsable:** Integrante 6 (Gestor) · **Prácticas XP:** Reuniones diarias de pie (stand-up) y rotación de parejas

El Gestor convoca las reuniones, controla el tiempo y anota los acuerdos aquí. Las reuniones son **de pie y de máximo 10 minutos**: no se resuelven problemas en la reunión, se acuerda con quién se resuelven.

## Calendario de reuniones

| Reunión | Cuándo | Duración | Para qué |
|---|---|---|---|
| Inicio del Día 1 | Antes de la primera ronda | 10 min | Arrancar la Iteración 1: repasar las historias del día y confirmar roles y parejas. |
| Mediodía del Día 1 | Entre la mañana y la tarde | 10 min | Ver el avance, mover impedimentos y rotar parejas. |
| Inicio del Día 2 | Antes de la primera ronda | 10 min | Revisar la velocidad del Día 1 (`xp velocidad`) y arrancar la Iteración 2. |
| Mediodía del Día 2 | Entre la mañana y la tarde | 10 min | Ver el avance, ordenar lo que falta de la entrega. |
| Cierre y retrospectiva | Al final del Día 2 | 20 min | Revisar lo que salió bien y lo que se mejora; verificar las directrices. |

## Rotación de parejas

Cada día tiene dos bloques (mañana y tarde) y en cada bloque trabajan **tres parejas**, de modo que todos programan con todos y cada pareja se repite una sola vez. La tabla es la que entrega `xp parejas`.

| Bloque | Ronda | Pareja A | Pareja B | Pareja C |
|---|---:|---|---|---|
| Día 1 · mañana | 1 | Integrante 1 – Integrante 6 | Integrante 2 – Integrante 5 | Integrante 3 – Integrante 4 |
| Día 1 · tarde | 2 | Integrante 1 – Integrante 5 | Integrante 6 – Integrante 4 | Integrante 2 – Integrante 3 |
| Día 2 · mañana | 3 | Integrante 1 – Integrante 4 | Integrante 5 – Integrante 3 | Integrante 6 – Integrante 2 |
| Día 2 · tarde | 4 | Integrante 1 – Integrante 3 | Integrante 4 – Integrante 2 | Integrante 5 – Integrante 6 |

Reglas:

1. En cada pareja hay un **conductor** (teclea) y un **navegante** (piensa y revisa); se cambian de rol cada 20–30 minutos.
2. La pareja trabaja en la historia de uno de los dos; el responsable de la historia (ver `xp/historias.csv`) decide el orden de las tareas.
3. Al cambiar de ronda, quien se queda con la tarea explica el estado a su nueva pareja.
4. Se integra y se prueba en cada cambio de ronda: «Pruebas + reporte de aceptación» en VS Code debe pasar antes de rotar.

## Formato de cada reunión

Cada integrante responde en una o dos frases:

1. ¿Qué terminé desde la última reunión?
2. ¿Qué voy a hacer ahora y con quién?
3. ¿Qué me impide avanzar?

Después, el Gestor anota **acuerdos**, **impedimentos** (con responsable y plazo) y los **puntos terminados** del bloque.

### Plantilla

```text
## Reunión: <nombre> · <día y hora>
Asistentes: Integrante 1, 2, 3, 4, 5, 6
Duración: <min>
Parejas del bloque: A-B / C-D / E-F

Avances:
- Integrante n: ...
Impedimentos:
- ... (responsable: Integrante n, plazo: ...)
Acuerdos:
- ...
Puntos terminados en el bloque: <n> · Acumulado de la iteración: <n> de <planificados>
```

## Registro de reuniones

### Reunión: Inicio del Día 1 · Iteración 1

- **Asistentes:** Integrantes 1 a 6 · **Duración:** 10 min
- **Parejas del bloque:** 1–6, 2–5, 3–4 (ronda 1)
- **Objetivo del día:** 35 puntos (13 historias).
- **Avances:** el Cliente (Integrante 2) presentó el backlog `xp/historias.csv`; el Consultor (Integrante 5) propuso la metáfora «Hormiguero de guerra».
- **Impedimentos:** ninguno.
- **Acuerdos:**
  - Primero el núcleo del framework (XP-01) y el autómata (HU-06): son la base de lo demás.
  - Las cédulas se escriben en `config/integrantes.csv` cuando cada uno las tenga; mientras tanto la aplicación avisa y sigue funcionando.
  - Se integra cada vez que cambia la ronda.

### Reunión: Mediodía del Día 1

- **Asistentes:** Integrantes 1 a 6 · **Duración:** 10 min
- **Parejas del bloque siguiente:** 1–5, 6–4, 2–3 (ronda 2)
- **Avances:** núcleo y directrices en marcha; autenticación y generación de `Grupo##.csv` con sus pruebas.
- **Impedimentos:** VS Code no reconocía JavaFX en una computadora porque se abrió una subcarpeta; siempre se abre la raíz del repositorio, donde están `.vscode/` y `lib/` (responsable: Integrante 5).
- **Acuerdos:** cada pareja deja su historia con pruebas antes del cambio de ronda; el Encargado de seguimiento actualiza los estados en `xp/historias.csv`.

### Reunión: Inicio del Día 2 · Iteración 2

- **Asistentes:** Integrantes 1 a 6 · **Duración:** 10 min
- **Parejas del bloque:** 1–4, 5–3, 6–2 (ronda 3)
- **Velocidad del Día 1:** se leyó con `xp velocidad`; el objetivo del Día 2 es de 18 puntos (8 historias).
- **Acuerdos:**
  - Orden del Día 2 según `xp/plan-entregas.md`: HU-15, HU-14, HU-04 y HU-09 por la mañana; HU-07, HU-10, HU-11 y HU-02 por la tarde.
  - Si algo no cabe se mueve primero HU-10 (prioridad BAJA).

### Reunión: Mediodía del Día 2

- **Asistentes:** Integrantes 1 a 6 · **Duración:** 10 min
- **Parejas del bloque siguiente:** 1–3, 4–2, 5–6 (ronda 4)
- **Avances:** configuración por archivo, bomba y coordenadas con loading listas.
- **Impedimentos:** ninguno anotado.
- **Acuerdos:** cada integrante escribe su cédula en `config/integrantes.csv` y se pulsa «Recargar configuración» para regenerar `Grupo##.csv`, la bomba y el autómata del equipo.

### Reunión: Cierre y retrospectiva · Día 2

- **Asistentes:** Integrantes 1 a 6 · **Duración:** 20 min
- **Verificación:** «Pruebas + reporte de aceptación» en VS Code sin fallos y `xp verificar` con todas las directrices en `[OK]`.
- **Salió bien:** las parejas rotativas repartieron el conocimiento; el framework avisó qué artefacto faltaba y de quién era.
- **A mejorar:** estimar con más holgura el primer día y escribir las cédulas desde el inicio.
- **Acuerdos finales:** entregar el repositorio con `config/integrantes.csv` completo y los archivos de `datos/` regenerados.
