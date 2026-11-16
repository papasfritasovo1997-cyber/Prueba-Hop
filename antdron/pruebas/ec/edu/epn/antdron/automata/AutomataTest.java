package ec.edu.epn.antdron.automata;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import ec.edu.epn.xp.pruebas.Historia;
import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Prueba;

@Historia("HU-06")
class AutomataTest {

    private static final String SIGMA = "abcdt";

    /** Todas las palabras sobre {a,b,c,d,t} de longitud 0..max. */
    private static List<String> palabras(int max) {
        List<String> todas = new ArrayList<>(List.of(""));
        List<String> nivel = List.of("");
        for (int n = 1; n <= max; n++) {
            List<String> siguiente = new ArrayList<>();
            for (String w : nivel) {
                for (char c : SIGMA.toCharArray()) {
                    siguiente.add(w + c);
                }
            }
            todas.addAll(siguiente);
            nivel = siguiente;
        }
        return todas;
    }

    @Prueba
    void cadaCoordenadaReconoceExactamenteSuLenguaje() {
        List<String> universo = palabras(7);
        for (int d = 0; d <= 9; d++) {
            String expresion = LenguajesOTAN.expresion(d);
            Automata a = ConstructorAutomata.desdeExpresion(expresion);
            // Las expresiones de la tabla OTAN también son expresiones regulares válidas de Java.
            for (String w : universo) {
                Afirmar.igual(Pattern.matches(expresion, w), a.acepta(w), expresion + " con w='" + w + "'");
            }
        }
    }

    @Prueba
    void ejemploDelEnunciado_coordenadas5y9() {
        Automata a = ConstructorAutomata.union(List.of("abcdt+", "ab*"));
        for (String w : List.of("abcdttt", "abcdt", "ab", "a", "abbbb")) {
            Afirmar.verdadero(a.acepta(w), w);
        }
        for (String w : List.of("", "abc", "abcd", "aa", "abcdtb", "b", "t", "abcdtta")) {
            Afirmar.falso(a.acepta(w), w);
        }
        Afirmar.falso(a.acepta(null));
        Pattern union = Pattern.compile("abcdt+|ab*");
        for (String w : palabras(7)) {
            Afirmar.igual(union.matcher(w).matches(), a.acepta(w), w);
        }
    }

    @Prueba
    void elAutomataEsMinimoYDeterminista() {
        Automata a = ConstructorAutomata.union(List.of("abcdt+", "ab*"));
        // q0 -a-> q1 -b-> q2 -b-> q3(b*) ; q2 -c-> q4 -d-> q5 -t-> q6(t*)
        Afirmar.igual(7, a.getNumeroEstados());
        Afirmar.igual(Set.of('a', 'b', 'c', 'd', 't'), a.getAlfabeto());
        Afirmar.igual(Set.of(1, 2, 3, 6), a.getFinales());
        Afirmar.igual(0, a.getInicial());
        Afirmar.igual(2, ConstructorAutomata.desdeExpresion("a+").getNumeroEstados());
        Afirmar.igual(ConstructorAutomata.union(List.of("a+", "a+")), ConstructorAutomata.desdeExpresion("a+"),
                "expresiones equivalentes producen el mismo AFD mínimo");
    }

    @Prueba
    void descripcionFormalYGramatica() {
        Automata a = ConstructorAutomata.union(List.of("abcdt+", "ab*"));
        String formal = a.descripcionFormal();
        Afirmar.verdadero(formal.contains("A = (Q, Σ, δ, q0, F)"));
        Afirmar.verdadero(formal.contains("Q  = {q0, q1, q2, q3, q4, q5, q6}"));
        Afirmar.verdadero(formal.contains("Σ  = {a, b, c, d, t}"));
        Afirmar.verdadero(formal.contains("F  = {q1, q2, q3, q6}"));
        Afirmar.verdadero(formal.contains("δ(q0, a) = q1"));

        Gramatica g = a.gramatica();
        Afirmar.igual("S", g.getInicial());
        Map<String, List<String>> p = g.getProducciones();
        Afirmar.igual(List.of("aA", "a"), p.get("S"));
        Afirmar.igual(List.of("bB", "b"), p.get("A"));
        Afirmar.igual(List.of("bC", "b", "cD"), p.get("B"));
        Afirmar.igual(List.of("tF", "t"), p.get("E"));
        Afirmar.verdadero(g.toString().contains("S → aA | a"));
    }

    @Prueba
    void gramaticaSinNoTerminalesImproductivos() {
        Gramatica g = ConstructorAutomata.desdeExpresion("abc").gramatica();
        Afirmar.igual(Map.of("S", List.of("aA"), "A", List.of("bB"), "B", List.of("c")), g.getProducciones());
        Afirmar.igual("T", Gramatica.noTerminal(19), "se salta la S");
    }

    @Prueba
    void exportaGrafoDotYMermaid() {
        Automata a = ConstructorAutomata.union(List.of("abcdt+", "ab*"));
        String dot = ExportadorGrafo.dot(a, "L = { abcdt+ , ab* }");
        Afirmar.verdadero(dot.startsWith("digraph AFD {"));
        Afirmar.verdadero(dot.contains("q6 [shape=doublecircle];"));
        Afirmar.verdadero(dot.contains("q0 [shape=circle];"));
        Afirmar.verdadero(dot.contains("inicio -> q0;"));
        Afirmar.verdadero(dot.contains("q6 -> q6 [label=\"t\"];"));

        String mermaid = ExportadorGrafo.mermaid(a);
        Afirmar.verdadero(mermaid.contains("[*] --> q0"));
        Afirmar.verdadero(mermaid.contains("q3 --> q3 : b"));
        Afirmar.verdadero(mermaid.contains("q6 --> [*]"));

        Automata ab = new Automata(2, Set.of('a', 'b'), Map.of(0, Map.of('a', 1, 'b', 1)), 0, Set.of(1));
        Afirmar.verdadero(ExportadorGrafo.dot(ab, "x").contains("q0 -> q1 [label=\"a,b\"];"));
    }

    @Prueba
    void validaExpresionesYDefinicion() {
        Afirmar.lanza(IllegalArgumentException.class, () -> ConstructorAutomata.desdeExpresion(""));
        Afirmar.lanza(IllegalArgumentException.class, () -> ConstructorAutomata.desdeExpresion("*a"));
        Afirmar.lanza(IllegalArgumentException.class, () -> ConstructorAutomata.desdeExpresion("a|b"));
        Afirmar.lanza(IllegalArgumentException.class, () -> ConstructorAutomata.union(List.of()));
        Afirmar.lanza(IllegalArgumentException.class,
                () -> new Automata(1, Set.of('a'), Map.of(0, Map.of('a', 5)), 0, Set.of()));
        Afirmar.lanza(IllegalArgumentException.class,
                () -> new Automata(1, Set.of('a'), Map.of(0, Map.of('z', 0)), 0, Set.of()));
    }

    @Prueba
    void lenguajesOTAN() {
        Afirmar.igual("abcdt+", LenguajesOTAN.expresion(5));
        Afirmar.igual("ab*", LenguajesOTAN.expresion(9));
        Afirmar.igual(5, LenguajesOTAN.digito("Coord-05"));
        Afirmar.igual(9, LenguajesOTAN.digito(" Coord-09 "));
        Afirmar.igual("Coord-00", LenguajesOTAN.geoposicion(0));
        Afirmar.verdadero(LenguajesOTAN.pertenece(6, "abcdttttt"));
        Afirmar.falso(LenguajesOTAN.pertenece(3, "abcd"));
        Afirmar.igual("Tanque", LenguajesOTAN.arsenal('t'));
        Afirmar.lanza(IllegalArgumentException.class, () -> LenguajesOTAN.expresion(10));
        Afirmar.lanza(IllegalArgumentException.class, () -> LenguajesOTAN.digito("Coord-5"));
        Afirmar.lanza(IllegalArgumentException.class, () -> LenguajesOTAN.digito(null));
        Afirmar.lanza(IllegalArgumentException.class, () -> LenguajesOTAN.arsenal('x'));
        Afirmar.igual(5, LenguajesOTAN.digitoObjetivo("1754265385"));
        Afirmar.igual(9, LenguajesOTAN.digitoObjetivo(" 1503639 "));
        Afirmar.lanza(IllegalArgumentException.class, () -> LenguajesOTAN.digitoObjetivo("12AB"));
        Afirmar.lanza(IllegalArgumentException.class, () -> LenguajesOTAN.digitoObjetivo(null));
    }

    @Prueba
    void automataMainImprimeDefinicionGramaticaYGrafo() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ByteArrayOutputStream errores = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(errores, true, StandardCharsets.UTF_8);
        Afirmar.igual(0, AutomataMain.ejecutar(new String[] {"1103635", "1503639"}, out, err));
        String s = bytes.toString(StandardCharsets.UTF_8);
        Afirmar.verdadero(s.contains("Coordenadas objetivo: Coord-05, Coord-09"));
        Afirmar.verdadero(s.contains("L = { abcdt+ , ab* }"));
        Afirmar.verdadero(s.contains("S → aA | a"));
        Afirmar.verdadero(s.contains("digraph AFD {"));
        Afirmar.verdadero(s.contains("stateDiagram-v2"));
        Afirmar.igual(1, AutomataMain.ejecutar(new String[0], out, err));
        Afirmar.igual(1, AutomataMain.ejecutar(new String[] {"12AB"}, out, err));
        Afirmar.verdadero(errores.toString(StandardCharsets.UTF_8).contains("Cédula inválida"));
    }
}
