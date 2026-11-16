package ec.edu.epn.antdron.bomba;


import java.util.Arrays;
import java.util.List;
import java.util.Set;

import ec.edu.epn.xp.pruebas.Historia;
import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Prueba;

@Historia("HU-14")
class BombaTest {

    /** Ejemplo del enunciado: 1103635 y 1503639 → Coord-05 y Coord-09. */
    private final Bomba bomba = new Bomba(List.of("1103635", "1503639"));

    @Prueba
    void tomaElUltimoDigitoDeCadaCedula() {
        Afirmar.igual(Set.of(5, 9), bomba.getDigitosObjetivo());
        Afirmar.igual("L = { abcdt+ , ab* }", bomba.lenguaje());
        Afirmar.igual(List.of("Coord-05", "Coord-09"), bomba.coordenadasObjetivo());
    }

    @Prueba
    void reproduceLaTablaDeCoordenadasADestruirDelEnunciado() {
        String[][] csv = {
            {"Coord-01", "aaaa"}, {"Coord-01", "a"}, {"Coord-00", "aa"}, {"Coord-03", "abc"},
            {"Coord-06", "abcdttttt"}, {"Coord-03", "abc"}, {"Coord-05", "abcdttt"}, {"Coord-09", "ab"},
        };
        List<String> destruidas = Arrays.stream(csv)
                .filter(fila -> bomba.debeExplotar(fila[0], fila[1]))
                .map(fila -> fila[0] + " | " + fila[1])
                .toList();
        Afirmar.igual(List.of("Coord-05 | abcdttt", "Coord-09 | ab"), destruidas);
    }

    @Prueba
    void destruccionExclusiva() {
        // "a" pertenece a ab*, pero Coord-01 no es objetivo.
        Afirmar.verdadero(bomba.evaluar("a"));
        Afirmar.falso(bomba.debeExplotar("Coord-01", "a"));
        // Coord-05 es objetivo, pero "abc" no pertenece a L.
        Afirmar.falso(bomba.debeExplotar("Coord-05", "abc"));
        Afirmar.falso(bomba.evaluar(null));
        Afirmar.verdadero(bomba.esObjetivo("Coord-09"));
        Afirmar.falso(bomba.esObjetivo("Coord-06"));
    }

    @Prueba
    void digitosRepetidosNoDuplicanElLenguaje() {
        Bomba b = new Bomba(List.of("1234562", "1234569", "9999999"));
        Afirmar.igual(Set.of(2, 9), b.getDigitosObjetivo());
        Afirmar.igual("L = { ab* }", b.lenguaje());
        Afirmar.igual(List.of("Coord-02", "Coord-09"), b.coordenadasObjetivo());
        Afirmar.igual(Bomba.deDigitos(List.of(5, 9)).getAutomata(), bomba.getAutomata());
    }

    @Prueba
    void cedulaDelIntegrante1() {
        Bomba propia = new Bomba(List.of("1754265385"));
        Afirmar.igual(List.of("Coord-05"), propia.coordenadasObjetivo());
        Afirmar.igual("L = { abcdt+ }", propia.lenguaje());
        Afirmar.igual(6, propia.getAutomata().getNumeroEstados());
        Afirmar.verdadero(propia.debeExplotar("Coord-05", "abcdttt"));
        Afirmar.falso(propia.debeExplotar("Coord-09", "ab"));
    }

    @Prueba
    void validaLaEntrada() {
        Afirmar.lanza(IllegalArgumentException.class, () -> new Bomba(List.of()));
        Afirmar.lanza(IllegalArgumentException.class, () -> new Bomba(null));
        Afirmar.lanza(IllegalArgumentException.class, () -> new Bomba(List.of("11036A9")));
        Afirmar.lanza(IllegalArgumentException.class, () -> new Bomba(Arrays.asList("1103635", null)));
        Afirmar.lanza(IllegalArgumentException.class, () -> Bomba.deDigitos(List.of(10)));
        Afirmar.lanza(IllegalArgumentException.class, () -> Bomba.deDigitos(null));
        Afirmar.lanza(IllegalArgumentException.class, () -> bomba.debeExplotar("Coordenada 5", "ab"));
    }
}
