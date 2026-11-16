package ec.edu.epn.antdron.auth;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import ec.edu.epn.xp.desarrollo.Credenciales;
import ec.edu.epn.xp.desarrollo.Equipo;
import ec.edu.epn.xp.desarrollo.Integrante;
import ec.edu.epn.xp.diseno.TarjetaCRC;

/** Autentica a los integrantes del grupo y al profesor ("patmic", "123") con máximo 3 intentos. */
@TarjetaCRC(responsabilidades = {"Validar usuario y contraseña", "Limitar a 3 intentos"},
        colaboradores = {"Equipo", "Credenciales", "EntradaUsuario"})
public final class Autenticador {

    public static final int MAX_INTENTOS = 3;
    public static final String USUARIO_PROFESOR = "patmic";
    private static final String HASH_PROFESOR = Credenciales.sha256("123");

    private final Map<String, String> credenciales = new LinkedHashMap<>();

    public Autenticador(Equipo equipo) {
        for (Integrante i : equipo.getIntegrantes()) {
            credenciales.put(i.getUsuario(), i.getClaveHash());
        }
        credenciales.put(USUARIO_PROFESOR, HASH_PROFESOR);
    }

    public boolean validar(String usuario, String clave) {
        if (usuario == null || clave == null) {
            return false;
        }
        return Credenciales.coincide(clave, credenciales.get(usuario.trim()));
    }

    /** @return el usuario autenticado, o vacío si agotó los intentos o se cerró la entrada. */
    public Optional<String> autenticar(EntradaUsuario entrada, PrintStream salida) {
        salida.println("[+] AUTENTICACIÓN (máximo " + MAX_INTENTOS + " intentos)");
        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            String usuario = entrada.leerLinea("Usuario: ");
            if (usuario == null) {
                salida.println();
                salida.println("Entrada cerrada: autenticación cancelada.");
                return Optional.empty();
            }
            String clave = entrada.leerClave("Contraseña: ");
            if (validar(usuario, clave)) {
                salida.println("Bienvenido, " + usuario.trim() + ".");
                return Optional.of(usuario.trim());
            }
            int restantes = MAX_INTENTOS - intento;
            salida.println(restantes > 0
                    ? "Usuario o contraseña incorrectos. Intentos restantes: " + restantes
                    : "Acceso denegado: se superó el máximo de " + MAX_INTENTOS + " intentos.");
        }
        return Optional.empty();
    }
}
