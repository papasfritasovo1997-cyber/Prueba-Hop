package ec.edu.epn.antdron.antciberdron;

/** Metralleta: va en la pata delantera derecha; usa munición, no energía. */
public final class Metralleta implements Arma {

    private static final int BALAS_POR_RAFAGA = 10;
    private int municion;

    public Metralleta(int municion) {
        if (municion < 0) {
            throw new IllegalArgumentException("La munición no puede ser negativa");
        }
        this.municion = municion;
    }

    @Override
    public String getNombre() {
        return "Metralleta";
    }

    @Override
    public String disparar(FuentePoder fuente) {
        if (municion == 0) {
            throw new IllegalStateException("Metralleta sin munición");
        }
        int disparadas = Math.min(BALAS_POR_RAFAGA, municion);
        municion -= disparadas;
        return "Ráfaga de " + disparadas + " balas (quedan " + municion + ")";
    }

    public int getMunicion() {
        return municion;
    }
}
