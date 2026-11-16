package ec.edu.epn.antdron.antciberdron;

/** Arma que se carga en una pata delantera del AntCiberDron. */
public interface Arma {

    String getNombre();

    /** Dispara usando, si la necesita, la energía de la fuente de poder. */
    String disparar(FuentePoder fuente);
}
