package ec.edu.epn.antdron.hormiga;

import java.util.Objects;
import java.util.Optional;

import ec.edu.epn.antdron.alimento.ANectar;
import ec.edu.epn.antdron.alimento.Alimento;

/**
 * Todo TIPO_HORMIGA nace como HLarva: vive comiendo néctar y, si come el ALIMENTO de su
 * TIPO_HORMIGA, se transforma en él. Cualquier otro alimento la mata.
 */
public class HLarva extends Hormiga {

    private final TipoHormiga destino;
    private Hormiga evolucion;

    public HLarva(TipoHormiga destino) {
        super();
        this.destino = Objects.requireNonNull(destino, "destino");
    }

    public HLarva(Integer id, String sexo, TipoHormiga destino) {
        super(id, sexo);
        this.destino = Objects.requireNonNull(destino, "destino");
    }

    @Override
    public String getTipo() {
        return "HLarva";
    }

    @Override
    public Alimento getAlimentoVital() {
        return new ANectar();
    }

    @Override
    public boolean comer(Alimento alimento) {
        Objects.requireNonNull(alimento, "alimento");
        if (!estaViva()) {
            return false;
        }
        if (getAlimentoVital().esDelMismoTipo(alimento)) {
            return true;
        }
        if (destino.esSuAlimento(alimento)) {
            evolucion = destino.crear(getId(), getSexo());
            setEstado(TRANSFORMADA);
            return true;
        }
        setEstado(MUERTA);
        return false;
    }

    public TipoHormiga getDestino() {
        return destino;
    }

    /** Hormiga adulta en la que se transformó la larva, si ya ocurrió. */
    public Optional<Hormiga> getEvolucion() {
        return Optional.ofNullable(evolucion);
    }
}
