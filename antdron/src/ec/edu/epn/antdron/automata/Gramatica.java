package ec.edu.epn.antdron.automata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gramática regular lineal por la derecha G = (N, T, P, S) equivalente al autómata:
 * por cada δ(qi, x) = qj se agrega Qi → x Qj, y además Qi → x si qj es final.
 */
public final class Gramatica {

    private final Map<String, List<String>> producciones;
    private final List<Character> terminales;
    private final String inicial;

    private Gramatica(Map<String, List<String>> producciones, List<Character> terminales, String inicial) {
        this.producciones = producciones;
        this.terminales = terminales;
        this.inicial = inicial;
    }

    public static Gramatica desde(Automata a) {
        Map<String, List<String>> producciones = new LinkedHashMap<>();
        for (int q = 0; q < a.getNumeroEstados(); q++) {
            producciones.put(noTerminal(q), new ArrayList<>());
        }
        if (a.getFinales().contains(a.getInicial())) {
            producciones.get(noTerminal(a.getInicial())).add("ε");
        }
        a.transiciones().forEach((origen, t) -> t.forEach((simbolo, destino) -> {
            List<String> lado = producciones.get(noTerminal(origen));
            // Un destino sin salidas no genera nada más: basta la producción terminal.
            if (!a.transiciones().get(destino).isEmpty()) {
                lado.add(simbolo + noTerminal(destino));
            }
            if (a.getFinales().contains(destino)) {
                lado.add(String.valueOf(simbolo));
            }
        }));
        producciones.values().removeIf(List::isEmpty);
        return new Gramatica(producciones, new ArrayList<>(a.getAlfabeto()), noTerminal(a.getInicial()));
    }

    /** q0 → S, q1 → A, q2 → B, ... (se salta la S para no repetirla). */
    public static String noTerminal(int estado) {
        if (estado == 0) {
            return "S";
        }
        char letra = (char) ('A' + estado - 1);
        if (letra >= 'S') {
            letra++;
        }
        return letra <= 'Z' ? String.valueOf(letra) : "Q" + estado;
    }

    public Map<String, List<String>> getProducciones() {
        return producciones;
    }

    public String getInicial() {
        return inicial;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("G = (N, T, P, S)\n");
        sb.append("N = {").append(String.join(", ", producciones.keySet())).append("}\n");
        sb.append("T = {").append(terminales.stream().map(String::valueOf).collect(Collectors.joining(", ")))
          .append("}\n");
        sb.append("S = ").append(inicial).append('\n');
        sb.append("P:\n");
        producciones.forEach((nt, lado) -> sb.append("   ").append(nt).append(" → ")
                .append(String.join(" | ", lado)).append('\n'));
        return sb.toString();
    }
}
