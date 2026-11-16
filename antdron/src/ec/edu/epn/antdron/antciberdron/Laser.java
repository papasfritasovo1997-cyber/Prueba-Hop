package ec.edu.epn.antdron.antciberdron;

/** Láser: va en la pata delantera izquierda; su potencia varía según la energía de la fuente de poder. */
public final class Laser implements Arma {

    public static final double CONSUMO_POR_DISPARO = 5;
    private final double potenciaMaxima;

    public Laser(double potenciaMaxima) {
        if (potenciaMaxima <= 0) {
            throw new IllegalArgumentException("La potencia máxima debe ser > 0");
        }
        this.potenciaMaxima = potenciaMaxima;
    }

    /** Potencia proporcional al porcentaje de energía disponible. */
    public double potencia(FuentePoder fuente) {
        return potenciaMaxima * fuente.porcentaje() / 100;
    }

    @Override
    public String getNombre() {
        return "Láser";
    }

    @Override
    public String disparar(FuentePoder fuente) {
        double potencia = potencia(fuente);
        fuente.consumir(CONSUMO_POR_DISPARO);
        return String.format("Disparo láser de %.1f W", potencia);
    }

    public double getPotenciaMaxima() {
        return potenciaMaxima;
    }
}
