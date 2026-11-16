package ec.edu.epn.antdron.hormiga;

import ec.edu.epn.antdron.alimento.AHerbivoro;
import ec.edu.epn.antdron.alimento.Alimento;

/** TIPO_HORMIGA del grupo 5: vive si come alimento herbívoro. */
public class HRastreadora extends Hormiga {

    public HRastreadora() {
        super();
    }

    public HRastreadora(Integer id, String sexo) {
        super(id, sexo);
    }

    @Override
    public String getTipo() {
        return "HRastreadora";
    }

    @Override
    public Alimento getAlimentoVital() {
        return new AHerbivoro();
    }
}
