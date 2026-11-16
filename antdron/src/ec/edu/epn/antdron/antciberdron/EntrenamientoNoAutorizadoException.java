package ec.edu.epn.antdron.antciberdron;

/** Se lanza cuando alguien que no es experto parlante del idioma intenta entrenarlo. */
public class EntrenamientoNoAutorizadoException extends RuntimeException {

    public EntrenamientoNoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}
