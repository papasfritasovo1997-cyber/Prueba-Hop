package ec.edu.epn.antdron.ui;

import java.util.List;
import java.util.Map;

import ec.edu.epn.antdron.automata.Automata;
import ec.edu.epn.antdron.automata.ConstructorAutomata;
import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Historia;
import ec.edu.epn.xp.pruebas.Prueba;

/** Ubicación de los estados en el grafo (no necesita abrir ventanas). */
@Historia("HU-06")
class LienzoAutomataTest {

    @Prueba
    void ubicaLosEstadosPorNivelesDesdeElInicial() {
        Automata a = ConstructorAutomata.union(List.of("abcdt+", "ab*"));
        Map<Integer, double[]> p = LienzoAutomata.posiciones(a);
        Afirmar.igual(a.getNumeroEstados(), p.size());
        Afirmar.verdadero(p.get(0)[0] < p.get(1)[0], "q1 a la derecha de q0");
        Afirmar.verdadero(p.get(1)[0] < p.get(2)[0]);
        Afirmar.igual(p.get(3)[0], p.get(4)[0], 0.001);
        Afirmar.verdadero(p.get(3)[1] != p.get(4)[1], "estados del mismo nivel en filas distintas");
        for (double[] xy : p.values()) {
            Afirmar.verdadero(xy[0] > 0 && xy[1] > 0);
        }
    }
}
