package ec.edu.epn.xp.seguimiento;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import ec.edu.epn.xp.ComandoXP;
import ec.edu.epn.xp.ContextoXP;
import ec.edu.epn.xp.planificacion.LectorBacklog;

/** {@code xp velocidad}: avance por iteración, clima de ayer y velocidad promedio. */
public final class ComandoVelocidad implements ComandoXP {

    @Override
    public String nombre() {
        return "velocidad";
    }

    @Override
    public String descripcion() {
        return "Velocidad del proyecto (clima de ayer y promedio)";
    }

    @Override
    public int ejecutar(List<String> argumentos, ContextoXP contexto) throws IOException {
        List<Iteracion> iteraciones = PlanEntregas.agrupar(
                new LectorBacklog().leer(contexto.ruta("backlog", "xp/historias.csv")));
        PrintStream out = contexto.getSalida();
        iteraciones.forEach(out::println);
        out.println("Velocidad (clima de ayer): " + Velocidad.climaDeAyer(iteraciones) + " pts");
        out.printf("Velocidad promedio: %.1f pts%n", Velocidad.promedio(iteraciones));
        return 0;
    }
}
