package ec.edu.epn.antdron.sistema;

/**
 * Estrategia de guerra OTAN (tabla del enunciado): por cada coordenada, el día, el horario
 * y la expresión del lenguaje que describe el tipo de arsenal.
 * Símbolos: a=Avión, b=Barco, c=Convoy, d=Dron, t=Tanque.
 */
public final class EstrategiaOTAN {

    /** Fila de la tabla. {@code dia} es null para Coord-00 (sin horario). */
    public record Entrada(int digito, DiaSemana dia, String horario, String expresion) {
        public String geoposicion() {
            return EstrategiaOTAN.geoposicion(digito);
        }
    }

    private static final Entrada[] TABLA = {
        new Entrada(0, null, "", "a+"),
        new Entrada(1, DiaSemana.LUNES, "01-02", "a+"),
        new Entrada(2, DiaSemana.MARTES, "02-04", "ab*"),
        new Entrada(3, DiaSemana.MIERCOLES, "03-06", "abc"),
        new Entrada(4, DiaSemana.JUEVES, "04-08", "abcd*"),
        new Entrada(5, DiaSemana.VIERNES, "05-10", "abcdt+"),
        new Entrada(6, DiaSemana.JUEVES, "04-08", "abcdt+"),
        new Entrada(7, DiaSemana.MIERCOLES, "03-06", "abcd*"),
        new Entrada(8, DiaSemana.MARTES, "02-04", "abc"),
        new Entrada(9, DiaSemana.LUNES, "01-02", "ab*"),
    };

    private EstrategiaOTAN() {
    }

    public static Entrada de(int digito) {
        if (digito < 0 || digito > 9) {
            throw new IllegalArgumentException("Dígito fuera de rango: " + digito);
        }
        return TABLA[digito];
    }

    public static String expresion(int digito) {
        return de(digito).expresion();
    }

    public static String geoposicion(int digito) {
        return String.format("Coord-%02d", digito);
    }

    public static String arsenal(char simbolo) {
        return switch (simbolo) {
            case 'a' -> "Avión";
            case 'b' -> "Barco";
            case 'c' -> "Convoy";
            case 'd' -> "Dron";
            case 't' -> "Tanque";
            default -> throw new IllegalArgumentException("Símbolo de arsenal desconocido: " + simbolo);
        };
    }
}
