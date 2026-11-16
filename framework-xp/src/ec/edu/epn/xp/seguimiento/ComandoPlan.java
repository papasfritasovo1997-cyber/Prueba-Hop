package ec.edu.epn.xp.seguimiento;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import ec.edu.epn.xp.ComandoXP;
import ec.edu.epn.xp.ContextoXP;
import ec.edu.epn.xp.planificacion.HistoriaUsuario;
import ec.edu.epn.xp.planificacion.LectorBacklog;

/** {@code xp plan [--proponer N]}: plan de entregas por iteración, o propuesta con velocidad N. */
public final class ComandoPlan implements ComandoXP {

    @Override
    public String nombre() {
        return "plan";
    }

    @Override
    public String descripcion() {
        return "Plan de entregas por iteración (--proponer N: propuesta con velocidad N)";
    }

    @Override
    public int ejecutar(List<String> argumentos, ContextoXP contexto) throws IOException {
        Integer velocidad = null;
        if (!argumentos.isEmpty()) {
            if (argumentos.size() != 2 || !argumentos.get(0).equals("--proponer")) {
                throw new IllegalArgumentException("Uso: xp plan [--proponer N]");
            }
            try {
                velocidad = Integer.parseInt(argumentos.get(1));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("--proponer necesita un número: " + argumentos.get(1));
            }
        }
        List<HistoriaUsuario> historias = new LectorBacklog().leer(contexto.ruta("backlog", "xp/historias.csv"));
        PrintStream out = contexto.getSalida();
        List<Iteracion> iteraciones;
        if (velocidad != null) {
            iteraciones = PlanEntregas.proponer(historias.stream().filter(h -> !h.estaTerminada()).toList(),
                    velocidad, 1);
            out.println("Propuesta con velocidad " + velocidad + " pts/iteración:");
        } else {
            iteraciones = PlanEntregas.agrupar(historias);
        }
        for (Iteracion it : iteraciones) {
            out.println(it);
            it.getHistorias().forEach(h -> out.println("   - " + h));
        }
        List<HistoriaUsuario> sinAsignar = historias.stream().filter(h -> h.getIteracion() == 0).toList();
        if (velocidad == null && !sinAsignar.isEmpty()) {
            out.println("Sin iteración asignada:");
            sinAsignar.forEach(h -> out.println("   - " + h));
        }
        return 0;
    }
}
