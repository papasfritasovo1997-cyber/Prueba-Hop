package ec.edu.epn.xp.planificacion;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import ec.edu.epn.xp.ComandoXP;
import ec.edu.epn.xp.ContextoXP;

/** {@code xp backlog}: lista las historias de usuario con su enunciado. */
public final class ComandoBacklog implements ComandoXP {

    @Override
    public String nombre() {
        return "backlog";
    }

    @Override
    public String descripcion() {
        return "Historias de usuario del backlog (xp/historias.csv)";
    }

    @Override
    public int ejecutar(List<String> argumentos, ContextoXP contexto) throws IOException {
        List<HistoriaUsuario> historias = new LectorBacklog().leer(contexto.ruta("backlog", "xp/historias.csv"));
        PrintStream out = contexto.getSalida();
        out.printf("%-6s | %-5s | %-3s | %-4s | %-11s | %s%n", "ID", "PRIO", "PTS", "ITER", "ESTADO", "TÍTULO");
        for (HistoriaUsuario h : historias) {
            out.printf("%-6s | %-5s | %3d | %4s | %-11s | %s%n", h.getId(), h.getPrioridad(), h.getPuntos(),
                    h.getIteracion() == 0 ? "-" : String.valueOf(h.getIteracion()), h.getEstado(), h.getTitulo());
            out.println("         " + h.enunciado());
        }
        out.println("Total: " + historias.size() + " historias, "
                + historias.stream().mapToInt(HistoriaUsuario::getPuntos).sum() + " puntos");
        return 0;
    }
}
