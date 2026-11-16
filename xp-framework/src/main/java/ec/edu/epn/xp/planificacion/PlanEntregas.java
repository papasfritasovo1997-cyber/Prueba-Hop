package ec.edu.epn.xp.planificacion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Plan de entregas: agrupa el backlog por iteración o propone una planificación según la velocidad. */
public final class PlanEntregas {

    private PlanEntregas() {
    }

    /** Agrupa las historias ya asignadas (iteración > 0) en orden de iteración. */
    public static List<Iteracion> agrupar(List<HistoriaUsuario> historias) {
        Map<Integer, List<HistoriaUsuario>> porIteracion = new TreeMap<>();
        for (HistoriaUsuario h : historias) {
            if (h.getIteracion() > 0) {
                porIteracion.computeIfAbsent(h.getIteracion(), k -> new ArrayList<>()).add(h);
            }
        }
        List<Iteracion> iteraciones = new ArrayList<>();
        porIteracion.forEach((numero, lista) -> iteraciones.add(new Iteracion(numero, lista)));
        return iteraciones;
    }

    /**
     * Propone iteraciones llenándolas por prioridad (mayor primero) sin superar la velocidad.
     * Una historia mayor que la velocidad debe dividirse antes de planificarse (regla XP).
     */
    public static List<Iteracion> proponer(List<HistoriaUsuario> historias, int velocidad, int primeraIteracion) {
        if (velocidad <= 0) {
            throw new IllegalArgumentException("La velocidad debe ser > 0");
        }
        List<HistoriaUsuario> ordenadas = new ArrayList<>(historias);
        ordenadas.sort(Comparator.comparingInt((HistoriaUsuario h) -> h.getPrioridad().getPeso()).reversed()
                .thenComparing(HistoriaUsuario::getId));

        List<Iteracion> plan = new ArrayList<>();
        List<HistoriaUsuario> actual = new ArrayList<>();
        int usados = 0;
        for (HistoriaUsuario h : ordenadas) {
            if (h.getPuntos() > velocidad) {
                throw new IllegalArgumentException("La historia " + h.getId() + " (" + h.getPuntos()
                        + " pts) supera la velocidad (" + velocidad + "): divídala en historias más pequeñas");
            }
            if (usados + h.getPuntos() > velocidad) {
                plan.add(new Iteracion(primeraIteracion + plan.size(), actual));
                actual = new ArrayList<>();
                usados = 0;
            }
            actual.add(h);
            usados += h.getPuntos();
        }
        if (!actual.isEmpty()) {
            plan.add(new Iteracion(primeraIteracion + plan.size(), actual));
        }
        return plan;
    }
}
