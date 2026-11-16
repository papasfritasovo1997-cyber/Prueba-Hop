package ec.edu.epn.antdron.automata;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Construye el AFD mínimo que reconoce un lenguaje del arsenal OTAN o la unión de varios.
 * <p>
 * Expresiones admitidas: secuencias de símbolos en minúscula, cada uno opcionalmente seguido de
 * '*' (cero o más) o '+' (una o más), p. ej. {@code a+}, {@code ab*}, {@code abcdt+}.
 * Pasos: expresión → AFN-ε → AFD (construcción de subconjuntos) → AFD mínimo (Moore).
 */
public final class ConstructorAutomata {

    private ConstructorAutomata() {
    }

    public static Automata desdeExpresion(String expresion) {
        return union(List.of(expresion));
    }

    /** L = L(e1) ∪ L(e2) ∪ ... */
    public static Automata union(List<String> expresiones) {
        if (expresiones == null || expresiones.isEmpty()) {
            throw new IllegalArgumentException("Se necesita al menos una expresión");
        }
        Afn afn = new Afn();
        int inicio = afn.nuevoEstado();
        for (String e : expresiones) {
            afn.epsilon(inicio, afn.agregarExpresion(e));
        }
        return minimizar(determinizar(afn, inicio));
    }

    // ---------------------------------------------------------------- AFN-ε

    private static final class Afn {
        final List<Map<Character, Set<Integer>>> trans = new ArrayList<>();
        final List<Set<Integer>> eps = new ArrayList<>();
        final Set<Integer> finales = new HashSet<>();
        final Set<Character> alfabeto = new TreeSet<>();

        int nuevoEstado() {
            trans.add(new HashMap<>());
            eps.add(new HashSet<>());
            return trans.size() - 1;
        }

        void arco(int desde, char simbolo, int hasta) {
            alfabeto.add(simbolo);
            trans.get(desde).computeIfAbsent(simbolo, k -> new HashSet<>()).add(hasta);
        }

        void epsilon(int desde, int hasta) {
            eps.get(desde).add(hasta);
        }

        /** Agrega el fragmento de la expresión y devuelve su estado inicial. */
        int agregarExpresion(String expresion) {
            String e = validar(expresion);
            int inicio = nuevoEstado();
            int actual = inicio;
            for (int i = 0; i < e.length(); i++) {
                char simbolo = e.charAt(i);
                char operador = i + 1 < e.length() ? e.charAt(i + 1) : ' ';
                int siguiente = nuevoEstado();
                if (operador == '*') {
                    // Estado nuevo con lazo: evita que x* comparta lazo con el símbolo anterior.
                    epsilon(actual, siguiente);
                    arco(siguiente, simbolo, siguiente);
                    i++;
                } else if (operador == '+') {
                    arco(actual, simbolo, siguiente);
                    arco(siguiente, simbolo, siguiente);
                    i++;
                } else {
                    arco(actual, simbolo, siguiente);
                }
                actual = siguiente;
            }
            finales.add(actual);
            return inicio;
        }

        Set<Integer> cierre(Set<Integer> estados) {
            Set<Integer> cierre = new TreeSet<>(estados);
            Deque<Integer> pendientes = new ArrayDeque<>(estados);
            while (!pendientes.isEmpty()) {
                for (int p : eps.get(pendientes.pop())) {
                    if (cierre.add(p)) {
                        pendientes.push(p);
                    }
                }
            }
            return cierre;
        }
    }

    static String validar(String expresion) {
        if (expresion == null || expresion.isBlank()) {
            throw new IllegalArgumentException("Expresión vacía");
        }
        String e = expresion.trim();
        if (!e.matches("([a-z][*+]?)+")) {
            throw new IllegalArgumentException("Expresión no soportada: '" + expresion
                    + "' (use símbolos a-z seguidos opcionalmente de * o +)");
        }
        return e;
    }

    // ------------------------------------------------- Subconjuntos (AFN → AFD)

    private static Automata determinizar(Afn afn, int inicio) {
        Map<Set<Integer>, Integer> indice = new LinkedHashMap<>();
        List<Set<Integer>> conjuntos = new ArrayList<>();
        Map<Integer, Map<Character, Integer>> delta = new TreeMap<>();
        Set<Integer> finales = new TreeSet<>();

        Set<Integer> inicial = afn.cierre(Set.of(inicio));
        indice.put(inicial, 0);
        conjuntos.add(inicial);
        for (int i = 0; i < conjuntos.size(); i++) {
            Set<Integer> actual = conjuntos.get(i);
            if (actual.stream().anyMatch(afn.finales::contains)) {
                finales.add(i);
            }
            for (char simbolo : afn.alfabeto) {
                Set<Integer> mover = new TreeSet<>();
                for (int q : actual) {
                    mover.addAll(afn.trans.get(q).getOrDefault(simbolo, Set.of()));
                }
                if (mover.isEmpty()) {
                    continue;
                }
                Set<Integer> destino = afn.cierre(mover);
                Integer j = indice.get(destino);
                if (j == null) {
                    j = conjuntos.size();
                    indice.put(destino, j);
                    conjuntos.add(destino);
                }
                delta.computeIfAbsent(i, k -> new TreeMap<>()).put(simbolo, j);
            }
        }
        return new Automata(conjuntos.size(), afn.alfabeto, delta, 0, finales);
    }

    // ------------------------------------------------------- Minimización

    /** Refinamiento de particiones de Moore, quita estados muertos y renumera en orden BFS desde q0. */
    static Automata minimizar(Automata a) {
        List<Character> sigma = new ArrayList<>(a.getAlfabeto());
        int n = a.getNumeroEstados();
        int sumidero = n; // estado de rechazo explícito para completar δ
        int total = n + 1;

        int[] clase = new int[total];
        for (int q = 0; q < total; q++) {
            clase[q] = a.getFinales().contains(q) ? 1 : 0;
        }
        int clases = -1;
        while (true) {
            Map<List<Integer>, Integer> firmas = new HashMap<>();
            int[] nueva = new int[total];
            for (int q = 0; q < total; q++) {
                List<Integer> firma = new ArrayList<>();
                firma.add(clase[q]);
                for (char s : sigma) {
                    firma.add(clase[destino(a, q, s, sumidero)]);
                }
                nueva[q] = firmas.computeIfAbsent(firma, k -> firmas.size());
            }
            clase = nueva;
            if (firmas.size() == clases) {
                break;
            }
            clases = firmas.size();
        }

        // Clases vivas: desde ellas se alcanza un estado final.
        Set<Integer> vivas = new HashSet<>();
        for (int f : a.getFinales()) {
            vivas.add(clase[f]);
        }
        boolean cambio = true;
        while (cambio) {
            cambio = false;
            for (int q = 0; q < total; q++) {
                if (vivas.contains(clase[q])) {
                    continue;
                }
                for (char s : sigma) {
                    if (vivas.contains(clase[destino(a, q, s, sumidero)])) {
                        vivas.add(clase[q]);
                        cambio = true;
                        break;
                    }
                }
            }
        }

        // Renumeración BFS desde la clase inicial, recorriendo Σ en orden.
        int[] representante = new int[clases];
        Arrays.fill(representante, -1);
        for (int q = total - 1; q >= 0; q--) {
            representante[clase[q]] = q;
        }
        Map<Integer, Integer> numero = new LinkedHashMap<>();
        Deque<Integer> cola = new ArrayDeque<>();
        int claseInicial = clase[a.getInicial()];
        numero.put(claseInicial, 0);
        cola.add(claseInicial);
        Map<Integer, Map<Character, Integer>> delta = new TreeMap<>();
        Set<Integer> finales = new TreeSet<>();
        while (!cola.isEmpty()) {
            int c = cola.poll();
            int q = representante[c];
            if (a.getFinales().contains(q)) {
                finales.add(numero.get(c));
            }
            for (char s : sigma) {
                int destinoClase = clase[destino(a, q, s, sumidero)];
                if (!vivas.contains(destinoClase)) {
                    continue;
                }
                if (!numero.containsKey(destinoClase)) {
                    numero.put(destinoClase, numero.size());
                    cola.add(destinoClase);
                }
                delta.computeIfAbsent(numero.get(c), k -> new TreeMap<>()).put(s, numero.get(destinoClase));
            }
        }
        return new Automata(numero.size(), a.getAlfabeto(), delta, 0, finales);
    }

    private static int destino(Automata a, int q, char s, int sumidero) {
        if (q == sumidero) {
            return sumidero;
        }
        Integer p = a.transicion(q, s);
        return p == null ? sumidero : p;
    }
}
