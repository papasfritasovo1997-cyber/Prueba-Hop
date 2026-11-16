package ec.edu.epn.antdron.pkAntCiberDronContract;

import ec.edu.epn.antdron.alimento.Alimento;

/** Contrato de toda hormiga (diagrama de clases del enunciado). */
public interface IHormiga {

    /** @return true si la hormiga sigue con vida después de comer. */
    boolean comer(Alimento alimento);
}
