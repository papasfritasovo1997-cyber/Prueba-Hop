package ec.edu.epn.xp.pruebas;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Historia("XP-05")
class EjecutorPruebasTest {

    /** Registro de lo que ejecutan las clases de muestra. */
    static final List<String> TRAZA = new ArrayList<>();

    @Historia("HU-99")
    static class Muestra {
        @AntesDeCada
        void preparar() {
            TRAZA.add("antes");
        }

        @DespuesDeCada
        void limpiar() {
            TRAZA.add("despues");
        }

        @Prueba
        void a_pasa() {
            TRAZA.add("a");
            Afirmar.igual(2, 1 + 1);
        }

        @Prueba
        @Historia({"HU-98", "HU-97"})
        void b_falla() {
            TRAZA.add("b");
            Afirmar.igual("x", "y", "comparación");
        }

        @Prueba
        void c_lanza() {
            throw new IllegalStateException("boom");
        }

        void noEsPrueba() {
            TRAZA.add("nunca");
        }
    }

    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final EjecutorPruebas ejecutor = new EjecutorPruebas(new PrintStream(bytes, true, StandardCharsets.UTF_8));

    @Prueba
    void ejecutaCadaPruebaConPreparacionYLimpiezaAunqueFalle() {
        TRAZA.clear();
        List<EjecutorPruebas.ResultadoPrueba> r = ejecutor.ejecutar(Muestra.class);
        Afirmar.igual(3, r.size());
        Afirmar.igual(List.of("antes", "a", "despues", "antes", "b", "despues", "antes", "despues"), TRAZA);
        Afirmar.verdadero(r.get(0).paso());
        Afirmar.falso(r.get(1).paso());
        Afirmar.contiene(r.get(1).detalle(), "comparación: se esperaba <x> pero fue <y>");
        Afirmar.contiene(r.get(2).detalle(), "IllegalStateException: boom");
        Afirmar.igual(List.of("HU-99"), r.get(0).historias());
        Afirmar.igual(List.of("HU-98", "HU-97"), r.get(1).historias(), "el método tiene prioridad");
        String salida = bytes.toString(StandardCharsets.UTF_8);
        Afirmar.contiene(salida, "✔ a_pasa");
        Afirmar.contiene(salida, "✘ b_falla");
    }

    @Prueba
    void registraResultadosPorHistoriaParaElReporte() throws IOException {
        Path archivo = Temporal.directorio().resolve("sub/resultados.csv");
        EjecutorPruebas.registrar(ejecutor.ejecutar(Muestra.class), archivo);
        List<String> lineas = Files.readAllLines(archivo);
        Afirmar.igual(List.of("HU-99;Muestra#a_pasa;PASO", "HU-98;Muestra#b_falla;FALLO",
                "HU-97;Muestra#b_falla;FALLO", "HU-99;Muestra#c_lanza;FALLO"), lineas);
        Afirmar.igual(ReporteAceptacion.Estado.RECHAZADA, ReporteAceptacion.resumir(lineas).get("HU-99").estado());
    }

    @Prueba
    void afirmacionesBasicas() {
        Afirmar.igual(1.0, 1.0004, 0.001);
        Afirmar.lanza(AssertionError.class, () -> Afirmar.igual(1.0, 1.1, 0.001));
        Afirmar.lanza(AssertionError.class, () -> Afirmar.verdadero(false));
        Afirmar.lanza(AssertionError.class, () -> Afirmar.falso(true));
        Afirmar.lanza(AssertionError.class, () -> Afirmar.noNulo(null));
        Afirmar.lanza(AssertionError.class, () -> Afirmar.contiene("abc", "z"));
        Afirmar.lanza(AssertionError.class, () -> Afirmar.noContiene("abc", "b"));
        Afirmar.lanza(AssertionError.class, () -> Afirmar.esInstancia(String.class, 3));
        Afirmar.lanza(AssertionError.class, () -> Afirmar.lanza(IllegalStateException.class, () -> { }));
        AssertionError e = Afirmar.lanza(AssertionError.class,
                () -> Afirmar.lanza(IllegalStateException.class, () -> {
                    throw new IllegalArgumentException("otra");
                }));
        Afirmar.contiene(e.getMessage(), "se lanzó IllegalArgumentException");
        Afirmar.igual("x", Afirmar.esInstancia(String.class, "x"));
    }

    @Prueba
    void temporalCreaDirectoriosQueSeBorranTrasLaPrueba() {
        Path d = Temporal.directorio();
        Afirmar.verdadero(Files.isDirectory(d));
        Temporal.limpiar();
        Afirmar.falso(Files.exists(d));
    }
}
