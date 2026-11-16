package ec.edu.epn.antdron.antciberdron;

/** Turbo reactor ubicado en la espalda: permite volar consumiendo energía de la fuente de poder. */
public final class TurboReactor {

    public static final String UBICACION = "espalda";
    private final double consumoPorMinuto;

    public TurboReactor(double consumoPorMinuto) {
        if (consumoPorMinuto <= 0) {
            throw new IllegalArgumentException("El consumo por minuto debe ser > 0");
        }
        this.consumoPorMinuto = consumoPorMinuto;
    }

    public String volar(int minutos, FuentePoder fuente) {
        if (minutos <= 0) {
            throw new IllegalArgumentException("Los minutos de vuelo deben ser > 0");
        }
        fuente.consumir(minutos * consumoPorMinuto);
        return "Vuelo de " + minutos + " min con el turbo reactor de la " + UBICACION;
    }

    public double getConsumoPorMinuto() {
        return consumoPorMinuto;
    }
}
