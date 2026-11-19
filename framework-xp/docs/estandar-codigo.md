# Estándar de código

**Responsable:** Sebastián Jibaja (Entrenador / Coach). Aplica a los dos proyectos (`framework-xp` y `antdron`). Cualquiera puede proponer cambios en la retrospectiva.

## Java 17+ y JavaFX

| Tema | Regla |
|---|---|
| Idioma | Identificadores y mensajes en **español**. En identificadores no se usan tildes ni ñ (`HZangano`, `Disenio` → `Diseno`). Los textos que ve el usuario sí llevan tildes. |
| Nombres | Clases `PascalCase`, métodos y variables `camelCase`, constantes `MAYUSCULAS_CON_GUION`. Paquetes en minúscula, salvo `pkAntCiberDronContract`, cuyo nombre exige el enunciado. |
| Paquetes | Framework: `ec.edu.epn.xp.<modulo>`. Aplicación: `ec.edu.epn.antdron.<seccion>`. Cada integrante trabaja en su paquete (ver la división de tareas). |
| Formato | 4 espacios, llaves en la misma línea, máximo 120 columnas, sin `import` con `*`. |
| Clases | Una responsabilidad por clase. Campos `private` y `final` cuando sea posible. Las clases de valor son inmutables. |
| Errores | Validar la entrada y lanzar `IllegalArgumentException` o `IllegalStateException` con un mensaje claro en español. Ningún programa termina mostrando un stack trace al usuario. |
| Comentarios | Javadoc breve en clases e interfaces públicas. Comentar solo el **porqué** de lo que no es obvio. |
| Dependencias | La aplicación **no** depende del framework. Sin Maven ni bibliotecas externas: solo el JDK y los `.jar` de JavaFX de `lib/` (funcionalidad mínima). |
| Pantallas | Las vistas JavaFX viven en `ec.edu.epn.antdron.ui` y solo muestran datos: las reglas del dominio están en sus paquetes y se prueban sin abrir ventanas. Toda acción del usuario captura los errores y los muestra en pantalla. |
| Seguridad | Las claves de los usuarios KGD se guardan con sal y hash. En `config/integrantes.csv` se recomienda `sha256:<hash>` en lugar de la clave en texto; nunca se suben contraseñas reales. |

## Pruebas

- Pruebas con el ejecutor del framework (`@Prueba`, `Afirmar`); no se usa JUnit ni Maven. La prueba se escribe antes que el código.
- Cada clase de prueba lleva `@Historia("HU-nn")` o `@Historia("XP-nn")` con la historia que verifica; su nombre termina en `Test`.
- Nombres descriptivos en español que digan el comportamiento: `bloqueaAlTercerIntentoFallido()`.
- Sin dependencias del orden de ejecución ni de la red; los archivos de prueba se crean con `Temporal.directorio()`.

## Git

- Una rama por historia: `hu-06-automata`, `xp-03-plan-entregas`.
- Commits pequeños, en presente y en español: `Agrega minimización del AFD`.
- Se integra por pull request con «Pruebas + reporte de aceptación» en VS Code en verde y la revisión de otra pareja; nadie sube directo a la rama principal.
- Cada integrante sube únicamente los archivos de su sección.
