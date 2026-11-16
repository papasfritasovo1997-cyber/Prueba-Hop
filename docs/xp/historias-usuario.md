# Historias de usuario

Fuente de verdad: `xp/historias.csv` (la lee el framework). Estimación en puntos (1 punto ≈ 1 día ideal de una pareja).

| ID | Historia | Prioridad | Pts | Iter. | Responsable | Criterios de aceptación |
|---|---|---|---|---|---|---|
| XP-01 | Como equipo XP, quiero registrar historias en un CSV para priorizarlas y estimarlas | Alta | 2 | 1 | Integrante 2 | Lee id, prioridad, puntos, iteración y estado; indica la línea con error |
| XP-02 | Como encargado de seguimiento, quiero agrupar historias por iteración y medir la velocidad para planificar con datos reales | Alta | 3 | 1 | Integrante 4 | Avance por iteración; propuesta según velocidad; clima de ayer y promedio |
| XP-03 | Como gestor, quiero cargar el equipo y rotar parejas para que todos programen con todos | Alta | 2 | 1 | Integrante 6 | Lee la configuración; cada pareja se repite una sola vez por ciclo |
| XP-04 | Como consultor, quiero declarar tarjetas CRC en las clases para que el diseño no se desactualice | Media | 2 | 1 | Integrante 5 | `@TarjetaCRC`; genera Markdown |
| XP-05 | Como encargado de pruebas, quiero vincular cada prueba con su historia para saber qué historias están aceptadas | Alta | 3 | 1 | Integrante 3 | `@Historia`; reporte ACEPTADA / RECHAZADA / SIN_PRUEBAS |
| XP-06 | Como entrenador, quiero usar el framework desde la consola y en CI para seguir el proceso en cada reunión | Alta | 3 | 1 | Integrante 1 | Comandos de la CLI; pipeline en cada push |
| HU-01 | Como integrante o profesor, quiero ingresar con usuario y contraseña para usar el sistema | Alta | 3 | 2 | Integrante 2 | Usuarios del grupo y patmic/123; máximo 3 intentos |
| HU-02 | Como profesor, quiero ver el equipo y sus alumnos para identificar al grupo | Alta | 1 | 2 | Integrante 2 | `[+] TEAM [N]: nombre`; alumno con cédula y nombre |
| HU-03 | Como KGD, quiero crear `Grupo##.csv` desde las cédulas para atacar las coordenadas del equipo | Alta | 3 | 2 | Integrante 3 | Una coordenada por dígito; horario OTAN; palabra w del lenguaje |
| HU-04 | Como KGD, quiero leer y presentar `Grupo##.csv` para ver las coordenadas ucranianas | Alta | 3 | 2 | Integrante 3 | Loading `\|/-` hasta 100% por línea; tabla con días y tipo de arsenal |
| HU-05 | Como KGD, quiero que el TIPO_HORMIGA nazca como HLarva para tener hormigas que se transforman al comer | Alta | 3 | 2 | Integrante 4 | HLarva vive con néctar; se transforma con su alimento; el TIPO_HORMIGA vive solo con su alimento |
| HU-06 | Como KGD, quiero un autómata con el último dígito de cada cédula para destruir solo el arsenal definido | Alta | 5 | 3 | Integrante 1 | A=(Q,Σ,δ,q0,F) mínimo; gramática y grafo; explota solo en coordenadas objetivo |
| HU-07 | Como KGD, quiero que el AntCiberDron busque el tipo de arsenal para saber dónde explota la bomba | Alta | 3 | 3 | Integrante 5 | `buscar` devuelve true/false; presenta Geoposición, Tipo Arsenal y Acción |
| HU-08 | Como KGD, quiero cargar metralleta y láser para atacar por tierra | Media | 2 | 3 | Integrante 5 | Metralleta solo en la pata delantera derecha; láser en la izquierda con potencia según energía |
| HU-09 | Como KGD, quiero volar con el turbo reactor para dominar el aire | Media | 2 | 3 | Integrante 2 | Turbo reactor en la espalda consume energía; recarga y reemplazo asistidos por la IIA |
| HU-10 | Como experto parlante, quiero entrenar léxico, gramática y fonética para que el AntCiberDron hable inglés y español | Baja | 2 | 4 | Integrante 4 | Solo expertos del idioma pueden entrenarlo |
| HU-11 | Como usuario KGD, quiero registrarme y crear mis hormigas para tener mi hormiguero virtual | Alta | 4 | 4 | Integrante 6 | Registro e ingreso; crear HLarva; alimentar y ver su estado |
| HU-12 | Como usuario KGD, quiero que mi hormiguero se guarde para no perder mis hormigas | Media | 3 | 4 | Integrante 6 | SQLite según el modelo ER; claves con sal y hash |
| HU-13 | Como profesor, quiero ejecutar la aplicación con Docker para probarla sin instalar Java | Alta | 2 | 4 | Integrante 5 | `docker compose run consola`; `docker compose up web` |

## Plan de entregas

| Iteración | Entrega | Historias | Puntos |
|---|---|---|---|
| 1 | Framework XP | XP-01 … XP-06 | 15 |
| 2 | Consola: login, equipo, coordenadas, hormigas | HU-01 … HU-05 | 13 |
| 3 | Bomba e IA del AntCiberDron | HU-06 … HU-09 | 12 |
| 4 | Idiomas, hormiguero virtual y Docker | HU-10 … HU-13 | 11 |

Velocidad (`xp velocidad`): clima de ayer 11 pts, promedio 12,8 pts por iteración.
