package ec.edu.epn.xp.planificacion;

import java.util.Locale;

/** Prioridad de negocio asignada por el Cliente a una historia de usuario. */
public enum Prioridad {
    ALTA(3), MEDIA(2), BAJA(1);

    private final int peso;

    Prioridad(int peso) {
        this.peso = peso;
    }

    public int getPeso() {
        return peso;
    }

    public static Prioridad desde(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("La prioridad es obligatoria (ALTA, MEDIA o BAJA)");
        }
        try {
            return valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Prioridad inválida: " + texto + " (use ALTA, MEDIA o BAJA)", e);
        }
    }
}
