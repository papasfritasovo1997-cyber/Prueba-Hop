package ec.edu.epn.antdron.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import ec.edu.epn.xp.desarrollo.Credenciales;
import ec.edu.epn.xp.desarrollo.Equipo;
import ec.edu.epn.xp.desarrollo.Integrante;
import ec.edu.epn.xp.desarrollo.RolXP;
import ec.edu.epn.xp.pruebas.Historia;

class AutenticadorTest {

    private static final Equipo EQUIPO = new Equipo(1, "Transformers", "", List.of(
            new Integrante("11036359", "Pepito Alimaña", "pepito", Credenciales.sha256("clave1"), RolXP.ENTRENADOR),
            new Integrante("11036353", "Juana Galarza", "juana", Credenciales.sha256("clave2"), RolXP.CLIENTE)));

    /** Entrada guionizada: alterna usuario y contraseña; null cuando se acaba. */
    static EntradaUsuario guion(String... lineas) {
        Deque<String> cola = new ArrayDeque<>(List.of(lineas));
        return new EntradaUsuario() {
            @Override
            public String leerLinea(String mensaje) {
                return cola.poll();
            }

            @Override
            public String leerClave(String mensaje) {
                return cola.poll();
            }
        };
    }

    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);

    @Test
    @Historia("HU-01")
    void aceptaIntegrantesYProfesor() {
        Autenticador a = new Autenticador(EQUIPO);
        assertTrue(a.validar("pepito", "clave1"));
        assertTrue(a.validar("juana", "clave2"));
        assertTrue(a.validar("patmic", "123"));
        assertFalse(a.validar("patmic", "1234"));
        assertFalse(a.validar("pepito", "clave2"));
        assertFalse(a.validar("nadie", "x"));
        assertFalse(a.validar(null, null));
    }

    @Test
    @Historia("HU-01")
    void permiteHastaTresIntentos() {
        Optional<String> u = new Autenticador(EQUIPO).autenticar(
                guion("x", "x", "pepito", "mal", "patmic", "123"), out);
        assertEquals(Optional.of("patmic"), u);
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("Intentos restantes: 1"));
    }

    @Test
    @Historia("HU-01")
    void bloqueaAlTercerIntentoFallido() {
        Optional<String> u = new Autenticador(EQUIPO).autenticar(
                guion("a", "1", "b", "2", "c", "3", "patmic", "123"), out);
        assertTrue(u.isEmpty(), "el cuarto intento no se debe evaluar");
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("se superó el máximo de 3 intentos"));
    }

    @Test
    @Historia("HU-01")
    void entradaCerradaNoProvocaError() {
        assertTrue(new Autenticador(EQUIPO).autenticar(guion(), out).isEmpty());
    }

    @Test
    @Historia("HU-02")
    void presentaElEquipoConElFormatoDelEnunciado() {
        PresentadorEquipo.presentar(EQUIPO, out);
        String s = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(s.contains("[+] TEAM [1]: Transformers"));
        assertTrue(s.contains("• Alumno 1: 11036359 | Pepito Alimaña"));
        assertTrue(s.contains("• Alumno 2: 11036353 | Juana Galarza"));
    }
}
