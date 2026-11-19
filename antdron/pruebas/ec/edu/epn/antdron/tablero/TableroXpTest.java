package ec.edu.epn.antdron.tablero;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ec.edu.epn.antdron.MotorAntdron;
import ec.edu.epn.antdron.config.Equipo;
import ec.edu.epn.antdron.config.Integrante;
import ec.edu.epn.antdron.config.LectorConfiguracion;
import ec.edu.epn.xp.directrices.Practica;
import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Historia;
import ec.edu.epn.xp.pruebas.Prueba;
import ec.edu.epn.xp.pruebas.Temporal;

/** Tablero de trabajo XP: las tareas de la división por fases, su estado y el avance del equipo. */
@Historia("XP-03")
class TableroXpTest {

    private static final Equipo EQUIPO = new Equipo(4, "Pragmatics", "", List.of(
            new Integrante("", "Sebastián Jibaja", "sjibaja", "1234", "ENTRENADOR"),
            new Integrante("", "Karen Mosquera", "kmosquera", "1234", "CLIENTE")));

    private static final String CABECERA = "id;sesion;practica;responsables;tarea;artefacto;evidencia;estado\n";

    private static Path proyecto(String tareas) throws Exception {
        Path raiz = Temporal.directorio();
        Files.createDirectories(raiz.resolve("xp"));
        Files.writeString(raiz.resolve("xp/tareas.csv"), "# comentario\n" + CABECERA + tareas);
        return raiz;
    }

    @Prueba
    void lasTareasDelPdfDeDivisionEstanEnElTablero() throws Exception {
        Path raiz = MotorAntdron.raizPorDefecto();
        Equipo equipo = new LectorConfiguracion().leer(raiz.resolve("config"));
        TableroXp tablero = TableroXp.cargar(raiz, equipo);

        Set<String> practicasDelFramework = Arrays.stream(Practica.values()).map(Practica::getNombre)
                .collect(Collectors.toSet());
        Set<String> practicasDelTablero = tablero.porPractica().stream().map(TableroXp.AvancePractica::practica)
                .collect(Collectors.toSet());
        Afirmar.igual(practicasDelFramework, practicasDelTablero, "las 17 prácticas XP tienen tareas");
        Afirmar.igual(Set.copyOf(Tarea.SESIONES), tablero.getTareas().stream().map(Tarea::sesion)
                .collect(Collectors.toSet()));
        for (TableroXp.AvanceIntegrante a : tablero.porIntegrante()) {
            Afirmar.verdadero(a.total() >= 5, a.integrante().nombre() + " debe tener sus tareas");
            Afirmar.falso(a.historias().isEmpty(), a.integrante().nombre() + " debe tener historias del backlog");
        }
        for (Tarea t : tablero.getTareas()) {
            Afirmar.verdadero(t.artefacto().isEmpty() || tablero.existe(t.artefacto()),
                    t.id() + ": no existe el artefacto " + t.artefacto());
            if (t.evidenciaEsArchivo()) {
                Afirmar.verdadero(t.evidencia().matches("docs/evidencias/f[1-4]-[a-z0-9-]+\\.png"), t.id());
            }
        }
        Afirmar.igual(21, tablero.getHistorias().size());
        Afirmar.verdadero(tablero.getHistorias().stream().allMatch(h -> !h.responsables().isEmpty()));
    }

    @Prueba
    void elEstadoSaleDeLaEvidenciaDelArtefactoODeLoDeclarado() throws Exception {
        Path raiz = proyecto(String.join("\n",
                "T-1;S1;Reuniones;sjibaja;Acta;xp/acta.md;docs/evidencias/f1-acta.png;POR_HACER",
                "T-2;S1;Reuniones;sjibaja;Acta;xp/acta.md;docs/evidencias/f1-otra.png;POR_HACER",
                "T-3;S3;Integración continua;kmosquera;Fusionar el PR;;PR fusionado;HECHA",
                "T-4;S4;;todos;Video;;Video S4;",
                "T-5;PREP;;kmosquera;Live Share;;Instalado;EN_CURSO"));
        Files.writeString(raiz.resolve("xp/acta.md"), "acta");
        Files.createDirectories(raiz.resolve("docs/evidencias"));
        Files.writeString(raiz.resolve("docs/evidencias/f1-acta.png"), "png");
        TableroXp tablero = TableroXp.cargar(raiz, EQUIPO);

        List<EstadoTarea> estados = tablero.getTareas().stream().map(tablero::estado).toList();
        Afirmar.igual(List.of(EstadoTarea.HECHA, EstadoTarea.EN_CURSO, EstadoTarea.HECHA, EstadoTarea.POR_HACER,
                EstadoTarea.EN_CURSO), estados);
        Afirmar.igual(2, tablero.responsables(tablero.getTareas().get(3)).size(), "«todos» son los dos integrantes");
        TableroXp.AvanceIntegrante karen = tablero.porIntegrante().get(1);
        Afirmar.igual(3, karen.total());
        Afirmar.igual(1, karen.hechas());
        Afirmar.igual(List.of(new TableroXp.AvancePractica("Reuniones", 1, 2),
                new TableroXp.AvancePractica("Integración continua", 1, 1)), tablero.porPractica());
    }

    @Prueba
    void cruzaElBacklogConElReporteDeAceptacion() throws Exception {
        Path raiz = proyecto("T-1;S1;Historias de usuario;kmosquera;Historias;;;\n");
        Files.writeString(raiz.resolve("xp/historias.csv"), String.join("\n",
                "id;titulo;como;quiero;para;prioridad;puntos;iteracion;estado;responsables;criterios",
                "HU-01;Autenticación;usuario;entrar;usar;ALTA;3;1;TERMINADA;Karen Mosquera;3 intentos",
                "HU-06;Autómata;KGD;bomba;explotar;ALTA;3;2;EN_PROGRESO;Sebastián Jibaja;AFD"));
        Files.writeString(raiz.resolve("xp/pruebas-aceptacion.md"), String.join("\n",
                "| Historia | Título | Pasadas | Fallidas | Otras | Estado |", "|---|---|---|---|---|---|",
                "| HU-01 | Autenticación | 7 | 0 | 0 | ACEPTADA |", "| HU-06 | Autómata | 4 | 1 | 0 | RECHAZADA |"));
        TableroXp tablero = TableroXp.cargar(raiz, EQUIPO);

        Afirmar.igual(new TableroXp.Historia("HU-01", "Autenticación", List.of("Karen Mosquera"), 3, 1, "TERMINADA",
                7, 0, "ACEPTADA"), tablero.getHistorias().get(0));
        Afirmar.igual(1L, tablero.historiasAceptadas());
        Afirmar.igual(1, tablero.pruebasFallidas());
        Afirmar.igual(List.of("HU-06"), tablero.porIntegrante().get(0).historias());
        Afirmar.igual(List.of(new TableroXp.AvanceIteracion(1, 3, 3), new TableroXp.AvanceIteracion(2, 3, 0)),
                tablero.porIteracion());
    }

    @Prueba
    void avisaSiLaTareaTieneDatosQueNoExisten() throws Exception {
        Afirmar.lanza(IllegalArgumentException.class,
                () -> TableroXp.cargar(proyecto("T-1;S1;;nadie;Algo;;;\n"), EQUIPO));
        Afirmar.lanza(IllegalArgumentException.class,
                () -> TableroXp.cargar(proyecto("T-1;S9;;sjibaja;Algo;;;\n"), EQUIPO));
        Afirmar.lanza(IllegalArgumentException.class,
                () -> TableroXp.cargar(proyecto("T-1;S1;;sjibaja;Algo;;;LISTA\n"), EQUIPO));
        Afirmar.lanza(java.io.IOException.class, () -> TableroXp.cargar(Temporal.directorio(), EQUIPO));
    }
}
