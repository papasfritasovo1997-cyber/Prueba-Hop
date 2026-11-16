package ec.edu.epn.antdron.hormiga;

import ec.edu.epn.antdron.alimento.ACarnivoro;
import ec.edu.epn.antdron.alimento.Alimento;

/** TIPO_HORMIGA de los grupos 1 y 2: vive si come alimento carnívoro. */
public class HSoldado extends Hormiga {

    public HSoldado() {
        super();
    }

    public HSoldado(Integer id, String sexo) {
        super(id, sexo);
    }

    @Override
    public String getTipo() {
        return "HSoldado";
    }

    @Override
    public Alimento getAlimentoVital() {
        return new ACarnivoro();
    }
}
