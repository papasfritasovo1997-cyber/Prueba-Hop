package ec.edu.epn.antdron.automata;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Exporta el grafo del autómata a Graphviz (DOT) y a Mermaid (se dibuja directamente en GitHub). */
public final class ExportadorGrafo {

    private ExportadorGrafo() {
    }

    public static String dot(Automata a, String titulo) {
        StringBuilder sb = new StringBuilder("digraph AFD {\n");
        sb.append("    rankdir=LR;\n");
        sb.append("    label=\"").append(titulo.replace("\"", "'")).append("\";\n");
        sb.append("    inicio [shape=point];\n");
        for (int q : a.estados()) {
            sb.append("    ").append(Automata.nombre(q)).append(" [shape=")
              .append(a.getFinales().contains(q) ? "doublecircle" : "circle").append("];\n");
        }
        sb.append("    inicio -> ").append(Automata.nombre(a.getInicial())).append(";\n");
        etiquetas(a).forEach((arco, simbolos) -> sb.append("    ").append(arco.replace("|", " -> "))
                .append(" [label=\"").append(simbolos).append("\"];\n"));
        return sb.append("}\n").toString();
    }

    public static String mermaid(Automata a) {
        StringBuilder sb = new StringBuilder("stateDiagram-v2\n    direction LR\n");
        sb.append("    [*] --> ").append(Automata.nombre(a.getInicial())).append('\n');
        etiquetas(a).forEach((arco, simbolos) -> sb.append("    ").append(arco.replace("|", " --> "))
                .append(" : ").append(simbolos).append('\n'));
        a.getFinales().forEach(f -> sb.append("    ").append(Automata.nombre(f)).append(" --> [*]\n"));
        return sb.toString();
    }

    /** Agrupa los símbolos que van del mismo origen al mismo destino: "a,b". */
    private static Map<String, String> etiquetas(Automata a) {
        Map<String, String> arcos = new TreeMap<>();
        a.transiciones().forEach((origen, t) -> t.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue, TreeMap::new,
                        Collectors.mapping(e -> String.valueOf(e.getKey()), Collectors.joining(","))))
                .forEach((destino, simbolos) ->
                        arcos.put(Automata.nombre(origen) + "|" + Automata.nombre(destino), simbolos)));
        return arcos;
    }
}
