package ec.edu.epn.antdron.tablero;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ec.edu.epn.antdron.config.Equipo;
import ec.edu.epn.antdron.config.Integrante;
import ec.edu.epn.antdron.pkAntCiberDronContract.Utility;

/**
 * Tablero de trabajo XP del equipo: las tareas de la división por fases ({@code xp/tareas.csv}), las historias
 * del backlog ({@code xp/historias.csv}) con su resultado en {@code xp/pruebas-aceptacion.md} y el avance de
 * cada integrante. Solo lee archivos del proyecto; no usa el framework XP.
 */
public final class TableroXp {

    public static final String TAREAS = "xp/tareas.csv";
    public static final String BACKLOG = "xp/historias.csv";
    public static final String ACEPTACION = "xp/pruebas-aceptacion.md";
    public static final String TODOS = "todos";

    /** Historia del backlog con el resultado de sus pruebas. */
    public record Historia(String id, String titulo, List<String> responsables, int puntos, int iteracion,
                           String estado, int pasadas, int fallidas, String aceptacion) {
        public boolean aceptada() {
            return "ACEPTADA".equals(aceptacion);
        }
    }

    public record AvanceIntegrante(Integrante integrante, int hechas, int enCurso, int total, List<String> historias) {
        public int porcentaje() {
            return total == 0 ? 0 : Math.round(hechas * 100f / total);
        }
    }

    public record AvancePractica(String practica, int hechas, int total) {
        public boolean defendida() {
            return total > 0 && hechas == total;
        }
    }

    /** Puntos planificados y terminados de una iteración (velocidad). */
    public record AvanceIteracion(int numero, int planificados, int terminados) {
    }

    private final Path raiz;
    private final Equipo equipo;
    private final List<Tarea> tareas;
    private final List<Historia> historias;

    private TableroXp(Path raiz, Equipo equipo, List<Tarea> tareas, List<Historia> historias) {
        this.raiz = raiz;
        this.equipo = equipo;
        this.tareas = List.copyOf(tareas);
        this.historias = List.copyOf(historias);
    }

    public static TableroXp cargar(Path raiz, Equipo equipo) throws IOException {
        Path archivoTareas = raiz.resolve(TAREAS);
        if (!Files.exists(archivoTareas)) {
            throw new IOException("No existe " + TAREAS + ": es la lista de tareas del tablero");
        }
        List<Tarea> tareas = leerTareas(Utility.leerTexto(archivoTareas).lines().toList(), equipo);
        Map<String, int[]> resultados = Files.exists(raiz.resolve(ACEPTACION))
                ? leerAceptacion(Utility.leerTexto(raiz.resolve(ACEPTACION)).lines().toList()) : Map.of();
        List<Historia> historias = Files.exists(raiz.resolve(BACKLOG))
                ? leerBacklog(Utility.leerTexto(raiz.resolve(BACKLOG)).lines().toList(), resultados) : List.of();
        return new TableroXp(raiz, equipo, tareas, historias);
    }

    static List<Tarea> leerTareas(List<String> lineas, Equipo equipo) {
        List<Tarea> tareas = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (Utility.esLineaIgnorable(linea, "id;")) {
                continue;
            }
            String[] c = Utility.camposCsv(linea);
            if (c.length != 8) {
                throw new IllegalArgumentException(TAREAS + " línea " + (i + 1) + ": se esperaban 8 campos y hay " + c.length);
            }
            List<String> responsables = Arrays.stream(c[3].split(",")).map(String::strip).filter(s -> !s.isEmpty()).toList();
            for (String r : responsables) {
                if (!r.equals(TODOS) && equipo.buscarUsuario(r).isEmpty()) {
                    throw new IllegalArgumentException(TAREAS + " línea " + (i + 1) + ": el usuario '" + r
                            + "' no está en config/integrantes.csv");
                }
            }
            try {
                tareas.add(new Tarea(c[0].strip(), c[1].strip(), c[2].strip(), responsables, c[4].strip(),
                        c[5].strip(), c[6].strip(), EstadoTarea.desde(c[7])));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(TAREAS + " línea " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
        return tareas;
    }

    /** Filas «| HU-01 | Título | pasadas | fallidas | otras | ESTADO |» del reporte de aceptación. */
    static Map<String, int[]> leerAceptacion(List<String> lineas) {
        Map<String, int[]> resultados = new LinkedHashMap<>();
        for (String linea : lineas) {
            String[] c = linea.split("\\|", -1);
            if (c.length == 8 && c[1].strip().matches("[A-Z]+-\\d+")) {
                try {
                    int estado = switch (c[6].strip()) {
                        case "ACEPTADA" -> 0;
                        case "RECHAZADA" -> 1;
                        default -> 2;
                    };
                    resultados.put(c[1].strip(), new int[] {Integer.parseInt(c[3].strip()),
                            Integer.parseInt(c[4].strip()), estado});
                } catch (NumberFormatException e) {
                    // Fila con otro formato: se ignora.
                }
            }
        }
        return resultados;
    }

    static List<Historia> leerBacklog(List<String> lineas, Map<String, int[]> resultados) {
        List<Historia> historias = new ArrayList<>();
        for (String linea : lineas) {
            if (Utility.esLineaIgnorable(linea, "id;")) {
                continue;
            }
            String[] c = Utility.camposCsv(linea);
            if (c.length != 11) {
                continue;
            }
            int[] r = resultados.getOrDefault(c[0].strip(), new int[] {0, 0, 3});
            String aceptacion = switch (r[2]) {
                case 0 -> "ACEPTADA";
                case 1 -> "RECHAZADA";
                default -> "SIN_PRUEBAS";
            };
            historias.add(new Historia(c[0].strip(), c[1].strip(),
                    Arrays.stream(c[9].split(",")).map(String::strip).filter(s -> !s.isEmpty()).toList(),
                    entero(c[6]), entero(c[7]), c[8].strip(), r[0], r[1], aceptacion));
        }
        return historias;
    }

    private static int entero(String texto) {
        try {
            return Integer.parseInt(texto.strip());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * HECHA si se declaró así o si su captura ya está en el repositorio; EN_CURSO si se declaró así o si el
     * artefacto existe pero falta la evidencia; si no, POR_HACER.
     */
    public EstadoTarea estado(Tarea t) {
        if (t.estadoDeclarado() == EstadoTarea.HECHA || (t.evidenciaEsArchivo() && existe(t.evidencia()))) {
            return EstadoTarea.HECHA;
        }
        if (t.estadoDeclarado() == EstadoTarea.EN_CURSO || (!t.artefacto().isEmpty() && existe(t.artefacto()))) {
            return EstadoTarea.EN_CURSO;
        }
        return EstadoTarea.POR_HACER;
    }

    public boolean existe(String rutaRelativa) {
        return !rutaRelativa.isEmpty() && Files.exists(raiz.resolve(rutaRelativa));
    }

    public List<Integrante> responsables(Tarea t) {
        if (t.responsables().contains(TODOS)) {
            return equipo.integrantes();
        }
        return t.responsables().stream().map(u -> equipo.buscarUsuario(u).orElseThrow()).toList();
    }

    public boolean esDe(Tarea t, Integrante integrante) {
        return t.responsables().contains(TODOS) || t.responsables().contains(integrante.usuario());
    }

    public List<Tarea> getTareas() {
        return tareas;
    }

    public List<Historia> getHistorias() {
        return historias;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public long contar(EstadoTarea estado) {
        return tareas.stream().filter(t -> estado(t) == estado).count();
    }

    public List<AvanceIntegrante> porIntegrante() {
        List<AvanceIntegrante> avance = new ArrayList<>();
        for (Integrante i : equipo.integrantes()) {
            List<Tarea> suyas = tareas.stream().filter(t -> esDe(t, i)).toList();
            List<String> deHistorias = historias.stream().filter(h -> h.responsables().contains(i.nombre()))
                    .map(Historia::id).toList();
            avance.add(new AvanceIntegrante(i, (int) suyas.stream().filter(t -> estado(t) == EstadoTarea.HECHA).count(),
                    (int) suyas.stream().filter(t -> estado(t) == EstadoTarea.EN_CURSO).count(), suyas.size(), deHistorias));
        }
        return avance;
    }

    /** Avance de cada práctica XP según sus tareas, en el orden en que aparecen. */
    public List<AvancePractica> porPractica() {
        Map<String, int[]> conteo = new LinkedHashMap<>();
        for (Tarea t : tareas) {
            if (!t.practica().isEmpty()) {
                int[] n = conteo.computeIfAbsent(t.practica(), k -> new int[2]);
                n[1]++;
                if (estado(t) == EstadoTarea.HECHA) {
                    n[0]++;
                }
            }
        }
        List<AvancePractica> avance = new ArrayList<>();
        conteo.forEach((p, n) -> avance.add(new AvancePractica(p, n[0], n[1])));
        return avance;
    }

    public List<AvanceIteracion> porIteracion() {
        Map<Integer, int[]> puntos = new TreeMap<>();
        for (Historia h : historias) {
            if (h.iteracion() > 0) {
                int[] p = puntos.computeIfAbsent(h.iteracion(), k -> new int[2]);
                p[0] += h.puntos();
                if ("TERMINADA".equals(h.estado())) {
                    p[1] += h.puntos();
                }
            }
        }
        List<AvanceIteracion> avance = new ArrayList<>();
        puntos.forEach((n, p) -> avance.add(new AvanceIteracion(n, p[0], p[1])));
        return avance;
    }

    public long historiasAceptadas() {
        return historias.stream().filter(Historia::aceptada).count();
    }

    public int pruebasPasadas() {
        return historias.stream().mapToInt(Historia::pasadas).sum();
    }

    public int pruebasFallidas() {
        return historias.stream().mapToInt(Historia::fallidas).sum();
    }
}
