# Framework XP · Guía de directrices

**Responsable:** Integrante 1 (Entrenador / Coach) · **Historia:** XP-01

Este framework es la **Parte 1** del proyecto. Define *cómo* trabaja el equipo con Extreme Programming y trae herramientas para comprobar que se cumple. La **Parte 2** (la aplicación AntCiberDron) sigue estas directrices, pero **no depende del framework**: compila, se prueba y se ejecuta sin él.

```
Framework XP (framework-xp/)           Aplicación (antdron/)
  dicta las directrices  ───────────▶   las cumple (historias, pruebas, CRC, CI, Docker)
  verifica los artefactos ◀──────────   produce los artefactos
  (sin dependencia de código entre ambos)
```

## 1. Valores

Comunicación, simplicidad, retroalimentación, coraje y respeto. Toda regla de esta guía sale de uno de ellos.

## 2. Roles

| Rol | Responsabilidad en este framework |
|---|---|
| Entrenador (Coach) | Hace cumplir esta guía y el estándar de código; mantiene el núcleo del framework |
| Cliente | Escribe y prioriza las historias, define los criterios de aceptación y acepta lo terminado |
| Encargado de pruebas (Tester) | Prepara y ejecuta las pruebas de aceptación y publica el reporte |
| Encargado de seguimiento (Tracker) | Mide la velocidad y el avance de cada iteración |
| Consultor | Metáfora, tarjetas CRC, soluciones puntuales y despliegue |
| Gestor (Big Boss) | Equipo, parejas, reuniones e integración continua |

Todos los roles programan. El rol de cada integrante se declara en `config/integrantes.csv`.

## 3. Directrices por fase

`antdron.cmd xp directrices` muestra esta tabla con las rutas del proyecto. La fuente de verdad es `ec.edu.epn.xp.directrices.Practica`.

| Fase | Práctica | Responsable | Artefacto por defecto | Regla |
|---|---|---|---|---|
| I. Planificación | Historias de usuario | Cliente | `xp/historias.csv` | "Como <rol>, quiero <funcionalidad> para <beneficio>", con prioridad, 1 a 5 puntos y criterios de aceptación. Más de 5 puntos: se divide. |
| | Plan de entregas | Tracker | `xp/plan-entregas.md` | El cliente elige las historias de cada entrega; cada entrega es software que funciona. |
| | Velocidad del proyecto | Tracker | `xp/historias.csv` | No se comprometen más puntos que los terminados en la iteración anterior. |
| | Iteraciones | Tracker | `xp/historias.csv` | Duración fija y corta, definida al iniciar (en ANTDRON, 1 día); cada historia tiene iteración y estado. |
| | Rotaciones | Gestor | `config/integrantes.csv` | Las parejas rotan en cada bloque de trabajo (al menos una vez por iteración). |
| | Reuniones | Gestor | `xp/reuniones.md` | Reunión diaria de pie de 15 min; se anotan los acuerdos. |
| II. Diseño | Metáfora del sistema | Consultor | `xp/metafora.md` | Una historia compartida explica el sistema y nombra sus partes. |
| | Tarjetas CRC | Consultor | `xp/tarjetas-crc.md` | Toda clase principal tiene tarjeta y se actualiza con el código. |
| | Soluciones puntuales | Consultor | `xp/spikes.md` | Prototipo pequeño y desechable ante cada duda técnica; se anota la conclusión. |
| | Funcionalidad mínima | Coach | — | Solo se programa lo que piden las historias de la iteración. |
| | Reciclaje | Coach | — | Se refactoriza con todas las pruebas en verde. |
| III. Desarrollo | Disponibilidad del cliente | Cliente | `config/integrantes.csv` | Hay un integrante con rol CLIENTE que responde dudas y acepta historias. |
| | Unidad de pruebas | Tester | `pruebas` | La prueba se escribe antes que el código; cada clase de prueba lleva `@Historia("<id>")`. |
| | Programación por parejas | Gestor | `config/integrantes.csv` | Todo el código de producción se escribe en parejas. |
| | Integración continua | Gestor | `.github/workflows/ci.yml` | Cada push compila y prueba; no se integra con la CI en rojo. |
| IV. Pruebas | Implantación | Consultor | `Dockerfile` | Cada entrega se despliega con un solo comando. |
| | Pruebas de aceptación | Tester | `xp/pruebas-aceptacion.md` | Una historia está terminada solo si pasan todas sus pruebas de aceptación. |

Cada proyecto puede cambiar la ruta de un artefacto en `xp.properties` (raíz del repositorio):

```properties
artefacto.UNIDAD_PRUEBAS=antdron/pruebas
artefacto.IMPLANTACION=Dockerfile
```

`antdron.cmd xp verificar` revisa que exista cada artefacto e indica el responsable de lo que falta. Termina con código 1 si falta algo, así que la integración continua puede usarlo como control.

## 4. Formatos mínimos compartidos

Cada responsable define el detalle de su artefacto, pero estos campos son obligatorios para que los módulos del framework se entiendan entre sí:

| Archivo | Separador | Columnas obligatorias (en este orden) |
|---|---|---|
| `xp/historias.csv` | `;` | `id;titulo;como;quiero;para;prioridad;puntos;iteracion;estado;responsable;criterios` |
| `config/integrantes.csv` | `;` | `cedula;nombre;usuario;clave;rol` (la cédula puede quedar vacía) |

- `id`: `XP-nn` (framework) o `HU-nn` (aplicación). `prioridad`: `ALTA`, `MEDIA` o `BAJA`. `puntos`: 1 a 5. `iteracion`: número (vacío si aún no se asigna). `estado`: `PENDIENTE`, `EN_PROGRESO` o `TERMINADA`. `criterios`: separados por `|`.
- `rol`: un valor de `RolXP` (`ENTRENADOR`, `CLIENTE`, `ENCARGADO_PRUEBAS`, `ENCARGADO_SEGUIMIENTO`, `CONSULTOR`, `GESTOR`).
- Líneas vacías y las que empiezan con `#` se ignoran.

## 5. Pruebas unitarias y trazabilidad (sin JUnit ni Maven)

El framework trae su propio ejecutor de pruebas (`ec.edu.epn.xp.pruebas`). Cada clase de prueba termina en `Test` y se etiqueta con la historia que verifica:

```java
@Historia("HU-06")
class BombaTest {
    @Prueba
    void explotaSoloEnCoordenadasObjetivo() {
        Afirmar.verdadero(bomba.debeExplotar("Coord-05", "abcdttt"));
    }
}
```

`antdron.cmd probar` compila y ejecuta las pruebas del framework y de la aplicación, guarda los resultados por historia en `build/pruebas/resultados.csv` y escribe el reporte `xp/pruebas-aceptacion.md`.

Solo las **pruebas** de la aplicación usan el framework. El código de la aplicación (`antdron/src`) se compila **sin** el framework en el classpath: si alguien lo importara por error, la compilación fallaría. Así la aplicación vale por sí sola y el framework dicta y verifica el proceso.

## 6. Ciclo de una iteración

En ANTDRON el proyecto dura **2 días** y cada día es una iteración. Un punto de historia equivale a **1 hora ideal de trabajo en pareja**.

| Momento del día | Duración | Qué se hace |
|---|---|---|
| Inicio | 30 min | **Planificación:** el Cliente prioriza y el Tracker compara los puntos con la velocidad (`xp plan`, `xp velocidad`). El día 1 se usa la estimación; el día 2, lo terminado el día 1. |
| Inicio | 15 min | **Reunión de pie:** qué hará cada uno, qué lo bloquea. El Gestor publica las parejas de la mañana (`xp parejas`). |
| Mañana | ~3 h | **Desarrollo:** prueba → código → reciclaje, en pareja y en una rama por historia. |
| Mediodía | 15 min | **Reunión de pie de control** y rotación de parejas para la tarde. |
| Tarde | ~3 h | **Desarrollo e integración:** pull request con la CI en verde y la revisión de otra pareja. |
| Cierre | 30 min | **Aceptación:** el Tester ejecuta `xp aceptacion` y el Cliente marca `TERMINADA`. **Retrospectiva:** se ajustan la guía y el estándar si hace falta. |

Una historia que no se termina en su día vuelve al plan del día siguiente; nunca se alarga la iteración.

## 7. Definición de terminado (DoD)

Una historia solo pasa a `TERMINADA` cuando:

- [ ] Cada criterio de aceptación tiene al menos una prueba con `@Historia` de la historia.
- [ ] `antdron.cmd probar` pasa en local y en la CI.
- [ ] Se programó en pareja y otra pareja revisó el pull request.
- [ ] Cumple el [estándar de código](estandar-codigo.md).
- [ ] Su tarjeta CRC y el diagrama afectado están actualizados.

## 8. Extender el framework

El núcleo (`ec.edu.epn.xp`) no conoce a los demás módulos. Cada módulo agrega comandos así:

1. Implementar `ec.edu.epn.xp.ComandoXP` (`nombre()`, `descripcion()`, `ejecutar(argumentos, contexto)`).
2. Agregar el nombre completo de la clase, uno por línea, en
   `framework-xp/recursos/META-INF/services/ec.edu.epn.xp.ComandoXP`.
3. Leer rutas con `contexto.ruta("clave", "valor/por/defecto")` o `contexto.artefacto(Practica.X)`, nunca con rutas fijas.
4. Lanzar `IllegalArgumentException` con un mensaje claro ante datos inválidos; la CLI lo muestra sin stack trace.

Comandos del núcleo: `ayuda`, `directrices [fase]` y `verificar [fase]`.

## 9. Uso

Desde la raíz del repositorio (en Linux/macOS, `./antdron.sh` en lugar de `antdron.cmd`):

```bash
antdron.cmd xp ayuda                 # todos los comandos del framework
antdron.cmd xp directrices diseno    # reglas de una fase
antdron.cmd xp verificar             # qué artefacto falta y de quién es
antdron.cmd xp backlog               # historias de usuario
antdron.cmd xp plan                  # plan de entregas por iteración
antdron.cmd xp velocidad             # velocidad del proyecto
antdron.cmd xp parejas 2             # parejas del bloque 2
antdron.cmd xp crc                   # clases sin tarjeta CRC
antdron.cmd probar                   # pruebas + reporte de aceptación
```
