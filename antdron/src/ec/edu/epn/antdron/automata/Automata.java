package ec.edu.epn.antdron.automata;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Autómata finito determinista A = (Q, Σ, δ, q0, F).
 * Los estados son enteros 0..n-1 (q0..qn-1). δ es parcial: una transición ausente lleva a un
 * estado de rechazo implícito, que no se dibuja.
 */
public final class Automata {

    private final int numeroEstados;
    private final Set<Character> alfabeto;
    private final Map<Integer, Map<Character, Integer>> delta;
    private final int inicial;
    private final Set<Integer> finales;

    public Automata(int numeroEstados, Set<Character> alfabeto, Map<Integer, Map<Character, Integer>> delta,
                    int inicial, Set<Integer> finales) {
        if (numeroEstados <= 0) {
            throw new IllegalArgumentException("El autómata necesita al menos un estado");
        }
        if (inicial < 0 || inicial >= numeroEstados) {
            throw new IllegalArgumentException("Estado inicial fuera de Q");
        }
        this.numeroEstados = numeroEstados;
        this.alfabeto = Collections.unmodifiableSet(new TreeSet<>(alfabeto));
        Map<Integer, Map<Character, Integer>> copia = new TreeMap<>();
        delta.forEach((q, trans) -> {
            validarEstado(q);
            Map<Character, Integer> t = new TreeMap<>();
            trans.forEach((simbolo, destino) -> {
                if (!this.alfabeto.contains(simbolo)) {
                    throw new IllegalArgumentException("Símbolo fuera de Σ: " + simbolo);
                }
                validarEstado(destino);
                t.put(simbolo, destino);
            });
            copia.put(q, Collections.unmodifiableMap(t));
        });
        this.delta = Collections.unmodifiableMap(copia);
        this.inicial = inicial;
        finales.forEach(this::validarEstado);
        this.finales = Collections.unmodifiableSet(new TreeSet<>(finales));
    }

    private void validarEstado(Integer q) {
        if (q == null || q < 0 || q >= numeroEstados) {
            throw new IllegalArgumentException("Estado fuera de Q: q" + q);
        }
    }

    /** Recorre la palabra con δ; acepta si termina en un estado final. */
    public boolean acepta(String palabra) {
        if (palabra == null) {
            return false;
        }
        int q = inicial;
        for (char simbolo : palabra.trim().toCharArray()) {
            Integer siguiente = transicion(q, simbolo);
            if (siguiente == null) {
                return false;
            }
            q = siguiente;
        }
        return finales.contains(q);
    }

    /** δ(q, simbolo), o null si no está definida. */
    public Integer transicion(int q, char simbolo) {
        Map<Character, Integer> t = delta.get(q);
        return t == null ? null : t.get(simbolo);
    }

    public Gramatica gramatica() {
        return Gramatica.desde(this);
    }

    /** Definición formal A = (Q, Σ, δ, q0, F) con la tabla de transiciones. */
    public String descripcionFormal() {
        StringBuilder sb = new StringBuilder();
        sb.append("A = (Q, Σ, δ, q0, F)\n");
        sb.append("Q  = {").append(estados().stream().map(Automata::nombre).collect(Collectors.joining(", ")))
          .append("}\n");
        sb.append("Σ  = {").append(alfabeto.stream().map(String::valueOf).collect(Collectors.joining(", ")))
          .append("}\n");
        sb.append("q0 = ").append(nombre(inicial)).append('\n');
        sb.append("F  = {").append(finales.stream().map(Automata::nombre).collect(Collectors.joining(", ")))
          .append("}\n");
        sb.append("δ:\n");
        transiciones().forEach((origen, t) -> t.forEach((simbolo, destino) ->
                sb.append("   δ(").append(nombre(origen)).append(", ").append(simbolo).append(") = ")
                  .append(nombre(destino)).append('\n')));
        return sb.toString();
    }

    public static String nombre(int estado) {
        return "q" + estado;
    }

    public Set<Integer> estados() {
        Set<Integer> q = new TreeSet<>();
        for (int i = 0; i < numeroEstados; i++) {
            q.add(i);
        }
        return q;
    }

    public int getNumeroEstados() { return numeroEstados; }
    public Set<Character> getAlfabeto() { return alfabeto; }
    public int getInicial() { return inicial; }
    public Set<Integer> getFinales() { return finales; }

    /** δ ordenada por estado de origen y símbolo. */
    public Map<Integer, Map<Character, Integer>> transiciones() {
        Map<Integer, Map<Character, Integer>> t = new LinkedHashMap<>();
        for (int q = 0; q < numeroEstados; q++) {
            t.put(q, delta.getOrDefault(q, Map.of()));
        }
        return t;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Automata a)) {
            return false;
        }
        return numeroEstados == a.numeroEstados && inicial == a.inicial && alfabeto.equals(a.alfabeto)
                && finales.equals(a.finales) && transiciones().equals(a.transiciones());
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroEstados, inicial, alfabeto, finales);
    }

    @Override
    public String toString() {
        return descripcionFormal();
    }
}
