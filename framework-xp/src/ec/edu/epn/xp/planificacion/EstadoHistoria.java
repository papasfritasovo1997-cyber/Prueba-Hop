package ec.edu.epn.xp.planificacion;

import java.util.Locale;

/** Ciclo de vida de una historia de usuario dentro de una iteración. */
public enum EstadoHistoria {
    PENDIENTE, EN_PROGRESO, TERMINADA;

    public static EstadoHistoria desde(String texto) {
        if (texto == null || texto.isBlank()) {
            return PENDIENTE;
        }
        try {
            return valueOf(texto.trim().toUpperCase(Locale.ROOT).replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Estado inválido: " + texto + " (use PENDIENTE, EN_PROGRESO o TERMINADA)", e);
        }
    }
}
