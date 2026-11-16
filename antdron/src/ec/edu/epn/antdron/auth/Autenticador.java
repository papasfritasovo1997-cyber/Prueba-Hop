package ec.edu.epn.antdron.auth;

import java.io.PrintStream;
import java.util.Optional;

import ec.edu.epn.antdron.config.Claves;
import ec.edu.epn.antdron.config.Equipo;
import ec.edu.epn.antdron.config.Integrante;

/**
 * Autentica a los integrantes del grupo (usuario y clave de config/integrantes.csv) y al profesor
 * ("patmic", "123"), con un máximo de 3 intentos. Lo usan la consola y la pantalla JavaFX.
 */
public final class Autenticador {

    public static final int MAX_INTENTOS = 3;
    public static final String USUARIO_PROFESOR = "patmic";
    private static final String CLAVE_PROFESOR = "123";

    /** Resultado de un intento. */
    public enum Resultado { ACEPTADO, RECHAZADO, BLOQUEADO }

    private final Equipo equipo;
    private int intentosFallidos;

    public Autenticador(Equipo equipo) {
        this.equipo = equipo;
    }

    /** Valida sin contar intentos. */
    public boolean validar(String usuario, String clave) {
        if (usuario == null || clave == null) {
            return false;
        }
        String u = usuario.trim();
        if (u.equals(USUARIO_PROFESOR)) {
            return clave.equals(CLAVE_PROFESOR);
        }
        Optional<Integrante> i = equipo.buscarUsuario(u);
        return i.isPresent() && i.get().puedeIngresar() && Claves.coincide(clave, i.get().clave());
    }

    /** Cuenta el intento; tras 3 fallos queda bloqueado aunque la clave sea correcta. */
    public Resultado intentar(String usuario, String clave) {
        if (estaBloqueado()) {
            return Resultado.BLOQUEADO;
        }
        if (validar(usuario, clave)) {
            return Resultado.ACEPTADO;
        }
        intentosFallidos++;
        return estaBloqueado() ? Resultado.BLOQUEADO : Resultado.RECHAZADO;
    }

    public boolean estaBloqueado() {
        return intentosFallidos >= MAX_INTENTOS;
    }

    public int intentosRestantes() {
        return Math.max(0, MAX_INTENTOS - intentosFallidos);
    }

    /** Nombre para el saludo: el del integrante o "Profesor". */
    public String nombreDe(String usuario) {
        if (USUARIO_PROFESOR.equals(usuario)) {
            return "Profesor (" + USUARIO_PROFESOR + ")";
        }
        return equipo.buscarUsuario(usuario).map(Integrante::nombre).orElse(usuario);
    }

    /** Flujo de consola. @return el usuario autenticado, o vacío si agotó los intentos o se cerró la entrada. */
    public Optional<String> autenticar(EntradaUsuario entrada, PrintStream salida) {
        salida.println("[+] AUTENTICACIÓN (máximo " + MAX_INTENTOS + " intentos)");
        while (!estaBloqueado()) {
            String usuario = entrada.leerLinea("Usuario: ");
            if (usuario == null) {
                salida.println();
                salida.println("Entrada cerrada: autenticación cancelada.");
                return Optional.empty();
            }
            String clave = entrada.leerClave("Contraseña: ");
            switch (intentar(usuario, clave)) {
                case ACEPTADO -> {
                    salida.println("Bienvenido, " + nombreDe(usuario.trim()) + ".");
                    return Optional.of(usuario.trim());
                }
                case RECHAZADO -> salida.println("Usuario o contraseña incorrectos. Intentos restantes: "
                        + intentosRestantes());
                case BLOQUEADO -> salida.println("Acceso denegado: se superó el máximo de " + MAX_INTENTOS
                        + " intentos.");
                default -> throw new IllegalStateException();
            }
        }
        return Optional.empty();
    }
}
