<!-- Generado con: java -cp antdron/target/antdron.jar ec.edu.epn.xp.cli.XpCli crc <clases...> -->
# Tarjetas CRC

### Clase: AntCiberDron

| Responsabilidades | Colaboradores |
|---|---|
| Buscar el tipo de arsenal (IIA) | Hormiga |
| Detonar la bomba solo en objetivos | Bomba |
| Cargar metralleta y láser | CoordenadaUK |
| Volar con el turbo reactor | FuentePoder |
| Recargar/reemplazar la fuente de poder | Arma |
| Aprender inglés y español | TurboReactor |
|  | ModuloIdiomas |

### Clase: Bomba

| Responsabilidades | Colaboradores |
|---|---|
| Conocer las coordenadas objetivo | Automata |
| Decidir si explota según el autómata | ConstructorAutomata |
|  | EstrategiaOTAN |
|  | CoordenadaUK |

### Clase: Hormiga

| Responsabilidades | Colaboradores |
|---|---|
| Conocer id, sexo y estado | Alimento |
| Comer y decidir si vive | Extremidad |
| Tener extremidades |  |

### Clase: SistemaRuso

| Responsabilidades | Colaboradores |
|---|---|
| Leer Grupo##.csv | CoordenadaUK |
| Validar cada coordenada | DiaSemana |

### Clase: Autenticador

| Responsabilidades | Colaboradores |
|---|---|
| Validar usuario y contraseña | Equipo |
| Limitar a 3 intentos | Credenciales |
|  | EntradaUsuario |

### Clase: HormigueroServicio

| Responsabilidades | Colaboradores |
|---|---|
| Registrar y autenticar usuarios KGD | BaseDatos |
| Crear hormigas en el hormiguero | HLarva |
| Alimentar hormigas y guardar el resultado | TipoHormiga |
|  | FabricaAlimentos |

### Clase: ServidorWeb

| Responsabilidades | Colaboradores |
|---|---|
| Mostrar login, registro y hormiguero | HormigueroServicio |
| Manejar la sesión del usuario KGD | Html |

