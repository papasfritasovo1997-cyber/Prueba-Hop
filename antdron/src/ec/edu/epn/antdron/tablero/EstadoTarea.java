package ec.edu.epn.antdron.tablero;

/** Columna del tablero en la que está una tarea. */
public enum EstadoTarea {
    POR_HACER("Por hacer"),
    EN_CURSO("En curso"),
    HECHA("Hecha");

    private final String etiqueta;

    EstadoTarea(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static EstadoTarea desde(String texto) {
        String t = texto == null ? "" : texto.trim().toUpperCase().replace(' ', '_');
        if (t.isEmpty()) {
            return POR_HACER;
        }
        try {
            return valueOf(t);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de tarea inválido: '" + texto + "' (POR_HACER, EN_CURSO o HECHA)");
        }
    }
}
