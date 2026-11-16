package ec.edu.epn.antdron.antciberdron;

/** Aspectos del idioma que solo un experto parlante puede entrenar. */
public enum AspectoLinguistico {
    LEXICO("Léxico"), GRAMATICA("Gramática"), FONETICA("Fonética");

    private final String etiqueta;

    AspectoLinguistico(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
