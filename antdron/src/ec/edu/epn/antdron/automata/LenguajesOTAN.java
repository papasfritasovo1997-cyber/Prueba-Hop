package ec.edu.epn.antdron.automata;

import java.util.Map;

import java.util.regex.Pattern;

/**
 * Lenguaje (tipo de arsenal) de cada coordenada según la estrategia OTAN del enunciado.
 * Símbolos: a = Avión, b = Barco, c = Convoy, d = Dron, t = Tanque.
 */
public final class LenguajesOTAN {

    private static final String[] EXPRESIONES = {
        "a+",      // Coord-00
        "a+",      // Coord-01
        "ab*",     // Coord-02
        "abc",     // Coord-03
        "abcd*",   // Coord-04
        "abcdt+",  // Coord-05
        "abcdt+",  // Coord-06
        "abcd*",   // Coord-07
        "abc",     // Coord-08
        "ab*",     // Coord-09
    };

    private static final Pattern GEOPOSICION = Pattern.compile("Coord-(\\d{2})");

    private static final Map<Character, String> ARSENAL =
            Map.of('a', "Avión", 'b', "Barco", 'c', "Convoy", 'd', "Dron", 't', "Tanque");

    private LenguajesOTAN() {
    }

    /** Expresión del lenguaje de la coordenada {@code digito} (0-9), p. ej. 5 → "abcdt+". */
    public static String expresion(int digito) {
        if (digito < 0 || digito > 9) {
            throw new IllegalArgumentException("La coordenada debe estar entre 0 y 9: " + digito);
        }
        return EXPRESIONES[digito];
    }

    /** "Coord-05" → 5. */
    public static int digito(String geoposicion) {
        var m = geoposicion == null ? null : GEOPOSICION.matcher(geoposicion.trim());
        if (m == null || !m.matches()) {
            throw new IllegalArgumentException("Geoposición inválida: '" + geoposicion + "' (formato Coord-0N)");
        }
        return Integer.parseInt(m.group(1)) % 10;
    }

    /** 5 → "Coord-05". */
    public static String geoposicion(int digito) {
        expresion(digito);
        return String.format("Coord-%02d", digito);
    }

    /** "1754265385" → 5: la coordenada objetivo es el último dígito de la cédula. */
    public static int digitoObjetivo(String cedula) {
        String c = cedula == null ? "" : cedula.trim();
        if (!c.matches("\\d{6,13}")) {
            throw new IllegalArgumentException("Cédula inválida '" + cedula + "': solo dígitos (6 a 13)");
        }
        return c.charAt(c.length() - 1) - '0';
    }

    /** True si la palabra w pertenece al lenguaje de la coordenada (útil para validar Grupo##.csv). */
    public static boolean pertenece(int digito, String w) {
        return ConstructorAutomata.desdeExpresion(expresion(digito)).acepta(w);
    }

    /** 'a' → "Avión". */
    public static String arsenal(char simbolo) {
        String nombre = ARSENAL.get(simbolo);
        if (nombre == null) {
            throw new IllegalArgumentException("Símbolo de arsenal desconocido: " + simbolo);
        }
        return nombre;
    }
}
