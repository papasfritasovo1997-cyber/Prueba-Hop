# Diagrama de casos de uso

**Responsable:** Integrante 2 (Cliente) · **Historias:** XP-02, HU-01, HU-02 y HU-09.

## Actores

| Actor | Quién es | Qué hace |
|---|---|---|
| **Integrante del grupo** | Alumno del equipo (usuario de `config/integrantes.csv`) | Ingresa, edita sus cédulas, recarga la configuración y usa todas las pestañas de la aplicación. |
| **Profesor (patmic)** | Usuario `patmic` / clave `123` | Ingresa por consola, JavaFX o Docker para revisar el equipo, las coordenadas, la bomba y el autómata. |
| **Usuario KGD** | Jugador del hormiguero virtual (se registra solo) | Se registra, ingresa, crea `HLarva` y las alimenta. |
| **Experto parlante** | Persona que domina un idioma (`ExpertoParlante`) | Entrena léxico, gramática y fonética del AntCiberDron; solo puede entrenar su idioma nativo. |
| **Equipo XP** | Los seis integrantes en sus roles XP | Usa el framework desde la línea de comandos (`antdron.cmd xp ...`). |

## 1. Aplicación AntCiberDron y framework XP (Mermaid)

```mermaid
flowchart LR
    Integrante(["Integrante del grupo"])
    Profesor(["Profesor patmic"])
    Kgd(["Usuario KGD"])
    Experto(["Experto parlante"])
    Equipo(["Equipo XP"])

    subgraph app["Aplicación AntCiberDron (JavaFX y consola)"]
        direction TB
        CU01(["Ingresar con usuario y clave - 3 intentos"])
        CU02(["Ver equipo y alumnos"])
        CU03(["Editar cédulas en config/integrantes.csv"])
        CU04(["Recargar configuración"])
        CU05(["Generar Grupo##.csv, bomba y autómata"])
        CU06(["Ver coordenadas ucranianas con loading"])
        CU07(["Ver bomba y autómata"])
        CU08(["Probar una palabra en el autómata"])
        CU09(["IA buscar arsenal"])
        CU10(["Cargar armas, volar y gestionar energía"])
        CU11(["Entrenar idiomas"])
        CU12(["Registrarse e ingresar al hormiguero"])
        CU13(["Crear hormiga HLarva"])
        CU14(["Alimentar hormiga"])
    end

    subgraph xp["Framework XP (línea de comandos)"]
        direction TB
        XP01(["directrices"])
        XP02(["verificar"])
        XP03(["backlog"])
        XP04(["plan"])
        XP05(["velocidad"])
        XP06(["parejas y equipo"])
        XP07(["crc"])
        XP08(["aceptacion"])
        XP09(["probar"])
    end

    Integrante --- CU01
    Integrante --- CU02
    Integrante --- CU03
    Integrante --- CU04
    Integrante --- CU06
    Integrante --- CU07
    Integrante --- CU09
    Integrante --- CU10
    Profesor --- CU01
    Profesor --- CU02
    Profesor --- CU06
    Profesor --- CU07
    Profesor --- CU08
    Profesor --- CU09
    Kgd --- CU12
    Kgd --- CU13
    Kgd --- CU14
    Experto --- CU11

    CU04 -. include .-> CU05
    CU01 -. include .-> CU05
    CU06 -. include .-> CU05
    CU08 -. extend .-> CU07
    CU09 -. include .-> CU07
    CU13 -. include .-> CU12
    CU14 -. include .-> CU13

    Equipo --- XP01
    Equipo --- XP02
    Equipo --- XP03
    Equipo --- XP04
    Equipo --- XP05
    Equipo --- XP06
    Equipo --- XP07
    Equipo --- XP08
    Equipo --- XP09
```

## 2. Descripción de los casos de uso

| Caso de uso | Actor | Descripción breve | Historia |
|---|---|---|---|
| Ingresar con usuario y clave | Integrante, Profesor | Pide usuario y contraseña; después de **3 intentos** fallidos se bloquea. Los usuarios salen de `config/integrantes.csv` (más `patmic`/`123`). | HU-01 |
| Ver equipo y alumnos | Integrante, Profesor | Muestra `[+] TEAM [N]: nombre` y cada alumno con cédula y nombre (la cédula vacía se muestra como pendiente). | HU-02 |
| Editar cédulas | Integrante | El equipo escribe las cédulas en `config/integrantes.csv`; pueden quedar vacías con un aviso. | HU-15 |
| Recargar configuración | Integrante | El botón «Recargar configuración» vuelve a leer `config/` y regenera todo (o se reinicia la aplicación). | HU-15 |
| Generar `Grupo##.csv`, bomba y autómata | Sistema | Una coordenada por último dígito, con el horario de la estrategia OTAN y una palabra `w` del lenguaje; la bomba y `datos/automata-grupo##.md` se regeneran. | HU-03, HU-06, HU-14 |
| Ver coordenadas con loading | Integrante, Profesor | Lee `Grupo##.csv` con un loading giratorio hasta 100 % por línea y muestra la tabla Loading, Geoposición, días y Tipo Arsenal. | HU-04 |
| Ver bomba y autómata | Integrante, Profesor | Pestaña «Bomba y autómata»: lenguaje, coordenadas objetivo, definición formal, gramática y grafo dibujado. | HU-06, HU-14 |
| Probar una palabra | Profesor | Escribe una palabra `w` y pulsa «¿w ∈ L?»: la bomba explota solo si la coordenada es objetivo **y** el autómata acepta el arsenal. | HU-14 |
| IA buscar arsenal | Integrante, Profesor | `AntCiberDron.buscar(tipoArsenal)` devuelve `true` o `false` y presenta Geoposición, Tipo Arsenal y Acción. | HU-07 |
| Armas, vuelo y energía | Integrante | Metralleta en la pata delantera derecha, láser en la izquierda con potencia según la energía; el turbo reactor de la espalda consume energía; la IIA recarga o reemplaza la fuente. | HU-08, HU-09 |
| Entrenar idiomas | Experto parlante | Entrena léxico, gramática y fonética; un no experto recibe `EntrenamientoNoAutorizadoException`. | HU-10 |
| Registrarse e ingresar al hormiguero | Usuario KGD | Crea un usuario (clave con sal y hash) y su hormiguero; luego ingresa. | HU-11, HU-12 |
| Crear hormiga | Usuario KGD | Toda hormiga nace como `HLarva` según el `TIPO_HORMIGA` del grupo. | HU-05, HU-11 |
| Alimentar hormiga | Usuario KGD | La larva vive con néctar y se transforma con su alimento; los adultos viven solo con el suyo; queda en el historial. | HU-05, HU-11 |
| Comandos del framework | Equipo XP | `directrices`, `verificar`, `backlog`, `plan`, `velocidad`, `equipo`, `parejas`, `crc`, `aceptacion` y `probar`. | XP-01 a XP-06 |

## 3. Versión PlantUML

Para generar la imagen, pegue el código en <https://www.plantuml.com/plantuml> o use `plantuml casos-uso.puml`.

```plantuml
@startuml
left to right direction
skinparam packageStyle rectangle

actor "Integrante del grupo" as Integrante
actor "Profesor (patmic)" as Profesor
actor "Usuario KGD" as Kgd
actor "Experto parlante" as Experto
actor "Equipo XP" as Equipo

rectangle "Aplicación AntCiberDron (JavaFX y consola)" {
  usecase "Ingresar (3 intentos)" as CU01
  usecase "Ver equipo y alumnos" as CU02
  usecase "Editar cédulas\n(config/integrantes.csv)" as CU03
  usecase "Recargar configuración" as CU04
  usecase "Generar Grupo##.csv,\nbomba y autómata" as CU05
  usecase "Ver coordenadas con loading" as CU06
  usecase "Ver bomba y autómata" as CU07
  usecase "Probar una palabra" as CU08
  usecase "IA buscar arsenal" as CU09
  usecase "Armas, vuelo y energía" as CU10
  usecase "Entrenar idiomas" as CU11
  usecase "Registrarse e ingresar\nal hormiguero" as CU12
  usecase "Crear hormiga HLarva" as CU13
  usecase "Alimentar hormiga" as CU14
}

rectangle "Framework XP (línea de comandos)" {
  usecase "directrices" as XP01
  usecase "verificar" as XP02
  usecase "backlog" as XP03
  usecase "plan" as XP04
  usecase "velocidad" as XP05
  usecase "parejas y equipo" as XP06
  usecase "crc" as XP07
  usecase "aceptacion" as XP08
  usecase "probar" as XP09
}

Integrante --> CU01
Integrante --> CU02
Integrante --> CU03
Integrante --> CU04
Integrante --> CU06
Integrante --> CU07
Integrante --> CU09
Integrante --> CU10
Profesor --> CU01
Profesor --> CU02
Profesor --> CU06
Profesor --> CU07
Profesor --> CU08
Profesor --> CU09
Kgd --> CU12
Kgd --> CU13
Kgd --> CU14
Experto --> CU11

CU04 ..> CU05 : <<include>>
CU01 ..> CU05 : <<include>>
CU06 ..> CU05 : <<include>>
CU08 ..> CU07 : <<extend>>
CU09 ..> CU07 : <<include>>
CU13 ..> CU12 : <<include>>
CU14 ..> CU13 : <<include>>

Equipo --> XP01
Equipo --> XP02
Equipo --> XP03
Equipo --> XP04
Equipo --> XP05
Equipo --> XP06
Equipo --> XP07
Equipo --> XP08
Equipo --> XP09
@enduml
```
