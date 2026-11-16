package ec.edu.epn.antdron.hormiga;

import java.util.Arrays;
import java.util.Optional;

import ec.edu.epn.antdron.alimento.ACarnivoro;
import ec.edu.epn.antdron.alimento.AHerbivoro;
import ec.edu.epn.antdron.alimento.AOmnivoro;
import ec.edu.epn.antdron.alimento.Alimento;

/** Tabla TIPO_HORMIGA / ALIMENTO / GRUPO del enunciado. */
public enum TipoHormiga {
    SOLDADO("HSoldado", new int[] {1, 2}) {
        @Override public Alimento alimento() { return new ACarnivoro(); }
        @Override public Hormiga crear(Integer id, String sexo) { return new HSoldado(id, sexo); }
    },
    ZANGANO("HZángano", new int[] {3, 4}) {
        @Override public Alimento alimento() { return new AOmnivoro(); }
        @Override public Hormiga crear(Integer id, String sexo) { return new HZangano(id, sexo); }
    },
    RASTREADORA("HRastreadora", new int[] {5}) {
        @Override public Alimento alimento() { return new AHerbivoro(); }
        @Override public Hormiga crear(Integer id, String sexo) { return new HRastreadora(id, sexo); }
    };

    private final String nombre;
    private final int[] grupos;

    TipoHormiga(String nombre, int[] grupos) {
        this.nombre = nombre;
        this.grupos = grupos;
    }

    /** ALIMENTO con el que vive este TIPO_HORMIGA. */
    public abstract Alimento alimento();

    public abstract Hormiga crear(Integer id, String sexo);

    public boolean esSuAlimento(Alimento a) {
        return alimento().esDelMismoTipo(a);
    }

    public String getNombre() {
        return nombre;
    }

    public static TipoHormiga paraGrupo(int grupo) {
        return Arrays.stream(values())
                .filter(t -> Arrays.stream(t.grupos).anyMatch(g -> g == grupo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El grupo " + grupo
                        + " no tiene TIPO_HORMIGA en el enunciado (solo grupos 1 a 5)"));
    }

    public static Optional<TipoHormiga> porNombre(String nombre) {
        return Arrays.stream(values()).filter(t -> t.nombre.equals(nombre)).findFirst();
    }
}
