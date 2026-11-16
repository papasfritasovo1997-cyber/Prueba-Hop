package ec.edu.epn.antdron.automata;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Muestra el autómata para las cédulas del equipo:
 * {@code java -cp antdron/target/antdron.jar ec.edu.epn.antdron.automata.AutomataMain 1103635 1503639}.
 * Imprime la definición formal, la gramática y el grafo (Graphviz y Mermaid) para la documentación.
 */
public final class AutomataMain {

    private AutomataMain() {
    }

    public static void main(String[] args) {
        int codigo = ejecutar(args, System.out, System.err);
        if (codigo != 0) {
            System.exit(codigo);
        }
    }

    static int ejecutar(String[] cedulas, PrintStream out, PrintStream err) {
        if (cedulas.length == 0) {
            err.println("Uso: AutomataMain <cédula> [<cédula> ...]");
            return 1;
        }
        Set<Integer> digitos = new LinkedHashSet<>();
        try {
            for (String cedula : cedulas) {
                digitos.add(LenguajesOTAN.digitoObjetivo(cedula));
            }
        } catch (IllegalArgumentException e) {
            err.println("Error: " + e.getMessage());
            return 1;
        }
        List<String> coordenadas = new ArrayList<>();
        List<String> expresiones = new ArrayList<>();
        for (int d : digitos) {
            coordenadas.add(LenguajesOTAN.geoposicion(d));
            if (!expresiones.contains(LenguajesOTAN.expresion(d))) {
                expresiones.add(LenguajesOTAN.expresion(d));
            }
        }
        String lenguaje = "L = { " + String.join(" , ", expresiones) + " }";
        Automata a = ConstructorAutomata.union(expresiones);
        out.println("Coordenadas objetivo: " + String.join(", ", coordenadas));
        out.println(lenguaje);
        out.println();
        out.println(a.descripcionFormal());
        out.println(a.gramatica());
        out.println("--- Graphviz (DOT) ---");
        out.println(ExportadorGrafo.dot(a, lenguaje));
        out.println("--- Mermaid ---");
        out.print(ExportadorGrafo.mermaid(a));
        return 0;
    }
}
