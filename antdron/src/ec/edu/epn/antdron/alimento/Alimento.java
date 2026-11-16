package ec.edu.epn.antdron.alimento;

import java.util.Objects;

/** Alimento abstracto (diagrama de clases del enunciado). */
public abstract class Alimento {

    private final String nombre;

    protected Alimento(String nombre) {
        this.nombre = Objects.requireNonNull(nombre);
    }

    public String getNombre() {
        return nombre;
    }

    /** Dos alimentos son del mismo tipo si son de la misma clase. */
    public boolean esDelMismoTipo(Alimento otro) {
        return otro != null && getClass() == otro.getClass();
    }

    @Override
    public String toString() {
        return nombre;
    }
}
