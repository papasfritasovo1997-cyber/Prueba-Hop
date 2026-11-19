# Evidencias de XP · Grupo 4 Pragmatics

**Responsable:** Leonel Morales (Gestor). Índice para el profesor: cada práctica XP con su artefacto y su evidencia (captura, video, acta o PR). El avance de cada tarea se ve en la pestaña **Tablero XP** de la aplicación, que lee `xp/tareas.csv`.

- Las capturas se guardan en [`docs/evidencias/`](evidencias/) con el nombre indicado; al subirla, la tarea pasa sola a «Hecha» en el tablero.
- Los videos se suben a Drive o YouTube (oculto) y se pega aquí el enlace; no se suben al repositorio.
- Nunca se capturan cédulas.

## I. Planificación (S1 · capturas)

| Práctica | Artefacto | Quién | Evidencia |
|---|---|---|---|
| Historias de usuario | [`xp/historias.csv`](../xp/historias.csv) | Karen Mosquera | [f1-historias.png](evidencias/f1-historias.png) · validación de Kened: [f1-validacion.png](evidencias/f1-validacion.png) |
| Plan de entregas | [`xp/plan-entregas.md`](../xp/plan-entregas.md) | David Jumbo | [f1-plan-entregas.png](evidencias/f1-plan-entregas.png) |
| Velocidad del proyecto | `xp velocidad` | David Jumbo | [f1-velocidad.png](evidencias/f1-velocidad.png) |
| Iteraciones | [`xp/plan-entregas.md`](../xp/plan-entregas.md) (iteraciones 1 y 2) | David Jumbo | [f1-plan-entregas.png](evidencias/f1-plan-entregas.png) |
| Rotaciones | [`xp/reuniones.md`](../xp/reuniones.md) o `xp parejas` | Leonel Morales | [f1-rotaciones.png](evidencias/f1-rotaciones.png) |
| Reuniones | [`xp/reuniones.md`](../xp/reuniones.md) (actas de coordinación, S3 y S4) | Leonel Morales | [f1-reunion-coordinacion.png](evidencias/f1-reunion-coordinacion.png) |
| (Reglas XP del equipo) | [`framework-xp/docs/guia-xp.md`](../framework-xp/docs/guia-xp.md) | Sebastián Jibaja | [f1-reglas-xp.png](evidencias/f1-reglas-xp.png) |

## II. Diseño (S2 · capturas y diagramas)

| Práctica | Artefacto | Quién | Evidencia |
|---|---|---|---|
| Metáfora del sistema | [`xp/metafora.md`](../xp/metafora.md) | Daniel Mera | [f2-metafora.png](evidencias/f2-metafora.png) |
| Tarjetas CRC | [`xp/tarjetas-crc.md`](../xp/tarjetas-crc.md) o `xp crc` | Daniel Mera | [f2-crc.png](evidencias/f2-crc.png) |
| Soluciones puntuales (spikes) | [`xp/spikes.md`](../xp/spikes.md) | Sebastián (1 y 5), Daniel (2), Leonel (3), Kened (4) | [f2-spikes-1-5.png](evidencias/f2-spikes-1-5.png) · [f2-spike2.png](evidencias/f2-spike2.png) · [f2-spike3.png](evidencias/f2-spike3.png) · [f2-spike4.png](evidencias/f2-spike4.png) |
| Funcionalidad mínima | [`xp/funcionalidad-minima.md`](../xp/funcionalidad-minima.md) | Karen Mosquera | [f2-funcionalidad-minima.png](evidencias/f2-funcionalidad-minima.png) |
| Reciclaje (refactorización) | [`Utility`](../antdron/src/ec/edu/epn/antdron/pkAntCiberDronContract/Utility.java) y las clases que la usan | Sebastián Jibaja y Daniel Mera | [f2-utility.png](evidencias/f2-utility.png) · [f2-reciclaje-pruebas.png](evidencias/f2-reciclaje-pruebas.png) · [f2-utility-crc.png](evidencias/f2-utility-crc.png) |

**Diagramas obligatorios** (en [`docs/diagramas/`](diagramas/)):

| Diagrama | Quién | Evidencia |
|---|---|---|
| [Arquitectura](diagramas/arquitectura.md) | Daniel Mera | [f2-arquitectura.png](evidencias/f2-arquitectura.png) |
| [Casos de uso](diagramas/casos-uso.md) | Karen Mosquera | [f2-casos-uso.png](evidencias/f2-casos-uso.png) |
| [Clases](diagramas/clases.md) | David Jumbo | [f2-clases.png](evidencias/f2-clases.png) |
| [Grafo del autómata](diagramas/grafo-automata.md) | Sebastián Jibaja | [f2-grafo-automata.png](evidencias/f2-grafo-automata.png) |
| [Entidad-relación (SQLite)](diagramas/modelo-er.md) | Leonel Morales | [f2-modelo-er.png](evidencias/f2-modelo-er.png) |

## III. Desarrollo (S3 · video de ~12 min, Karen y Sebastián)

| Práctica | Artefacto | Quién | Evidencia |
|---|---|---|---|
| Programación por parejas | Live Share sobre HU-01 (`Autenticador`, `Claves`) | Karen Mosquera y Sebastián Jibaja | Video S3: _(enlace)_ · [f3-live-share.png](evidencias/f3-live-share.png) |
| Disponibilidad del cliente | Karen confirma los criterios de HU-01 y aprueba el PR | Karen Mosquera | Video S3: _(enlace)_ · PR: _(enlace)_ |
| Unidad de pruebas | [`antdron/pruebas/`](../antdron/pruebas/) | Kened Lugmaña | [f3-pruebas-hu01.png](evidencias/f3-pruebas-hu01.png) |
| Integración continua | PR aprobado y fusionado a `main` | Sebastián Jibaja | PR: _(enlace)_ |

## IV. Pruebas (S4 · video de ~20 min, Leonel, Kened y Daniel)

| Práctica | Artefacto | Quién | Evidencia |
|---|---|---|---|
| Implantación | [`Dockerfile`](../Dockerfile) y `docker compose up --build` | Daniel Mera | Video S4: _(enlace)_ · [f4-docker.png](evidencias/f4-docker.png) |
| Pruebas de aceptación | [`xp/pruebas-aceptacion.md`](../xp/pruebas-aceptacion.md) | Kened Lugmaña y Karen Mosquera | Video S4: _(enlace)_ · aceptación de Karen en GitHub: _(enlace)_ |
| (Estado y velocidad final) | `xp plan` y `xp velocidad` | David Jumbo | [f4-estado-velocidad.png](evidencias/f4-estado-velocidad.png) |
| (Versión final) | Release `v1.0` con la evaluación de XP | Sebastián Jibaja | Release: _(enlace)_ |
| (Retrospectiva) | Acta en [`xp/reuniones.md`](../xp/reuniones.md) | Leonel Morales | [f4-cierre.png](evidencias/f4-cierre.png) |
