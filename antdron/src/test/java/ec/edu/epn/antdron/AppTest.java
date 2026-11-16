package ec.edu.epn.antdron;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ec.edu.epn.antdron.auth.EntradaUsuario;
import ec.edu.epn.xp.desarrollo.Credenciales;
import ec.edu.epn.xp.pruebas.Historia;

/** Prueba de aceptación de extremo a extremo del modo consola. */
@Historia({"HU-01", "HU-02", "HU-03", "HU-04", "HU-06", "HU-07"})
class AppTest {

    @TempDir
    Path dir;

    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errores = new ByteArrayOutputStream();

    @BeforeEach
    void preparar() throws IOException {
        Path config = Files.createDirectories(dir.resolve("config"));
        Files.writeString(config.resolve("equipo.properties"), "grupo=1\nnombre=Transformers\nmetafora=x\n");
        Files.writeString(config.resolve("integrantes.csv"), String.join("\n",
                "cedula;nombre;usuario;claveSha256;rol",
                "1103635;Pepito Alimaña;pepito;" + Credenciales.sha256("1") + ";ENTRENADOR",
                "1503639;Juana Galarza;juana;" + Credenciales.sha256("2") + ";CLIENTE"));
    }

    private int ejecutar(List<String> teclado, String... args) {
        Deque<String> cola = new ArrayDeque<>(teclado);
        EntradaUsuario entrada = new EntradaUsuario() {
            @Override
            public String leerLinea(String m) {
                return cola.poll();
            }

            @Override
            public String leerClave(String m) {
                return cola.poll();
            }
        };
        Map<String, String> entorno = Map.of("ANTDRON_CONFIG", dir.resolve("config").toString(),
                "ANTDRON_DATA", dir.resolve("data").toString());
        return new App(new PrintStream(salida, true, StandardCharsets.UTF_8),
                new PrintStream(errores, true, StandardCharsets.UTF_8), entorno, entrada).ejecutar(args);
    }

    @Test
    void flujoCompletoConElProfesor() {
        assertEquals(0, ejecutar(List.of("patmic", "123")));
        String s = salida.toString(StandardCharsets.UTF_8);
        assertTrue(s.contains("Bienvenido, patmic."));
        assertTrue(s.contains("[+] TEAM [1]: Transformers"));
        assertTrue(s.contains("• Alumno 2: 1503639 | Juana Galarza"));
        assertTrue(s.contains("se generó a partir de las cédulas"));
        assertTrue(Files.exists(dir.resolve("data/Grupo01.csv")));
        assertTrue(s.contains("[+] COORDENADAS UCRANIANAS:"));
        assertTrue(s.contains("Total: 14 coordenadas"));
        assertTrue(s.contains("L = { abcdt+ , ab* }"));
        assertTrue(s.contains("Se transformó en: HSoldado"));
        assertTrue(s.contains("[+] COORDENADAS UCRANIANAS A DESTRUIR:"));
        assertTrue(s.contains("Coord-05    | "));
        assertTrue(s.contains("Coord-09    | "));
        assertTrue(s.contains("Idiomas: Inglés 100%, Español 33%"));
        assertTrue(s.contains("Misión completada."));
    }

    @Test
    void tresIntentosFallidosTerminanConCodigo1() {
        assertEquals(1, ejecutar(List.of("a", "b", "c", "d", "e", "f")));
        assertTrue(salida.toString(StandardCharsets.UTF_8).contains("Acceso denegado"));
    }

    @Test
    void modosAuxiliares() {
        assertEquals(0, ejecutar(List.of(), "generar-csv"));
        assertTrue(Files.exists(dir.resolve("data/Grupo01.csv")));
        assertEquals(0, ejecutar(List.of(), "automata"));
        String s = salida.toString(StandardCharsets.UTF_8);
        assertTrue(s.contains("A = (Q, Σ, δ, q0, F)"));
        assertTrue(s.contains("S → aA | a"));
        assertTrue(s.contains("digraph AFD"));
        assertTrue(s.contains("stateDiagram-v2"));
        assertEquals(0, ejecutar(List.of(), "hash", "123"));
        assertTrue(s.length() < salida.size());
        assertTrue(salida.toString(StandardCharsets.UTF_8).contains(Credenciales.sha256("123")));
        assertEquals(1, ejecutar(List.of(), "desconocido"));
    }

    @Test
    void configuracionFaltanteMuestraErrorSinExcepcion() throws IOException {
        Files.delete(dir.resolve("config/integrantes.csv"));
        assertEquals(2, ejecutar(List.of("patmic", "123")));
        assertTrue(errores.toString(StandardCharsets.UTF_8).contains("Falta la configuración del equipo"));
    }
}
