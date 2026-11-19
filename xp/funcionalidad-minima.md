# Funcionalidad mínima

**Responsable:** Karen Mosquera (Cliente) · **Práctica XP:** Funcionalidad mínima · **Se defiende en:** S2 (captura `f2-funcionalidad-minima.png`)

Regla del equipo: se programa solo lo que piden las historias de la iteración actual, nada «por si acaso». Como Cliente, decido qué historias son imprescindibles para que el profesor pueda usar el AntCiberDron y cuáles pueden esperar. El backlog completo, con criterios de aceptación, está en `xp/historias.csv`.

## Lo imprescindible (prioridad ALTA)

Sin estas historias la aplicación no cumple el enunciado o no se puede presentar.

| Historia | Por qué es imprescindible | Iteración |
|---|---|---|
| HU-01 Autenticación | Nadie entra sin usuario y clave; máximo 3 intentos. Es lo primero que ve el profesor. | 1 |
| HU-15 Configuración por archivo | Con solo escribir las cédulas se genera todo; sin ella habría que tocar el código. | 2 |
| HU-02 Presentar equipo | El enunciado pide mostrar el grupo, sus integrantes y roles. | 2 |
| HU-03 Generar `Grupo04.csv` | Las coordenadas salen de las cédulas del equipo. | 1 |
| HU-04 Leer coordenadas con loading | El sistema ruso lee el archivo y muestra la carga. | 2 |
| HU-05 Hormigas y alimentos | Nuestra hormiga es HZángano y vive con Omnívoro. | 1 |
| HU-06 Autómata | Decide en qué coordenadas puede actuar la bomba. | 1 |
| HU-07 IA buscar arsenal | Es la misión del AntCiberDron. | 2 |
| HU-11 Pantallas del hormiguero virtual | El usuario de la KGD crea y alimenta sus hormigas. | 2 |
| HU-13 Aplicación dockerizada | La aplicación se entrega y se muestra con un solo comando. | 1 |
| HU-14 Bomba | Explota solo donde el autómata lo permite. | 2 |

## Necesario, pero después (prioridad MEDIA)

| Historia | Por qué puede esperar |
|---|---|
| HU-08 Armas en patas delanteras | Complementa la misión; el dron busca sin armas. |
| HU-09 Vuelo y fuente de poder | Se agrega cuando la búsqueda ya funciona. |
| HU-12 Persistencia (SQLite) | El hormiguero funciona en memoria; guardar es el siguiente paso. |

## Si queda tiempo (prioridad BAJA)

| Historia | Por qué es lo último |
|---|---|
| HU-10 Idiomas | No bloquea ninguna otra historia ni la presentación. |

## Lo que no se hace

- Nada que no esté en una historia: ni pantallas extra ni reportes que el enunciado no pida.
- No se agregan librerías «por si acaso»: JavaFX y el controlador de SQLite son las únicas.

Las historias del framework (XP-01 a XP-06) también son ALTA salvo XP-04, porque sin ellas no se puede demostrar el proceso XP.
