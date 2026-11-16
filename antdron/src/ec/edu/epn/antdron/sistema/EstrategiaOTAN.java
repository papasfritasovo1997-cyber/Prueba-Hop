package ec.edu.epn.antdron.sistema;

import ec.edu.epn.antdron.automata.LenguajesOTAN;

/**
 * Estrategia de guerra OTAN (tabla del enunciado): por cada coordenada, el día y el horario del ataque.
 * El lenguaje (tipo de arsenal) de cada coordenada está en {@link LenguajesOTAN}.
 */
public final class EstrategiaOTAN {

    /** Fila de la tabla. {@code dia} es null para Coord-00 (sin horario). */
    public record Entrada(int digito, DiaSemana dia, String horario) {
        public String geoposicion() {
            return LenguajesOTAN.geoposicion(digito);
        }

        public String expresion() {
            return LenguajesOTAN.expresion(digito);
        }
    }

    private static final Entrada[] TABLA = {
        new Entrada(0, null, ""),
        new Entrada(1, DiaSemana.LUNES, "01-02"),
        new Entrada(2, DiaSemana.MARTES, "02-04"),
        new Entrada(3, DiaSemana.MIERCOLES, "03-06"),
        new Entrada(4, DiaSemana.JUEVES, "04-08"),
        new Entrada(5, DiaSemana.VIERNES, "05-10"),
        new Entrada(6, DiaSemana.JUEVES, "04-08"),
        new Entrada(7, DiaSemana.MIERCOLES, "03-06"),
        new Entrada(8, DiaSemana.MARTES, "02-04"),
        new Entrada(9, DiaSemana.LUNES, "01-02"),
    };

    private EstrategiaOTAN() {
    }

    public static Entrada de(int digito) {
        if (digito < 0 || digito > 9) {
            throw new IllegalArgumentException("Dígito fuera de rango: " + digito);
        }
        return TABLA[digito];
    }
}
