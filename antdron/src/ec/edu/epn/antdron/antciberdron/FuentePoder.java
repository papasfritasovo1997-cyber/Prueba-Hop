package ec.edu.epn.antdron.antciberdron;

/** Fuente de poder recargable y reemplazable; solo el AntCiberDron (su IIA) puede recargarla. */
public final class FuentePoder {

    private final double capacidad;
    private double energia;

    public FuentePoder(double capacidad) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser > 0");
        }
        this.capacidad = capacidad;
        this.energia = capacidad;
    }

    public void consumir(double cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("Consumo negativo");
        }
        if (cantidad > energia) {
            throw new IllegalStateException(String.format(
                    "Energía insuficiente: se requieren %.1f y quedan %.1f", cantidad, energia));
        }
        energia -= cantidad;
    }

    /** Package-private: la recarga se hace bajo la asistencia del IIA (AntCiberDron). */
    void recargar() {
        energia = capacidad;
    }

    public double porcentaje() {
        return energia * 100 / capacidad;
    }

    public double getEnergia() {
        return energia;
    }

    public double getCapacidad() {
        return capacidad;
    }

    @Override
    public String toString() {
        return String.format("FuentePoder[%.1f/%.1f (%.0f%%)]", energia, capacidad, porcentaje());
    }
}
