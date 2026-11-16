package ec.edu.epn.antdron.auth;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import ec.edu.epn.antdron.config.Claves;
import ec.edu.epn.antdron.config.Equipo;
import ec.edu.epn.antdron.config.Integrante;
import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Historia;
import ec.edu.epn.xp.pruebas.Prueba;

class AutenticadorTest {

    private static final Equipo EQUIPO = new Equipo(1, "Transformers", "", List.of(
            new Integrante("11036359", "Pepito Alimaña", "pepito", "clave1", "ENTRENADOR"),
            new Integrante("", "Juana Galarza", "juana", Claves.PREFIJO_SHA256 + Claves.sha256("clave2"), "CLIENTE"),
            new Integrante("", "Sin Usuario", "", "", "GESTOR")));

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

    private String texto() {
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Prueba
    @Historia("HU-01")
    void aceptaIntegrantesYProfesorConClavePlanaOHash() {
        Autenticador a = new Autenticador(EQUIPO);
        Afirmar.verdadero(a.validar("pepito", "clave1"));
        Afirmar.verdadero(a.validar("juana", "clave2"), "clave guardada como sha256:");
        Afirmar.verdadero(a.validar("patmic", "123"));
        Afirmar.falso(a.validar("patmic", "1234"));
        Afirmar.falso(a.validar("pepito", "clave2"));
        Afirmar.falso(a.validar("nadie", "x"));
        Afirmar.falso(a.validar("", ""), "un integrante sin usuario ni clave no puede ingresar");
        Afirmar.falso(a.validar(null, null));
        Afirmar.igual("Profesor (patmic)", a.nombreDe("patmic"));
        Afirmar.igual("Juana Galarza", a.nombreDe("juana"));
    }

    @Prueba
    @Historia("HU-01")
    void permiteHastaTresIntentos() {
        Optional<String> u = new Autenticador(EQUIPO).autenticar(
                guion("x", "x", "pepito", "mal", "patmic", "123"), out);
        Afirmar.igual(Optional.of("patmic"), u);
        Afirmar.contiene(texto(), "Intentos restantes: 1");
        Afirmar.contiene(texto(), "Bienvenido, Profesor (patmic).");
    }

    @Prueba
    @Historia("HU-01")
    void bloqueaAlTercerIntentoFallidoAunqueDespuesLaClaveSeaCorrecta() {
        Autenticador a = new Autenticador(EQUIPO);
        Optional<String> u = a.autenticar(guion("a", "1", "b", "2", "c", "3", "patmic", "123"), out);
        Afirmar.verdadero(u.isEmpty(), "el cuarto intento no se debe evaluar");
        Afirmar.contiene(texto(), "se superó el máximo de 3 intentos");
        Afirmar.igual(Autenticador.Resultado.BLOQUEADO, a.intentar("patmic", "123"));
        Afirmar.igual(0, a.intentosRestantes());
    }

    @Prueba
    @Historia("HU-01")
    void intentarCuentaLosFallosComoLaPantalla() {
        Autenticador a = new Autenticador(EQUIPO);
        Afirmar.igual(Autenticador.Resultado.RECHAZADO, a.intentar("pepito", "x"));
        Afirmar.igual(2, a.intentosRestantes());
        Afirmar.igual(Autenticador.Resultado.ACEPTADO, a.intentar("pepito", "clave1"));
        Afirmar.igual(Autenticador.Resultado.RECHAZADO, a.intentar("pepito", "y"));
        Afirmar.igual(Autenticador.Resultado.BLOQUEADO, a.intentar("pepito", "z"));
        Afirmar.verdadero(a.estaBloqueado());
    }

    @Prueba
    @Historia("HU-01")
    void entradaCerradaNoProvocaError() {
        Afirmar.verdadero(new Autenticador(EQUIPO).autenticar(guion(), out).isEmpty());
    }

    @Prueba
    @Historia("HU-02")
    void presentaElEquipoConElFormatoDelEnunciado() {
        List<String> l = PresentadorEquipo.lineas(EQUIPO);
        Afirmar.igual("[+] TEAM [1]: Transformers", l.get(0));
        Afirmar.igual("      • Alumno 1: 11036359 | Pepito Alimaña", l.get(1));
        Afirmar.igual("      • Alumno 2: (cédula pendiente) | Juana Galarza", l.get(2));
    }
}
