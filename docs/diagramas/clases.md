# Diagrama de clases

**Responsable:** David Jumbo (Encargado de seguimiento) · **Historias:** HU-05 (hormigas y alimentos) y HU-10 (idiomas).

El código tiene **dos partes independientes**: la aplicación `antdron/` (JavaFX) y el framework `framework-xp/`. La aplicación no importa ninguna clase del framework. Los diagramas están divididos por tema para que se lean bien; las relaciones entre diagramas se indican en el texto.

Convenciones: `<<abstract>>` clase abstracta, `<<interface>>` interfaz, `<<enumeration>>` enum, `<<record>>` registro de Java.

## 1. Aplicación · Hormigas y alimentos (HU-05)

`Hormiga` y `Alimento` son abstractas y el contrato `pkAntCiberDronContract` define `IHormiga` e `IIA`, como pide el enunciado. Toda hormiga **nace como `HLarva`**: vive con néctar y se transforma al comer el alimento de su `TipoHormiga` (según el número de grupo: 1-2 `HSoldado`, 3-4 `HZangano`, 5 `HRastreadora`).

```mermaid
classDiagram
    direction TB

    class IHormiga {
        <<interface>>
        +comer(Alimento) boolean
    }
    class Hormiga {
        <<abstract>>
        -Integer id
        -String sexo
        -String estado
        -List~Extremidad~ extremidades
        +getTipo() String
        +getAlimentoVital() Alimento
        +comer(Alimento) boolean
        +estaViva() boolean
        #setEstado(String) void
    }
    class HLarva {
        -TipoHormiga destino
        -Hormiga evolucion
        +getDestino() TipoHormiga
        +getEvolucion() Optional
        +comer(Alimento) boolean
    }
    class HSoldado
    class HZangano
    class HRastreadora
    class TipoHormiga {
        <<enumeration>>
        SOLDADO
        ZANGANO
        RASTREADORA
        +alimento() Alimento
        +crear(Integer, String) Hormiga
        +esSuAlimento(Alimento) boolean
        +paraGrupo(int) TipoHormiga
        +porNombre(String) Optional
    }
    class Extremidad {
        <<enumeration>>
        ANTENA_IZQUIERDA
        ANTENA_DERECHA
        MANDIBULA
        PATA_DELANTERA_DERECHA
        PATA_DELANTERA_IZQUIERDA
        PATA_MEDIA_DERECHA
        PATA_MEDIA_IZQUIERDA
        PATA_TRASERA_DERECHA
        PATA_TRASERA_IZQUIERDA
        +anatomia() List
        +esPata() boolean
    }
    class Alimento {
        <<abstract>>
        -String nombre
        +getNombre() String
        +esDelMismoTipo(Alimento) boolean
    }
    class ANectar
    class ACarnivoro
    class AOmnivoro
    class AHerbivoro
    class FabricaAlimentos {
        +todos() List
        +desde(String) Alimento
        +normalizar(String) String
    }

    IHormiga <|.. Hormiga
    Hormiga <|-- HLarva
    Hormiga <|-- HSoldado
    Hormiga <|-- HZangano
    Hormiga <|-- HRastreadora
    Hormiga "1" o-- "9" Extremidad
    HLarva --> TipoHormiga : destino
    TipoHormiga ..> Hormiga : crea
    TipoHormiga --> Alimento : alimento vital
    Hormiga ..> Alimento : come
    Alimento <|-- ANectar
    Alimento <|-- ACarnivoro
    Alimento <|-- AOmnivoro
    Alimento <|-- AHerbivoro
    FabricaAlimentos ..> Alimento : crea
```

## 2. Aplicación · AntCiberDron (HU-07 a HU-10)

El `AntCiberDron` lo pilota una hormiga adulta, implementa `IIA` (`buscar`), lleva armas en las patas delanteras (metralleta a la derecha, láser a la izquierda), vuela con el turbo reactor de la espalda y entrena idiomas con la ayuda de un `ExpertoParlante`.

```mermaid
classDiagram
    direction TB

    class IIA {
        <<interface>>
        +buscar(String) boolean
    }
    class AntCiberDron {
        -Hormiga piloto
        -Bomba bomba
        -List~CoordenadaUK~ coordenadas
        -FuentePoder fuente
        -TurboReactor turbo
        -ModuloIdiomas idiomas
        +buscar(String) boolean
        +buscarObjetivos() List
        +equipar(Extremidad, Arma) void
        +equiparMetralleta(Metralleta) void
        +equiparLaser(Laser) void
        +dispararMetralleta() String
        +dispararLaser() String
        +potenciaLaser() double
        +volar(int) String
        +recargarFuente() String
        +reemplazarFuente(FuentePoder) String
        +aprender(ExpertoParlante, Idioma, AspectoLinguistico, String) void
    }
    class Arma {
        <<interface>>
        +getNombre() String
        +disparar(FuentePoder) String
    }
    class Metralleta {
        -int municion
        +disparar(FuentePoder) String
    }
    class Laser {
        -double potenciaMaxima
        +potencia(FuentePoder) double
        +disparar(FuentePoder) String
    }
    class TurboReactor {
        -double consumoPorMinuto
        +volar(int, FuentePoder) String
    }
    class FuentePoder {
        -double capacidad
        -double energia
        +consumir(double) void
        +porcentaje() double
    }
    class ModuloIdiomas {
        +entrenar(ExpertoParlante, Idioma, AspectoLinguistico, String) void
        +conocimiento(Idioma, AspectoLinguistico) List
        +dominio(Idioma) int
        +puedeHablar(Idioma) boolean
    }
    class ExpertoParlante {
        -String nombre
        -Idioma idiomaNativo
    }
    class Idioma {
        <<enumeration>>
        INGLES
        ESPANOL
    }
    class AspectoLinguistico {
        <<enumeration>>
        LEXICO
        GRAMATICA
        FONETICA
    }
    class EntrenamientoNoAutorizadoException {
        <<exception>>
    }
    class Hormiga
    class Bomba
    class CoordenadaUK
    class Extremidad {
        <<enumeration>>
    }

    IIA <|.. AntCiberDron
    Arma <|.. Metralleta
    Arma <|.. Laser
    AntCiberDron --> Hormiga : piloto
    AntCiberDron --> Bomba
    AntCiberDron --> CoordenadaUK : busca en
    AntCiberDron *-- FuentePoder
    AntCiberDron *-- TurboReactor
    AntCiberDron *-- ModuloIdiomas
    AntCiberDron o-- Arma : en patas delanteras
    AntCiberDron ..> Extremidad
    Metralleta ..> FuentePoder
    Laser ..> FuentePoder
    TurboReactor ..> FuentePoder : consume
    ModuloIdiomas ..> ExpertoParlante : solo expertos
    ModuloIdiomas ..> Idioma
    ModuloIdiomas ..> AspectoLinguistico
    ModuloIdiomas ..> EntrenamientoNoAutorizadoException : lanza
    ExpertoParlante --> Idioma
```

## 3. Aplicación · Autómata, bomba y coordenadas (HU-03, HU-04, HU-06, HU-14)

`LenguajesOTAN` asocia el último dígito de la cédula con una coordenada y un lenguaje regular. `ConstructorAutomata` construye el AFD mínimo de la unión; `Bomba` lo usa para explotar solo en coordenadas objetivo con arsenal aceptado. `SistemaRuso.readCoord(fileName)` lee `Grupo##.csv` y devuelve `List<CoordenadaUK>`.

```mermaid
classDiagram
    direction LR

    class LenguajesOTAN {
        +expresion(int) String
        +digito(String) int
        +geoposicion(int) String
        +digitoObjetivo(String) int
        +pertenece(int, String) boolean
        +arsenal(char) String
    }
    class ConstructorAutomata {
        +desdeExpresion(String) Automata
        +union(List) Automata
        ~validar(String) String
        ~minimizar(Automata) Automata
    }
    class Automata {
        -int numeroEstados
        -Set~Character~ alfabeto
        -int inicial
        -Set~Integer~ finales
        +acepta(String) boolean
        +transicion(int, char) Integer
        +gramatica() Gramatica
        +descripcionFormal() String
        +estados() Set
        +transiciones() Map
    }
    class Gramatica {
        -Map producciones
        -String inicial
        +desde(Automata) Gramatica
        +noTerminal(int) String
        +getProducciones() Map
    }
    class ExportadorGrafo {
        +dot(Automata, String) String
        +mermaid(Automata) String
    }
    class AutomataMain {
        +main(String[]) void
        ~ejecutar(String[], PrintStream, PrintStream) int
    }
    class Bomba {
        -Automata automata
        -Set~Integer~ digitosObjetivo
        +evaluar(String) boolean
        +esObjetivo(String) boolean
        +debeExplotar(String, String) boolean
        +lenguaje() String
        +coordenadasObjetivo() List
        +getAutomata() Automata
    }
    class SistemaRuso {
        +readCoord(String) List
        +parsear(List) List
    }
    class CoordenadaUK {
        -String geoposicion
        -int digito
        -String tipoArsenal
        +getHorario(DiaSemana) String
    }
    class DiaSemana {
        <<enumeration>>
        LUNES
        MARTES
        MIERCOLES
        JUEVES
        VIERNES
    }
    class EstrategiaOTAN {
        +de(int) Entrada
    }
    class GeneradorCsvGrupo {
        +generar(List) String
        +escribir(Equipo, Path) Path
        ~palabra(String, Random) String
    }
    class Loading {
        +desdeSistema(PrintStream) Loading
        +cargar() void
    }
    class PresentadorCoordenadas {
        +presentar(List) void
    }

    LenguajesOTAN ..> ConstructorAutomata : expresiones
    ConstructorAutomata ..> Automata : crea
    Automata ..> Gramatica : gramatica
    Gramatica ..> Automata : desde
    ExportadorGrafo ..> Automata
    AutomataMain ..> Bomba
    AutomataMain ..> ExportadorGrafo
    Bomba *-- Automata
    Bomba ..> LenguajesOTAN
    SistemaRuso ..> CoordenadaUK : crea
    CoordenadaUK --> DiaSemana : horarios
    EstrategiaOTAN ..> DiaSemana
    EstrategiaOTAN ..> LenguajesOTAN
    GeneradorCsvGrupo ..> EstrategiaOTAN
    GeneradorCsvGrupo ..> LenguajesOTAN : palabra w
    PresentadorCoordenadas --> Loading
    PresentadorCoordenadas ..> CoordenadaUK
```

## 4. Aplicación · Configuración, autenticación y persistencia (HU-01, HU-02, HU-12, HU-15)

`MotorAntdron` es el punto central: lee `config/`, regenera `datos/Grupo##.csv`, la bomba y `datos/automata-grupo##.md` cada vez que se carga (arranque o «Recargar configuración»), y entrega todo a la consola y a la interfaz JavaFX.

```mermaid
classDiagram
    direction TB

    class App {
        +main(String[]) void
        +ejecutar(String) int
    }
    class MotorAntdron {
        -Path raiz
        -Equipo equipo
        -List~CoordenadaUK~ coordenadas
        -Bomba bomba
        -TipoHormiga tipoHormiga
        +cargar(Path) MotorAntdron
        +raizPorDefecto() Path
        +prepararPiloto() Piloto
        +crearAntCiberDron(Hormiga) AntCiberDron
        +hormiguero() HormigueroServicio
        +getAvisos() List
        +getBomba() Optional
        +getArchivoCoordenadas() Optional
        +getArchivoAutomata() Optional
    }
    class Piloto {
        <<record>>
        Hormiga hormiga
        List pasos
    }
    class Equipo {
        <<record>>
        int grupo
        String nombre
        String metafora
        List integrantes
        +grupoConFormato() String
        +archivoCoordenadas() String
        +cedulas() List
        +cedulasPendientes() int
        +buscarUsuario(String) Optional
    }
    class Integrante {
        <<record>>
        String cedula
        String nombre
        String usuario
        String clave
        String rol
        +tieneCedula() boolean
        +puedeIngresar() boolean
        +ultimoDigito() int
        +cedulaVisible() String
    }
    class LectorConfiguracion {
        +leer(Path) Equipo
        +parsearIntegrantes(List) List
    }
    class Claves {
        +sha256(String) String
        +coincide(String, String) boolean
    }
    class Autenticador {
        -int intentosRestantes
        +validar(String, String) boolean
        +intentar(String, String) Resultado
        +estaBloqueado() boolean
        +autenticar(EntradaUsuario, PrintStream) Optional
    }
    class EntradaUsuario {
        <<interface>>
        +leerLinea(String) String
        +leerClave(String) String
        +delSistema(PrintStream) EntradaUsuario
    }
    class PresentadorEquipo {
        +lineas(Equipo) List
    }
    class HormigueroServicio {
        +registrar(String, String) int
        +autenticar(String, String) Optional
        +nombreUsuario(int) String
        +crearHormiga(int, String) int
        +listar(int) List
        +alimentar(int, int, String) String
    }
    class HormigaRegistro {
        <<record>>
        int id
        String tipo
        String sexo
        String estado
        String creadaEn
    }
    class BaseDatos {
        ~enTransaccion(Trabajo) T
        ~getArchivo() Path
    }
    class GeneradorCsvGrupo
    class Bomba
    class AntCiberDron
    class TipoHormiga {
        <<enumeration>>
    }

    App --> MotorAntdron : consola y generar
    App --> Autenticador
    App --> PresentadorEquipo
    MotorAntdron --> LectorConfiguracion : lee config
    MotorAntdron *-- Equipo
    MotorAntdron ..> GeneradorCsvGrupo : regenera Grupo
    MotorAntdron --> Bomba : crea
    MotorAntdron ..> AntCiberDron : crea
    MotorAntdron --> TipoHormiga
    MotorAntdron --> HormigueroServicio
    MotorAntdron ..> Piloto
    LectorConfiguracion ..> Equipo : crea
    Equipo "1" *-- "0..6" Integrante
    Autenticador --> Equipo
    Autenticador ..> EntradaUsuario
    Autenticador ..> Claves
    Integrante ..> Claves
    PresentadorEquipo ..> Equipo
    HormigueroServicio *-- BaseDatos : hormiguero.db (SQLite)
    HormigueroServicio ..> HormigaRegistro : lista
    HormigueroServicio ..> Claves : hash con sal
    HormigueroServicio --> TipoHormiga
```

## 5. Aplicación · Interfaz JavaFX (sin FXML) y tablero XP

Las clases de la interfaz son de acceso de paquete (`ec.edu.epn.antdron.ui`) salvo `AntdronFx`. Cada pestaña de `VistaPrincipal` es un panel que recibe el `MotorAntdron`.

```mermaid
classDiagram
    direction TB

    class Application {
        <<JavaFX>>
    }
    class AntdronFx {
        -MotorAntdron motor
        +iniciar(String[]) void
        +start(Stage) void
        ~recargar() boolean
    }
    class VistaIngreso {
        ~getRaiz() Parent
    }
    class VistaPrincipal {
        ~getRaiz() Parent
    }
    class PanelEquipo {
        ~getRaiz() Parent
    }
    class PanelCoordenadas {
        ~alMostrar() void
        ~getRaiz() Parent
    }
    class PanelAutomata {
        ~getRaiz() Parent
    }
    class LienzoAutomata {
        ~posiciones(Automata) Map
    }
    class PanelDron {
        ~getRaiz() Parent
    }
    class VistaKgd {
        ~getRaiz() Parent
        ~getMotor() MotorAntdron
    }
    class PanelTablero {
        ~getRaiz() Parent
    }
    class TableroXp {
        +cargar(Path, Equipo)$ TableroXp
        +estado(Tarea) EstadoTarea
        +porIntegrante() List
        +porPractica() List
        +porIteracion() List
    }
    class Tarea {
        <<record>>
        String id
        String sesion
        String practica
        List responsables
        String artefacto
        String evidencia
    }
    class EstadoTarea {
        <<enumeration>>
        POR_HACER
        EN_CURSO
        HECHA
    }
    class Utility {
        +camposCsv(String)$ String[]
        +esLineaIgnorable(String, String)$ boolean
        +leerTexto(Path)$ String
        +normalizar(String)$ String
    }
    class Ui {
        ~estilos() String
        ~titulo(String) Label
        ~tarjeta(Node[]) VBox
        ~tabla(String) TableView
        ~error(String, String) void
    }
    class MotorAntdron
    class Autenticador
    class HormigueroServicio
    class AntCiberDron
    class Automata

    Application <|-- AntdronFx
    AntdronFx --> VistaIngreso : login
    AntdronFx --> VistaPrincipal : tras ingresar
    AntdronFx --> VistaKgd : usuario KGD
    AntdronFx --> MotorAntdron : recargar
    VistaIngreso --> Autenticador : 3 intentos
    VistaPrincipal *-- PanelEquipo
    VistaPrincipal *-- PanelCoordenadas
    VistaPrincipal *-- PanelAutomata
    VistaPrincipal *-- PanelDron
    VistaPrincipal *-- VistaKgd
    VistaPrincipal *-- PanelTablero
    PanelTablero --> TableroXp : xp/tareas.csv
    TableroXp *-- Tarea
    TableroXp ..> EstadoTarea
    TableroXp ..> Utility
    PanelAutomata --> LienzoAutomata
    LienzoAutomata ..> Automata
    PanelDron --> AntCiberDron
    VistaKgd --> HormigueroServicio
    PanelEquipo ..> Ui
    PanelCoordenadas ..> Ui
    PanelAutomata ..> Ui
    PanelDron ..> Ui
    VistaKgd ..> Ui
    PanelTablero ..> Ui
    VistaIngreso ..> Ui
```

`Utility` (paquete `pkAntCiberDronContract`) es el resultado del **reciclaje**: reúne el manejo de líneas CSV con `;`, la lectura de archivos editados en Windows y la normalización de textos que antes estaban repetidos. La usan `LectorConfiguracion`, `SistemaRuso`, `FabricaAlimentos`, `HormigueroServicio` (importación de los CSV anteriores) y `TableroXp`.

## 6. Framework XP (`framework-xp/`)

El núcleo (`XpCli`) ejecuta cualquier `ComandoXP`; cada módulo aporta sus comandos (`directrices`, `verificar`, `backlog`, `plan`, `velocidad`, `equipo`, `parejas`, `crc`, `aceptacion`). El ejecutor de pruebas propio (`EjecutorPruebas`) reemplaza a JUnit y alimenta el reporte de aceptación por historia.

```mermaid
classDiagram
    direction TB

    class XpCli {
        +main(String[]) void
        +ejecutar(String[], PrintStream, PrintStream) int
        +nombresDeComandos() List
    }
    class ComandoXP {
        <<interface>>
        +nombre() String
        +descripcion() String
        +ejecutar(List, ContextoXP) int
    }
    class ContextoXP {
        +cargar(Path, PrintStream, PrintStream) ContextoXP
        +propiedad(String, String) String
        +ruta(String, String) Path
        +artefacto(Practica) Optional
        +relativa(Path) String
    }
    class Practica {
        <<enumeration>>
        +getFase() Fase
        +getNombre() String
        +getResponsable() RolXP
        +getRegla() String
        +getArtefactoPorDefecto() Optional
        +deFase(Fase) List
    }
    class Fase {
        <<enumeration>>
        PLANIFICACION
        DISENO
        DESARROLLO
        PRUEBAS
    }
    class RolXP {
        <<enumeration>>
        +getEtiqueta() String
        +getResponsabilidad() String
        +desde(String) RolXP
    }
    class ComandoDirectrices
    class ComandoVerificar {
        +verificar(ContextoXP, List) List
    }
    class ComandoBacklog
    class ComandoPlan
    class ComandoVelocidad
    class ComandoEquipo
    class ComandoParejas
    class ComandoCrc
    class ComandoAceptacion

    XpCli o-- ComandoXP : descubre
    ComandoXP <|.. ComandoDirectrices
    ComandoXP <|.. ComandoVerificar
    ComandoXP <|.. ComandoBacklog
    ComandoXP <|.. ComandoPlan
    ComandoXP <|.. ComandoVelocidad
    ComandoXP <|.. ComandoEquipo
    ComandoXP <|.. ComandoParejas
    ComandoXP <|.. ComandoCrc
    ComandoXP <|.. ComandoAceptacion
    ComandoXP ..> ContextoXP
    ContextoXP ..> Practica
    Practica --> Fase
    Practica --> RolXP
    ComandoVerificar ..> Practica
    ComandoDirectrices ..> Practica
```

### 6.1 Planificación, seguimiento, desarrollo y diseño

```mermaid
classDiagram
    direction LR

    class LectorBacklog {
        +leer(Path) List
        +parsear(List) List
    }
    class HistoriaUsuario {
        -String id
        -String titulo
        -int puntos
        -int iteracion
        +enunciado() String
        +conEstado(EstadoHistoria) HistoriaUsuario
        +estaTerminada() boolean
    }
    class Prioridad {
        <<enumeration>>
        ALTA
        MEDIA
        BAJA
    }
    class EstadoHistoria {
        <<enumeration>>
    }
    class PlanEntregas {
        +agrupar(List) List
        +proponer(List, int, int) List
    }
    class Iteracion {
        +getNumero() int
        +puntosPlanificados() int
        +puntosCompletados() int
        +avance() int
        +estaCerrada() boolean
    }
    class Velocidad {
        +climaDeAyer(List) int
        +promedio(List) double
    }
    class LectorEquipo {
        +leer(Path) Equipo
        +parsearIntegrantes(List) List
    }
    class Equipo {
        +numeroConFormato() String
        +buscarPorUsuario(String) Optional
    }
    class Integrante {
        -String cedula
        -String nombre
        -String usuario
        +tieneCedula() boolean
    }
    class RotacionParejas {
        +totalRondas() int
        +parejas(int) List
    }
    class RevisorCRC {
        +tarjetas(List) Set
        +clases(Path) Set
        +revisar(Path, Path) Revision
    }
    class RolXP {
        <<enumeration>>
    }

    LectorBacklog ..> HistoriaUsuario : crea
    HistoriaUsuario --> Prioridad
    HistoriaUsuario --> EstadoHistoria
    PlanEntregas ..> Iteracion : crea
    Iteracion "1" o-- "*" HistoriaUsuario
    Velocidad ..> Iteracion
    LectorEquipo ..> Equipo : crea
    Equipo "1" *-- "*" Integrante
    Integrante --> RolXP
    RotacionParejas ..> Integrante : nombres
```

### 6.2 Pruebas y aceptación (XP-05)

```mermaid
classDiagram
    direction LR

    class Prueba {
        <<annotation>>
    }
    class Historia {
        <<annotation>>
    }
    class AntesDeCada {
        <<annotation>>
    }
    class DespuesDeCada {
        <<annotation>>
    }
    class EjecutorPruebas {
        +main(String[]) void
        +ejecutarDirectorio(Path) List
        +ejecutar(Class) List
        +registrar(List, Path) void
    }
    class ResultadoPrueba {
        <<record>>
        String clase
        String metodo
        List historias
        boolean paso
    }
    class Afirmar {
        +igual(Object, Object) void
        +verdadero(boolean) void
        +falso(boolean) void
        +lanza(Class, Ejecutable) Throwable
    }
    class Temporal {
        +directorio() Path
    }
    class ReporteAceptacion {
        +resumir(List) Map
        +markdown(Map, List) String
        +generar(Path, List) String
    }
    class Estado {
        <<enumeration>>
        ACEPTADA
        RECHAZADA
        SIN_PRUEBAS
    }
    class ComandoAceptacion
    class HistoriaUsuario
    class LectorBacklog

    EjecutorPruebas ..> Prueba : lee
    EjecutorPruebas ..> Historia : lee
    EjecutorPruebas ..> AntesDeCada
    EjecutorPruebas ..> DespuesDeCada
    EjecutorPruebas ..> ResultadoPrueba : crea
    EjecutorPruebas ..> Temporal : limpia
    ReporteAceptacion ..> Estado
    ReporteAceptacion ..> HistoriaUsuario
    ComandoAceptacion ..> ReporteAceptacion
    ComandoAceptacion ..> LectorBacklog
```
