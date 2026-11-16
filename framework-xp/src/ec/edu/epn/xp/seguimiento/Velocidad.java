package ec.edu.epn.xp.seguimiento;

import ec.edu.epn.xp.planificacion.HistoriaUsuario;

import java.util.List;

/** Velocidad del proyecto: puntos de historia terminados por iteración. */
public final class Velocidad {

    private Velocidad() {
    }

    /** "El clima de ayer": lo terminado en la última iteración con avance es lo que se puede comprometer. */
    public static int climaDeAyer(List<Iteracion> iteraciones) {
        for (int i = iteraciones.size() - 1; i >= 0; i--) {
            int completados = iteraciones.get(i).puntosCompletados();
            if (completados > 0) {
                return completados;
            }
        }
        return 0;
    }

    /** Promedio de puntos completados en las iteraciones que ya tienen avance. */
    public static double promedio(List<Iteracion> iteraciones) {
        return iteraciones.stream()
                .mapToInt(Iteracion::puntosCompletados)
                .filter(p -> p > 0)
                .average()
                .orElse(0);
    }
}
