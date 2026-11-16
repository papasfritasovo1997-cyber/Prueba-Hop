package ec.edu.epn.xp.desarrollo;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import ec.edu.epn.xp.ComandoXP;
import ec.edu.epn.xp.ContextoXP;

/** {@code xp equipo}: grupo, metáfora, integrantes y roles XP. */
public final class ComandoEquipo implements ComandoXP {

    @Override
    public String nombre() {
        return "equipo";
    }

    @Override
    public String descripcion() {
        return "Integrantes, roles XP y metáfora del sistema (config/)";
    }

    @Override
    public int ejecutar(List<String> argumentos, ContextoXP contexto) throws IOException {
        Equipo e = new LectorEquipo().leer(contexto.ruta("equipo", "config"));
        PrintStream out = contexto.getSalida();
        out.println("[+] GRUPO " + e.numeroConFormato() + ": " + e.getNombre());
        out.println("    Metáfora: " + e.getMetafora());
        for (Integrante i : e.getIntegrantes()) {
            out.printf("    • %-18s | %-28s | %s%n", i.tieneCedula() ? i.getCedula() : "(cédula pendiente)",
                    i.getNombre(), i.getRol().getEtiqueta());
        }
        long pendientes = e.getIntegrantes().stream().filter(i -> !i.tieneCedula()).count();
        if (pendientes > 0) {
            out.println("    Faltan " + pendientes + " cédulas en config/integrantes.csv");
        }
        return 0;
    }
}
