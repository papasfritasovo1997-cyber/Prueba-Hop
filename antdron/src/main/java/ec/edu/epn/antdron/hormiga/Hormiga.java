package ec.edu.epn.antdron.hormiga;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import ec.edu.epn.antdron.alimento.Alimento;
import ec.edu.epn.antdron.pkAntCiberDronContract.IHormiga;
import ec.edu.epn.xp.diseno.TarjetaCRC;

/** Hormiga abstracta (diagrama de clases del enunciado): id, sexo y estado. */
@TarjetaCRC(responsabilidades = {"Conocer id, sexo y estado", "Comer y decidir si vive", "Tener extremidades"},
        colaboradores = {"Alimento", "Extremidad"})
public abstract class Hormiga implements IHormiga {

    public static final String VIVA = "VIVA";
    public static final String MUERTA = "MUERTA";
    public static final String TRANSFORMADA = "TRANSFORMADA";
    public static final String HEMBRA = "HEMBRA";
    public static final String MACHO = "MACHO";

    private static final AtomicInteger SECUENCIA = new AtomicInteger();

    private Integer id;
    private String sexo;
    private String estado;
    private final List<Extremidad> extremidades = Extremidad.anatomia();

    protected Hormiga() {
        this(SECUENCIA.incrementAndGet(), HEMBRA);
    }

    protected Hormiga(Integer id, String sexo) {
        this.id = Objects.requireNonNull(id, "id");
        this.sexo = validarSexo(sexo);
        this.estado = VIVA;
    }

    private static String validarSexo(String sexo) {
        String s = sexo == null ? "" : sexo.trim().toUpperCase(Locale.ROOT);
        if (!s.equals(HEMBRA) && !s.equals(MACHO)) {
            throw new IllegalArgumentException("Sexo inválido: " + sexo + " (HEMBRA o MACHO)");
        }
        return s;
    }

    /** Nombre del TIPO_HORMIGA, p. ej. "HSoldado". */
    public abstract String getTipo();

    /** Alimento con el que este tipo de hormiga vive. */
    public abstract Alimento getAlimentoVital();

    /** Vive si come su alimento; cualquier otro alimento la mata. Una hormiga muerta no come. */
    @Override
    public boolean comer(Alimento alimento) {
        Objects.requireNonNull(alimento, "alimento");
        if (!estaViva()) {
            return false;
        }
        if (getAlimentoVital().esDelMismoTipo(alimento)) {
            return true;
        }
        estado = MUERTA;
        return false;
    }

    public boolean estaViva() {
        return VIVA.equals(estado);
    }

    protected void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getId() { return id; }
    public String getSexo() { return sexo; }
    public String getEstado() { return estado; }
    public List<Extremidad> getExtremidades() { return extremidades; }

    @Override
    public String toString() {
        return getTipo() + "{id=" + id + ", sexo=" + sexo + ", estado=" + estado + "}";
    }
}
