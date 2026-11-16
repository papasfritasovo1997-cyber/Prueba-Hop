package ec.edu.epn.antdron.antciberdron;

/** Idiomas que el AntCiberDron puede aprender. */
public enum Idioma {
    INGLES("Inglés"), ESPANOL("Español");

    private final String etiqueta;

    Idioma(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
