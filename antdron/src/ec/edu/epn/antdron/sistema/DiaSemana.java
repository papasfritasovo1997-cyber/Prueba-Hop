package ec.edu.epn.antdron.sistema;

/** Días de la estrategia OTAN (columnas del CSV). */
public enum DiaSemana {
    LUNES("Lunes"), MARTES("Martes"), MIERCOLES("Miércoles"), JUEVES("Jueves"), VIERNES("Viernes");

    private final String etiqueta;

    DiaSemana(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
