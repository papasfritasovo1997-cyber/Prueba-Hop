# Metáfora del sistema · «Hormiguero de guerra»

**Responsable:** Integrante 5 (Consultor) · **Práctica XP:** Metáfora del sistema

> Cada módulo del sistema es una hormiga especializada que coopera con las demás para que el **AntCiberDron** cumpla su misión. La colonia obedece a una reina que no vemos —el archivo de configuración— y todo lo demás se organiza a partir de lo que ella pone en el nido.

La misma frase está en `config/equipo.properties` (clave `metafora`) y la muestra la aplicación en la pestaña «Equipo».

## Por qué una metáfora

La metáfora da un vocabulario común: cuando alguien dice «la exploradora no encontró nada» todo el equipo entiende que la búsqueda de arsenal devolvió `false`. Sirve para nombrar clases nuevas y para decidir dónde poner una responsabilidad: si es de una hormiga que ya existe, va en su módulo; si no, hace falta un rol nuevo en la colonia.

## Roles de la colonia

| En el hormiguero | En el sistema | Módulo / clases | Qué hace |
|---|---|---|---|
| **La reina** (da las órdenes) | Configuración del equipo | `config/equipo.properties`, `config/integrantes.csv`, `LectorConfiguracion`, `Equipo`, `Integrante` | Define el grupo y las cédulas. Sin tocar código, lo que ella escribe cambia el comportamiento de toda la colonia. |
| **La partera** | Motor de arranque | `MotorAntdron` | Recibe las órdenes de la reina y prepara el nido: coordenadas, bomba, autómata y piloto. Se vuelve a ejecutar cada vez que se recarga la configuración. |
| **La centinela** (vigila la entrada) | Autenticación | `Autenticador`, `EntradaUsuario`, `Claves` | Deja pasar solo a quien conoce la contraseña; tres intentos y se cierra la puerta. |
| **La cronista** | Presentación del equipo | `PresentadorEquipo`, `PanelEquipo` | Cuenta quién forma la colonia. |
| **La cartógrafa** | Coordenadas | `GeneradorCsvGrupo`, `SistemaRuso`, `CoordenadaUK`, `EstrategiaOTAN`, `Loading`, `PresentadorCoordenadas` | Dibuja el mapa de Ucrania: una coordenada por dígito, con su horario y su palabra. |
| **La estratega** (reconoce patrones) | Autómata | `Automata`, `ConstructorAutomata`, `Gramatica`, `LenguajesOTAN`, `ExportadorGrafo` | Reconoce el patrón del arsenal enemigo (a, b, c, d, t) y dice «sí» o «no». |
| **La soldado de la bomba** | Bomba | `Bomba` | Solo explota si la coordenada es objetivo **y** la estratega reconoce el arsenal. |
| **La exploradora** | IA | `IIA`, `AntCiberDron.buscar` | Sale a buscar el tipo de arsenal y vuelve con `true` o `false`. |
| **La guerrera** | Armas y vuelo | `Arma`, `Metralleta`, `Laser`, `TurboReactor`, `FuentePoder` | Ataca por tierra con las patas delanteras y por aire con el turbo reactor, gastando energía. |
| **La intérprete** | Idiomas | `ModuloIdiomas`, `ExpertoParlante`, `Idioma`, `AspectoLinguistico` | Aprende inglés y español, pero solo de un experto del idioma. |
| **Las obreras** | Hormigas | `Hormiga`, `HLarva`, `HSoldado`, `HZangano`, `HRastreadora`, `TipoHormiga`, `Extremidad` | La colonia misma. Todas nacen como larva y se transforman al comer. |
| **La despensa** | Alimentos | `Alimento`, `ANectar`, `ACarnivoro`, `AOmnivoro`, `AHerbivoro`, `FabricaAlimentos` | Cada tipo de hormiga vive solo con su alimento. |
| **La archivista** (memoria de la colonia) | Persistencia | `HormigueroServicio`, `TablaCsv`, `HormigaRegistro` | Guarda en CSV quién tiene qué hormigas y qué comieron. |
| **La atalaya** | Interfaz JavaFX | `AntdronFx`, `VistaIngreso`, `VistaPrincipal`, `Panel*`, `VistaKgd`, `Ui` | Desde ella los humanos miran el hormiguero y le dan órdenes. |

## Reglas de convivencia

1. **La reina manda:** nada de cédulas, grupos o nombres escritos en el código; todo sale de `config/`.
2. **Una hormiga, un oficio:** cada clase tiene una responsabilidad (ver `xp/tarjetas-crc.md`).
3. **Las hormigas se hablan por feromonas simples:** los módulos se comunican con interfaces pequeñas (`IHormiga`, `IIA`, `Arma`, `EntradaUsuario`).
4. **La colonia no depende del entrenador:** la aplicación no importa el framework XP; el framework solo observa el nido desde fuera.
5. **Lo que el nido genera se regenera:** `datos/Grupo##.csv` y `datos/automata-grupo##.md` no se editan a mano.

## La metáfora y el enunciado

| Enunciado | En la metáfora |
|---|---|
| `TIPO_HORMIGA` según el grupo | La reina decide qué casta de hormiga cría la colonia (1-2 soldado, 3-4 zángano, 5 rastreadora). |
| La larva se transforma al comer | La larva vive con néctar y se vuelve adulta con el alimento de su casta. |
| El AntCiberDron pilotado por una hormiga | La obrera adulta sube al dron y la exploradora (IA) guía la misión. |
| La bomba que explota solo en el arsenal definido | La soldado de la bomba solo ataca con permiso de la estratega. |
