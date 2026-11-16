package ec.edu.epn.antdron.bomba;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ec.edu.epn.antdron.automata.Automata;
import ec.edu.epn.antdron.automata.ConstructorAutomata;
import ec.edu.epn.antdron.automata.LenguajesOTAN;

/**
 * Bomba basada en un autómata. Las coordenadas objetivo son las del último dígito de la cédula de cada
 * integrante (p. ej. 1103635 y 1503639 → Coord-05 y Coord-09 → L = { abcdt+ , ab* }).
 * Explota solo si la coordenada es objetivo y el autómata acepta el tipo de arsenal: destrucción exclusiva.
 * Solo usa tipos del JDK en su interfaz pública para que las demás secciones la integren sin acoplarse.
 */
public final class Bomba {

    private final Set<Integer> digitosObjetivo;
    private final List<String> expresiones;
    private final Automata automata;

    /** @param cedulas cédulas de los integrantes (solo dígitos); se toma el último dígito de cada una. */
    public Bomba(Collection<String> cedulas) {
        this(digitosDe(cedulas));
    }

    private Bomba(Set<Integer> digitos) {
        if (digitos.isEmpty()) {
            throw new IllegalArgumentException("La bomba necesita al menos una coordenada objetivo");
        }
        this.digitosObjetivo = Collections.unmodifiableSet(digitos);
        this.expresiones = digitos.stream().map(LenguajesOTAN::expresion).distinct().toList();
        this.automata = ConstructorAutomata.union(expresiones);
    }

    /** Crea la bomba directamente con los dígitos de las coordenadas objetivo (0-9). */
    public static Bomba deDigitos(Collection<Integer> digitos) {
        if (digitos == null) {
            throw new IllegalArgumentException("Indique las coordenadas objetivo");
        }
        Set<Integer> conjunto = new LinkedHashSet<>();
        for (Integer d : digitos) {
            LenguajesOTAN.expresion(d == null ? -1 : d);
            conjunto.add(d);
        }
        return new Bomba(conjunto);
    }

    private static Set<Integer> digitosDe(Collection<String> cedulas) {
        if (cedulas == null) {
            throw new IllegalArgumentException("Indique las cédulas de los integrantes");
        }
        Set<Integer> digitos = new LinkedHashSet<>();
        for (String cedula : cedulas) {
            digitos.add(LenguajesOTAN.digitoObjetivo(cedula));
        }
        return digitos;
    }

    /** True si el tipo de arsenal (palabra w) pertenece a L. */
    public boolean evaluar(String tipoArsenal) {
        return automata.acepta(tipoArsenal);
    }

    /** True si la geoposición ("Coord-05") es una coordenada objetivo. */
    public boolean esObjetivo(String geoposicion) {
        return digitosObjetivo.contains(LenguajesOTAN.digito(geoposicion));
    }

    /** Decisión de la bomba para una línea de Grupo##.csv: objetivo y w ∈ L. */
    public boolean debeExplotar(String geoposicion, String tipoArsenal) {
        return esObjetivo(geoposicion) && evaluar(tipoArsenal);
    }

    /** "L = { abcdt+ , ab* }" */
    public String lenguaje() {
        return "L = { " + String.join(" , ", expresiones) + " }";
    }

    /** ["Coord-05", "Coord-09"] en el orden de los integrantes. */
    public List<String> coordenadasObjetivo() {
        List<String> lista = new ArrayList<>();
        digitosObjetivo.forEach(d -> lista.add(LenguajesOTAN.geoposicion(d)));
        return lista;
    }

    public Set<Integer> getDigitosObjetivo() {
        return digitosObjetivo;
    }

    public Automata getAutomata() {
        return automata;
    }
}
