package ec.edu.epn.antdron.hormiga;

import ec.edu.epn.antdron.alimento.AOmnivoro;
import ec.edu.epn.antdron.alimento.Alimento;

/** TIPO_HORMIGA de los grupos 3 y 4: vive si come alimento omnívoro. */
public class HZangano extends Hormiga {

    public HZangano() {
        super();
    }

    public HZangano(Integer id, String sexo) {
        super(id, sexo);
    }

    @Override
    public String getTipo() {
        return "HZángano";
    }

    @Override
    public Alimento getAlimentoVital() {
        return new AOmnivoro();
    }
}
