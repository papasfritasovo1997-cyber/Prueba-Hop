# Tarjetas CRC (Clase · Responsabilidades · Colaboradores)

**Responsable:** Integrante 5 (Consultor) · **Práctica XP:** Tarjetas CRC

Hay una tarjeta por cada clase, interfaz, enumeración, registro y excepción de `antdron/src`. El encabezado de cada tarjeta debe ser exactamente `### Clase: NombreDeLaClase`: el comando `xp crc` lo usa para listar las clases sin tarjeta y las tarjetas de clases que ya no existen. Si se agrega o se borra una clase, se actualiza este archivo en la misma pareja.

La metáfora que ordena las tarjetas está en `xp/metafora.md`.

## Paquete `ec.edu.epn.antdron` (arranque)

### Clase: App

| Responsabilidades | Colaboradores |
|---|---|
| Punto de entrada: elige el modo (`gui`, `consola`, `generar`, `automata`) | MotorAntdron |
| Modo consola: login, equipo, coordenadas con loading, bomba y simulación del AntCiberDron | Autenticador, PresentadorEquipo, PresentadorCoordenadas, AntCiberDron |
| Devuelve códigos de salida (0 bien, 1 modo desconocido, 2 error) | AutomataMain, AntdronFx |

### Clase: MotorAntdron

| Responsabilidades | Colaboradores |
|---|---|
| Cargar `config/` y regenerar `datos/Grupo##.csv`, la bomba y `datos/automata-grupo##.md` | LectorConfiguracion, GeneradorCsvGrupo, SistemaRuso, Bomba |
| Acumular avisos (cédulas vacías, grupo sin tipo de hormiga) | Equipo |
| Preparar al piloto (HLarva que se transforma) y crear el AntCiberDron con sus armas | TipoHormiga, HLarva, AntCiberDron, FuentePoder, TurboReactor |
| Entregar el servicio del hormiguero virtual | HormigueroServicio |

## Paquete `config`

### Clase: LectorConfiguracion

| Responsabilidades | Colaboradores |
|---|---|
| Leer `config/equipo.properties` y `config/integrantes.csv` | Equipo, Integrante |
| Aceptar cédulas vacías y avisar con claridad si el archivo está mal escrito | MotorAntdron |

### Clase: Equipo

| Responsabilidades | Colaboradores |
|---|---|
| Guardar grupo, nombre, metáfora e integrantes | Integrante |
| Dar el nombre `Grupo##.csv`, las cédulas registradas y cuántas faltan | GeneradorCsvGrupo, Bomba |
| Buscar a un integrante por usuario | Autenticador |

### Clase: Integrante

| Responsabilidades | Colaboradores |
|---|---|
| Guardar cédula, nombre, usuario, clave y rol de un integrante | Equipo |
| Saber si tiene cédula y cuál es su último dígito | LenguajesOTAN |
| Mostrar la cédula o «(cédula pendiente)» | PresentadorEquipo |

### Clase: Claves

| Responsabilidades | Colaboradores |
|---|---|
| Calcular SHA-256 de un texto | HormigueroServicio |
| Comparar una clave escrita con la guardada (texto tal cual o `sha256:`) | Integrante, Autenticador |

## Paquete `auth`

### Clase: Autenticador

| Responsabilidades | Colaboradores |
|---|---|
| Validar usuario y clave (integrantes y profesor `patmic`) | Equipo, Claves |
| Contar los intentos y bloquear al tercer error | EntradaUsuario |
| Dar el nombre visible del usuario | VistaIngreso, App |

### Clase: EntradaUsuario

| Responsabilidades | Colaboradores |
|---|---|
| Abstraer la lectura de una línea y de una clave (consola o pruebas) | Autenticador |
| Ocultar la clave cuando hay consola real | App |

### Clase: PresentadorEquipo

| Responsabilidades | Colaboradores |
|---|---|
| Armar las líneas `[+] TEAM [N]: nombre` y `Alumno i: cédula, nombre` | Equipo, Integrante |
| Mostrarlas igual en consola y en pruebas | App |

## Paquete `hormiga`

### Clase: Hormiga

| Responsabilidades | Colaboradores |
|---|---|
| Ser la hormiga abstracta con id, sexo, estado y extremidades | IHormiga, Extremidad |
| Decidir si vive o muere al comer | Alimento |
| Obligar a cada subclase a decir su tipo y su alimento vital | HSoldado, HZangano, HRastreadora, HLarva |

### Clase: HLarva

| Responsabilidades | Colaboradores |
|---|---|
| Nacer como larva y vivir con néctar | ANectar |
| Transformarse en el tipo de su destino al comer su alimento | TipoHormiga, Alimento |
| Entregar la hormiga adulta en que se transformó | Hormiga |

### Clase: HSoldado

| Responsabilidades | Colaboradores |
|---|---|
| Representar al soldado (grupos 1-2) | Hormiga |
| Vivir solo con alimento carnívoro | ACarnivoro |

### Clase: HZangano

| Responsabilidades | Colaboradores |
|---|---|
| Representar al zángano (grupos 3-4) | Hormiga |
| Vivir solo con alimento omnívoro | AOmnivoro |

### Clase: HRastreadora

| Responsabilidades | Colaboradores |
|---|---|
| Representar a la rastreadora (grupo 5) | Hormiga |
| Vivir solo con alimento herbívoro | AHerbivoro |

### Clase: TipoHormiga

| Responsabilidades | Colaboradores |
|---|---|
| Relacionar el número de grupo con el tipo de hormiga | Equipo, MotorAntdron |
| Dar el alimento y crear la hormiga adulta de cada tipo | Alimento, Hormiga |
| Buscar un tipo por nombre (al leer los CSV) | HormigueroServicio |

### Clase: Extremidad

| Responsabilidades | Colaboradores |
|---|---|
| Listar las partes del cuerpo (antenas, mandíbula, seis patas) | Hormiga, AntCiberDron |
| Decir si una extremidad es pata | AntCiberDron |

## Paquete `alimento`

### Clase: Alimento

| Responsabilidades | Colaboradores |
|---|---|
| Ser el alimento abstracto con nombre | Hormiga, HLarva |
| Comparar si dos alimentos son del mismo tipo | TipoHormiga |

### Clase: ANectar

| Responsabilidades | Colaboradores |
|---|---|
| Ser el néctar, el alimento de la larva | HLarva |

### Clase: ACarnivoro

| Responsabilidades | Colaboradores |
|---|---|
| Ser el alimento del soldado | HSoldado |

### Clase: AOmnivoro

| Responsabilidades | Colaboradores |
|---|---|
| Ser el alimento del zángano | HZangano |

### Clase: AHerbivoro

| Responsabilidades | Colaboradores |
|---|---|
| Ser el alimento de la rastreadora | HRastreadora |

### Clase: FabricaAlimentos

| Responsabilidades | Colaboradores |
|---|---|
| Crear un alimento a partir de su nombre (con o sin tildes) | Alimento, HormigueroServicio |
| Listar todos los alimentos disponibles | VistaKgd |

## Paquete `pkAntCiberDronContract`

### Clase: IHormiga

| Responsabilidades | Colaboradores |
|---|---|
| Declarar que una hormiga puede comer un alimento (`comer`) | Hormiga, Alimento |

### Clase: IIA

| Responsabilidades | Colaboradores |
|---|---|
| Declarar que la IA puede buscar un tipo de arsenal (`buscar`) | AntCiberDron |

## Paquete `antciberdron`

### Clase: AntCiberDron

| Responsabilidades | Colaboradores |
|---|---|
| Buscar el tipo de arsenal y presentar Geoposición, Tipo Arsenal y Acción | Bomba, CoordenadaUK |
| Llevar la metralleta (pata delantera derecha) y el láser (izquierda) | Arma, Metralleta, Laser, Extremidad |
| Volar con el turbo reactor y gestionar la fuente de poder | TurboReactor, FuentePoder |
| Aprender idiomas con un experto | ModuloIdiomas, ExpertoParlante |
| Ser pilotado por una hormiga adulta | Hormiga |

### Clase: Arma

| Responsabilidades | Colaboradores |
|---|---|
| Declarar el contrato de un arma: nombre y disparo | FuentePoder, AntCiberDron |

### Clase: Metralleta

| Responsabilidades | Colaboradores |
|---|---|
| Disparar gastando munición | Arma |
| Ir solo en la pata delantera derecha | AntCiberDron |

### Clase: Laser

| Responsabilidades | Colaboradores |
|---|---|
| Disparar con potencia proporcional a la energía disponible | FuentePoder, Arma |
| Ir en la pata delantera izquierda | AntCiberDron |

### Clase: TurboReactor

| Responsabilidades | Colaboradores |
|---|---|
| Hacer volar al dron consumiendo energía por minuto | FuentePoder |
| Estar ubicado en la espalda | AntCiberDron |

### Clase: FuentePoder

| Responsabilidades | Colaboradores |
|---|---|
| Guardar la energía y la capacidad | AntCiberDron |
| Consumir energía y dar el porcentaje restante | TurboReactor, Laser |
| Ser recargada o reemplazada por la IIA | AntCiberDron |

### Clase: ModuloIdiomas

| Responsabilidades | Colaboradores |
|---|---|
| Registrar léxico, gramática y fonética por idioma | Idioma, AspectoLinguistico |
| Aceptar entrenamiento solo de un experto del idioma | ExpertoParlante, EntrenamientoNoAutorizadoException |
| Decir el dominio y si el dron puede hablar un idioma | AntCiberDron |

### Clase: ExpertoParlante

| Responsabilidades | Colaboradores |
|---|---|
| Representar a un experto con su idioma nativo | Idioma, ModuloIdiomas |

### Clase: Idioma

| Responsabilidades | Colaboradores |
|---|---|
| Enumerar los idiomas (inglés y español) | ModuloIdiomas, ExpertoParlante |

### Clase: AspectoLinguistico

| Responsabilidades | Colaboradores |
|---|---|
| Enumerar los aspectos a entrenar (léxico, gramática, fonética) | ModuloIdiomas |

### Clase: EntrenamientoNoAutorizadoException

| Responsabilidades | Colaboradores |
|---|---|
| Avisar que quien entrena no es experto del idioma | ModuloIdiomas, PanelDron |

## Paquete `automata`

### Clase: LenguajesOTAN

| Responsabilidades | Colaboradores |
|---|---|
| Dar la expresión regular de cada dígito (Coord-00 a Coord-09) | ConstructorAutomata |
| Convertir dígito, geoposición y cédula entre sí | Integrante, Bomba |
| Decir si una palabra pertenece al lenguaje de un dígito y traducir los símbolos a arsenales | GeneradorCsvGrupo, PanelAutomata |

### Clase: Automata

| Responsabilidades | Colaboradores |
|---|---|
| Representar el AFD mínimo A = (Q, Σ, δ, q0, F) | ConstructorAutomata |
| Decir si acepta una palabra | Bomba |
| Describirse formalmente y producir su gramática | Gramatica, MotorAntdron |

### Clase: ConstructorAutomata

| Responsabilidades | Colaboradores |
|---|---|
| Convertir expresiones regulares en AFN-ε y unirlas | LenguajesOTAN |
| Determinizar por subconjuntos y minimizar con Moore sin sumidero | Automata |
| Rechazar expresiones mal formadas | Bomba |

### Clase: Gramatica

| Responsabilidades | Colaboradores |
|---|---|
| Derivar la gramática regular por la derecha desde las transiciones | Automata |
| Imprimir las producciones | MotorAntdron, AutomataMain, PanelAutomata |

### Clase: ExportadorGrafo

| Responsabilidades | Colaboradores |
|---|---|
| Exportar el autómata a Mermaid y a Graphviz (DOT) | Automata |
| Alimentar el documento generado y la consola | MotorAntdron, AutomataMain |

### Clase: AutomataMain

| Responsabilidades | Colaboradores |
|---|---|
| Imprimir lenguaje, definición formal, gramática, DOT y Mermaid para cédulas dadas | Bomba, ExportadorGrafo |
| Validar las cédulas recibidas | App |

## Paquete `bomba`

### Clase: Bomba

| Responsabilidades | Colaboradores |
|---|---|
| Calcular las coordenadas objetivo desde los últimos dígitos | LenguajesOTAN, Integrante |
| Construir el autómata de la unión de lenguajes | ConstructorAutomata, Automata |
| Explotar solo si la coordenada es objetivo y el autómata acepta el arsenal | AntCiberDron, PanelAutomata |

## Paquete `sistema`

### Clase: GeneradorCsvGrupo

| Responsabilidades | Colaboradores |
|---|---|
| Escribir `datos/Grupo##.csv` con una coordenada por dígito | Equipo, EstrategiaOTAN |
| Generar una palabra `w` del lenguaje de cada coordenada | LenguajesOTAN |

### Clase: SistemaRuso

| Responsabilidades | Colaboradores |
|---|---|
| Implementar `readCoord(fileName)` que lee el CSV | CoordenadaUK |
| Dar error claro con la línea mala | MotorAntdron |

### Clase: CoordenadaUK

| Responsabilidades | Colaboradores |
|---|---|
| Guardar geoposición, horarios por día y tipo de arsenal | DiaSemana |
| Dar el dígito y el horario de un día | PresentadorCoordenadas, PanelCoordenadas |

### Clase: DiaSemana

| Responsabilidades | Colaboradores |
|---|---|
| Enumerar los días hábiles de lunes a viernes con su etiqueta | CoordenadaUK, EstrategiaOTAN |

### Clase: EstrategiaOTAN

| Responsabilidades | Colaboradores |
|---|---|
| Guardar la tabla de la estrategia: día y horario de cada coordenada | DiaSemana, LenguajesOTAN |
| Entregar la fila de un dígito | GeneradorCsvGrupo |

### Clase: Loading

| Responsabilidades | Colaboradores |
|---|---|
| Animar un cursor giratorio hasta 100 % en la consola | PresentadorCoordenadas |
| Permitir tiempos cortos para las pruebas | App |

### Clase: PresentadorCoordenadas

| Responsabilidades | Colaboradores |
|---|---|
| Mostrar la tabla Loading, Geoposición, días y Tipo Arsenal | CoordenadaUK, Loading |

## Paquete `persistencia`

### Clase: HormigueroServicio

| Responsabilidades | Colaboradores |
|---|---|
| Registrar usuarios con clave con sal y hash y crear su hormiguero | BaseDatos, Claves |
| Crear hormigas HLarva y listarlas por usuario | HormigaRegistro, HLarva |
| Alimentar aplicando las reglas del dominio y guardar el historial | Hormiga, FabricaAlimentos, TipoHormiga |
| Importar una vez los CSV de la versión anterior | BaseDatos |

### Clase: BaseDatos

| Responsabilidades | Colaboradores |
|---|---|
| Crear `hormiguero.db` y sus tablas con `esquema.sql` si no existen | HormigueroServicio |
| Ejecutar cada operación en una transacción con las claves foráneas activas | Controlador sqlite-jdbc |

### Clase: HormigaRegistro

| Responsabilidades | Colaboradores |
|---|---|
| Guardar una fila de hormiga (id, tipo, sexo, estado, fecha) para mostrarla | HormigueroServicio, VistaKgd |

## Paquete `ui` (JavaFX sin FXML)

### Clase: AntdronFx

| Responsabilidades | Colaboradores |
|---|---|
| Abrir la ventana JavaFX y cambiar entre las vistas | VistaIngreso, VistaPrincipal |
| Recargar el motor cuando cambian las cédulas | MotorAntdron |

### Clase: VistaIngreso

| Responsabilidades | Colaboradores |
|---|---|
| Pedir usuario y clave y mostrar los intentos restantes | Autenticador |
| Entrar a la vista principal o al hormiguero KGD | AntdronFx, VistaKgd |

### Clase: VistaPrincipal

| Responsabilidades | Colaboradores |
|---|---|
| Armar las pestañas: Equipo, Coordenadas, Bomba y autómata, AntCiberDron y Hormiguero virtual | PanelEquipo, PanelCoordenadas, PanelAutomata, PanelDron, VistaKgd |
| Ofrecer «Recargar configuración», cerrar sesión y salir | AntdronFx |

### Clase: PanelEquipo

| Responsabilidades | Colaboradores |
|---|---|
| Mostrar el grupo, el nombre y la tabla de integrantes | MotorAntdron, Integrante |
| Explicar dónde editar las cédulas | Ui |

### Clase: PanelCoordenadas

| Responsabilidades | Colaboradores |
|---|---|
| Presentar las coordenadas con un loading giratorio hasta 100 % por línea | MotorAntdron, CoordenadaUK |
| Avisar si aún no hay cédulas | Ui |

### Clase: PanelAutomata

| Responsabilidades | Colaboradores |
|---|---|
| Mostrar lenguaje, definición formal, gramática y grafo | Bomba, Automata, LienzoAutomata |
| Probar si una palabra `w` pertenece a L | Bomba |

### Clase: LienzoAutomata

| Responsabilidades | Colaboradores |
|---|---|
| Dibujar los estados y las transiciones del autómata en un lienzo | Automata |
| Calcular la posición de cada estado | PanelAutomata |

### Clase: PanelDron

| Responsabilidades | Colaboradores |
|---|---|
| Ofrecer IA buscar arsenal, armas, vuelo, energía e idiomas | AntCiberDron, MotorAntdron |
| Mostrar los errores de entrenamiento no autorizado | ExpertoParlante, EntrenamientoNoAutorizadoException |

### Clase: VistaKgd

| Responsabilidades | Colaboradores |
|---|---|
| Registrar e ingresar a usuarios KGD | HormigueroServicio |
| Crear hormigas y alimentarlas, mostrando su estado | HormigaRegistro, FabricaAlimentos |

### Clase: Ui

| Responsabilidades | Colaboradores |
|---|---|
| Dar estilos y fábricas de etiquetas, tarjetas, tablas y diálogos | Panel* y Vista* |
