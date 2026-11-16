package ec.edu.epn.antdron.pkAntCiberDronContract;

/** Inteligencia artificial del AntCiberDron (diagrama de clases del enunciado). */
public interface IIA {

    /**
     * Busca el tipo de arsenal en las coordenadas ucranianas y valora con el autómata si la bomba debe explotar.
     * Presenta la coordenada donde explota.
     *
     * @return true si la bomba explota en al menos una coordenada objetivo.
     */
    boolean buscar(String tipoArsenal);
}
