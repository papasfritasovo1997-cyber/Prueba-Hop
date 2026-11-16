# Diagrama de casos de uso

**Responsable:** Integrante 2 (Cliente).

Actores: **Integrante del grupo**, **Profesor** (patmic), **Usuario KGD** (pantallas web), **Experto parlante** y **Equipo XP** (framework).

```mermaid
flowchart LR
    alumno(["👤 Integrante del grupo"])
    profesor(["👤 Profesor (patmic)"])
    kgd(["👤 Usuario KGD"])
    experto(["👤 Experto parlante"])
    equipo(["👤 Equipo XP"])

    subgraph Consola["Sistema AntCiberDron · consola"]
        UC1(("Autenticarse<br/>máx. 3 intentos"))
        UC2(("Ver equipo<br/>y alumnos"))
        UC3(("Generar<br/>Grupo##.csv"))
        UC4(("Ver coordenadas<br/>con loading"))
        UC5(("Buscar arsenal<br/>y detonar bomba"))
        UC6(("Ver autómata,<br/>gramática y grafo"))
        UC7(("Usar armas, volar<br/>y gestionar energía"))
        UC8(("Entrenar idiomas"))
    end

    subgraph Web["Hormiguero virtual · web"]
        UC9(("Registrarse"))
        UC10(("Iniciar sesión"))
        UC11(("Crear hormiga<br/>(HLarva)"))
        UC12(("Alimentar hormiga"))
    end

    subgraph Framework["Framework XP · CLI"]
        UC13(("Gestionar backlog<br/>y plan de entregas"))
        UC14(("Consultar velocidad"))
        UC15(("Rotar parejas"))
        UC16(("Reporte de aceptación"))
    end

    alumno --- UC1
    profesor --- UC1
    UC1 -. include .-> UC2
    UC2 -. include .-> UC4
    UC4 -. extend .-> UC3
    UC4 -. include .-> UC5
    alumno --- UC6
    alumno --- UC7
    experto --- UC8
    kgd --- UC9
    kgd --- UC10
    kgd --- UC11
    kgd --- UC12
    UC11 -. include .-> UC10
    UC12 -. include .-> UC10
    equipo --- UC13
    equipo --- UC14
    equipo --- UC15
    equipo --- UC16
```

## Versión UML (PlantUML)

Pegue este código en https://www.plantuml.com/plantuml para obtener el diagrama UML estándar:

```plantuml
@startuml
left to right direction
actor "Integrante del grupo" as Alumno
actor "Profesor (patmic)" as Profesor
actor "Usuario KGD" as KGD
actor "Experto parlante" as Experto
actor "Equipo XP" as Equipo

rectangle "AntCiberDron (consola)" {
  usecase "Autenticarse (máx. 3 intentos)" as UC1
  usecase "Ver equipo y alumnos" as UC2
  usecase "Generar Grupo##.csv" as UC3
  usecase "Ver coordenadas con loading" as UC4
  usecase "Buscar arsenal y detonar bomba" as UC5
  usecase "Ver autómata, gramática y grafo" as UC6
  usecase "Usar armas, volar y gestionar energía" as UC7
  usecase "Entrenar idiomas" as UC8
}
rectangle "Hormiguero virtual (web)" {
  usecase "Registrarse" as UC9
  usecase "Iniciar sesión" as UC10
  usecase "Crear hormiga (HLarva)" as UC11
  usecase "Alimentar hormiga" as UC12
}
rectangle "Framework XP (CLI)" {
  usecase "Gestionar backlog y plan" as UC13
  usecase "Consultar velocidad" as UC14
  usecase "Rotar parejas" as UC15
  usecase "Reporte de aceptación" as UC16
}
Profesor --|> Alumno
Alumno --> UC1
UC1 ..> UC2 : <<include>>
UC2 ..> UC4 : <<include>>
UC3 ..> UC4 : <<extend>>
UC4 ..> UC5 : <<include>>
Alumno --> UC6
Alumno --> UC7
Experto --> UC8
KGD --> UC9
KGD --> UC10
KGD --> UC11
KGD --> UC12
UC11 ..> UC10 : <<include>>
UC12 ..> UC10 : <<include>>
Equipo --> UC13
Equipo --> UC14
Equipo --> UC15
Equipo --> UC16
@enduml
```
