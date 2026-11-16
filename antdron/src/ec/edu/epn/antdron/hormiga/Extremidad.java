package ec.edu.epn.antdron.hormiga;

import java.util.List;

/** Extremidades de una hormiga; el AntCiberDron se compone de las mismas. */
public enum Extremidad {
    ANTENA_IZQUIERDA, ANTENA_DERECHA, MANDIBULA,
    PATA_DELANTERA_DERECHA, PATA_DELANTERA_IZQUIERDA,
    PATA_MEDIA_DERECHA, PATA_MEDIA_IZQUIERDA,
    PATA_TRASERA_DERECHA, PATA_TRASERA_IZQUIERDA;

    public static List<Extremidad> anatomia() {
        return List.of(values());
    }

    public boolean esPata() {
        return name().startsWith("PATA_");
    }
}
