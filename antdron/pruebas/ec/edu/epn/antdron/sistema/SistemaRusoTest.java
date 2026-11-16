package ec.edu.epn.antdron.sistema;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Prueba;
import ec.edu.epn.xp.pruebas.Temporal;

import ec.edu.epn.antdron.automata.LenguajesOTAN;
import ec.edu.epn.antdron.config.Equipo;
import ec.edu.epn.antdron.config.Integrante;
import ec.edu.epn.xp.pruebas.Historia;

class SistemaRusoTest {

    private static final Equipo EQUIPO = new Equipo(7, "Transformers", "", List.of(
            new Integrante("11036359", "Pepito Alimaña", "pepito", "h", "ENTRENADOR"),
            new Integrante("11036353", "Juana Galarza", "juana", "h", "CLIENTE")));

    @Prueba
    @Historia("HU-03")
    void generaUnaLineaPorDigitoConHorarioDeLaEstrategia() throws IOException {
        Path dir = Temporal.directorio();
        Path archivo = new GeneradorCsvGrupo().escribir(EQUIPO, dir);
        Afirmar.igual("Grupo07.csv", archivo.getFileName().toString());

        List<CoordenadaUK> coords = new SistemaRuso().readCoord(archivo.toString());
        Afirmar.igual(16, coords.size());
        // Igual al ejemplo del enunciado para la cédula 11036359.
        Afirmar.igual(List.of("Coord-01", "Coord-01", "Coord-00", "Coord-03", "Coord-06", "Coord-03", "Coord-05",
                "Coord-09"), coords.subList(0, 8).stream().map(CoordenadaUK::getGeoposicion).toList());
        Afirmar.igual("01-02", coords.get(0).getHorario(DiaSemana.LUNES));
        Afirmar.igual("", coords.get(2).getHorario(DiaSemana.LUNES));
        Afirmar.igual("03-06", coords.get(3).getHorario(DiaSemana.MIERCOLES));
        Afirmar.igual("04-08", coords.get(4).getHorario(DiaSemana.JUEVES));
        Afirmar.igual("05-10", coords.get(6).getHorario(DiaSemana.VIERNES));
        Afirmar.igual("01-02", coords.get(7).getHorario(DiaSemana.LUNES));
    }

    @Prueba
    @Historia("HU-03")
    void cadaPalabraPerteneceAlLenguajeDeSuCoordenada() throws IOException {
        Path dir = Temporal.directorio();
        Path archivo = new GeneradorCsvGrupo().escribir(EQUIPO, dir);
        for (CoordenadaUK c : new SistemaRuso().readCoord(archivo.toString())) {
            String expresion = LenguajesOTAN.expresion(c.getDigito());
            // Las expresiones de la tabla OTAN también son expresiones regulares válidas de Java.
            Afirmar.verdadero(Pattern.matches(expresion, c.getTipoArsenal()), c + " no está en " + expresion);
        }
        Random r = new Random(1);
        for (int i = 0; i < 200; i++) {
            for (int d = 0; d <= 9; d++) {
                String w = GeneradorCsvGrupo.palabra(LenguajesOTAN.expresion(d), r);
                Afirmar.verdadero(Pattern.matches(LenguajesOTAN.expresion(d), w), w);
            }
        }
    }

    @Prueba
    @Historia("HU-03")
    void elArchivoEsReproducible() {
        Afirmar.igual(new GeneradorCsvGrupo().generar(EQUIPO.cedulas()), new GeneradorCsvGrupo().generar(EQUIPO.cedulas()));
    }

    @Prueba
    @Historia("HU-04")
    void leeElFormatoDelEnunciado() {
        List<CoordenadaUK> c = new SistemaRuso().parsear(List.of(
                "Geoposición ; Lunes     ; Martes ; Miércoles   ; Jueves    ; Viernes    ; Tipo Arsenal",
                "Coord-01;       01-02 ;         ;              ;          ;            ; aaaa",
                "Coord-05;             ;         ;              ;          ; 05-10      ; abcdttt",
                ""));
        Afirmar.igual(2, c.size());
        Afirmar.igual("aaaa", c.get(0).getTipoArsenal());
        Afirmar.igual(5, c.get(1).getDigito());
        Afirmar.igual("05-10", c.get(1).getHorario(DiaSemana.VIERNES));
    }

    @Prueba
    @Historia("HU-04")
    void rechazaLineasInvalidas() {
        SistemaRuso s = new SistemaRuso();
        Afirmar.lanza(IllegalArgumentException.class, () -> s.parsear(List.of("Coord-01;a;b")));
        Afirmar.lanza(IllegalArgumentException.class, () -> s.parsear(List.of("Lugar-1;;;;;;a")));
        Afirmar.lanza(IOException.class, () -> s.readCoord("no-existe.csv"));
    }

    @Prueba
    @Historia("HU-04")
    void presentaCadaLineaConLoading() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        List<CoordenadaUK> c = new SistemaRuso().parsear(List.of(
                "Coord-03;;;03-06;;;abc", "Coord-09;01-02;;;;;ab"));
        new PresentadorCoordenadas(out, new Loading(out, 0)).presentar(c);
        String texto = bytes.toString(StandardCharsets.UTF_8);
        Afirmar.verdadero(texto.contains("[+] COORDENADAS UCRANIANAS:"));
        Afirmar.verdadero(texto.contains("Loading Geoposición"));
        for (String cuadro : List.of("\\ 0%", "| 10%", "/ 20%", "- 30%")) {
            Afirmar.verdadero(texto.contains(cuadro), "falta el cuadro " + cuadro);
        }
        Afirmar.verdadero(texto.contains("100%    Coord-03    |       |        | 03-06     |"));
        Afirmar.verdadero(texto.contains("100%    Coord-09    | 01-02 |"));
        Afirmar.verdadero(texto.contains("Total: 2 coordenadas"));
    }
}
