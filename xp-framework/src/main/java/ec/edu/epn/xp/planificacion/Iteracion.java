package ec.edu.epn.xp.planificacion;

import java.util.List;

/** Iteración XP (1 a 3 semanas) con las historias comprometidas. */
public final class Iteracion {

    private final int numero;
    private final List<HistoriaUsuario> historias;

    public Iteracion(int numero, List<HistoriaUsuario> historias) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El número de iteración debe ser > 0");
        }
        this.numero = numero;
        this.historias = List.copyOf(historias);
    }

    public int getNumero() {
        return numero;
    }

    public List<HistoriaUsuario> getHistorias() {
        return historias;
    }

    public int puntosPlanificados() {
        return historias.stream().mapToInt(HistoriaUsuario::getPuntos).sum();
    }

    public int puntosCompletados() {
        return historias.stream().filter(HistoriaUsuario::estaTerminada).mapToInt(HistoriaUsuario::getPuntos).sum();
    }

    /** Porcentaje de puntos terminados (0-100). */
    public int avance() {
        int planificados = puntosPlanificados();
        return planificados == 0 ? 0 : Math.round(puntosCompletados() * 100f / planificados);
    }

    public boolean estaCerrada() {
        return !historias.isEmpty() && historias.stream().allMatch(HistoriaUsuario::estaTerminada);
    }

    @Override
    public String toString() {
        return "Iteración " + numero + " (" + puntosCompletados() + "/" + puntosPlanificados() + " pts, "
                + avance() + "%)";
    }
}
