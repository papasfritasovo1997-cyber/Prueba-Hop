package ec.edu.epn.antdron.antciberdron;

import java.util.Objects;

/** Experto parlante nativo de un idioma: el único autorizado a entrenar ese idioma. */
public final class ExpertoParlante {

    private final String nombre;
    private final Idioma idiomaNativo;

    public ExpertoParlante(String nombre, Idioma idiomaNativo) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El experto debe tener nombre");
        }
        this.nombre = nombre.trim();
        this.idiomaNativo = Objects.requireNonNull(idiomaNativo, "idiomaNativo");
    }

    public String getNombre() {
        return nombre;
    }

    public Idioma getIdiomaNativo() {
        return idiomaNativo;
    }

    @Override
    public String toString() {
        return nombre + " (" + idiomaNativo.getEtiqueta() + ")";
    }
}
