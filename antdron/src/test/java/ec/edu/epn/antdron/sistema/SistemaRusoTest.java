package ec.edu.epn.antdron.sistema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ec.edu.epn.xp.desarrollo.Equipo;
import ec.edu.epn.xp.desarrollo.Integrante;
import ec.edu.epn.xp.desarrollo.RolXP;
import ec.edu.epn.xp.pruebas.Historia;

class SistemaRusoTest {

    private static final Equipo EQUIPO = new Equipo(7, "Transformers", "", List.of(
            new Integrante("11036359", "Pepito Alimaña", "pepito", "h", RolXP.ENTRENADOR),
            new Integrante("11036353", "Juana Galarza", "juana", "h", RolXP.CLIENTE)));

    @Test
    @Historia("HU-03")
    void generaUnaLineaPorDigitoConHorarioDeLaEstrategia(@TempDir Path dir) throws IOException {
        Path archivo = new GeneradorCsvGrupo().escribir(EQUIPO, dir);
        assertEquals("Grupo07.csv", archivo.getFileName().toString());

        List<CoordenadaUK> coords = new SistemaRuso().readCoord(archivo.toString());
        assertEquals(16, coords.size());
        // Igual al ejemplo del enunciado para la cédula 11036359.
        assertEquals(List.of("Coord-01", "Coord-01", "Coord-00", "Coord-03", "Coord-06", "Coord-03", "Coord-05",
                "Coord-09"), coords.subList(0, 8).stream().map(CoordenadaUK::getGeoposicion).toList());
        assertEquals("01-02", coords.get(0).getHorario(DiaSemana.LUNES));
        assertEquals("", coords.get(2).getHorario(DiaSemana.LUNES));
        assertEquals("03-06", coords.get(3).getHorario(DiaSemana.MIERCOLES));
        assertEquals("04-08", coords.get(4).getHorario(DiaSemana.JUEVES));
        assertEquals("05-10", coords.get(6).getHorario(DiaSemana.VIERNES));
        assertEquals("01-02", coords.get(7).getHorario(DiaSemana.LUNES));
    }

    @Test
    @Historia("HU-03")
    void cadaPalabraPerteneceAlLenguajeDeSuCoordenada(@TempDir Path dir) throws IOException {
        Path archivo = new GeneradorCsvGrupo().escribir(EQUIPO, dir);
        for (CoordenadaUK c : new SistemaRuso().readCoord(archivo.toString())) {
            String expresion = EstrategiaOTAN.expresion(c.getDigito());
            // Las expresiones de la tabla OTAN también son expresiones regulares válidas de Java.
            assertTrue(Pattern.matches(expresion, c.getTipoArsenal()), c + " no está en " + expresion);
        }
        Random r = new Random(1);
        for (int i = 0; i < 200; i++) {
            for (int d = 0; d <= 9; d++) {
                String w = GeneradorCsvGrupo.palabra(EstrategiaOTAN.expresion(d), r);
                assertTrue(Pattern.matches(EstrategiaOTAN.expresion(d), w), w);
            }
        }
    }

    @Test
    @Historia("HU-03")
    void elArchivoEsReproducible() {
        assertEquals(new GeneradorCsvGrupo().generar(EQUIPO), new GeneradorCsvGrupo().generar(EQUIPO));
    }

    @Test
    @Historia("HU-04")
    void leeElFormatoDelEnunciado() {
        List<CoordenadaUK> c = new SistemaRuso().parsear(List.of(
                "Geoposición ; Lunes     ; Martes ; Miércoles   ; Jueves    ; Viernes    ; Tipo Arsenal",
                "Coord-01;       01-02 ;         ;              ;          ;            ; aaaa",
                "Coord-05;             ;         ;              ;          ; 05-10      ; abcdttt",
                ""));
        assertEquals(2, c.size());
        assertEquals("aaaa", c.get(0).getTipoArsenal());
        assertEquals(5, c.get(1).getDigito());
        assertEquals("05-10", c.get(1).getHorario(DiaSemana.VIERNES));
    }

    @Test
    @Historia("HU-04")
    void rechazaLineasInvalidas() {
        SistemaRuso s = new SistemaRuso();
        assertThrows(IllegalArgumentException.class, () -> s.parsear(List.of("Coord-01;a;b")));
        assertThrows(IllegalArgumentException.class, () -> s.parsear(List.of("Lugar-1;;;;;;a")));
        assertThrows(IOException.class, () -> s.readCoord("no-existe.csv"));
    }

    @Test
    @Historia("HU-04")
    void presentaCadaLineaConLoading() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        List<CoordenadaUK> c = new SistemaRuso().parsear(List.of(
                "Coord-03;;;03-06;;;abc", "Coord-09;01-02;;;;;ab"));
        new PresentadorCoordenadas(out, new Loading(out, 0)).presentar(c);
        String texto = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(texto.contains("[+] COORDENADAS UCRANIANAS:"));
        assertTrue(texto.contains("Loading Geoposición"));
        for (String cuadro : List.of("\\ 0%", "| 10%", "/ 20%", "- 30%")) {
            assertTrue(texto.contains(cuadro), "falta el cuadro " + cuadro);
        }
        assertTrue(texto.contains("100%    Coord-03    |       |        | 03-06     |"));
        assertTrue(texto.contains("100%    Coord-09    | 01-02 |"));
        assertTrue(texto.contains("Total: 2 coordenadas"));
    }
}
