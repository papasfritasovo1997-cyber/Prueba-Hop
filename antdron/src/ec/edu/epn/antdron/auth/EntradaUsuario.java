package ec.edu.epn.antdron.auth;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/** Fuente de la entrada del usuario; permite probar el login sin teclado. Devuelve null si no hay más entrada. */
public interface EntradaUsuario {

    String leerLinea(String mensaje);

    String leerClave(String mensaje);

    /** Usa la consola del sistema (oculta la contraseña) o, si no hay terminal, la entrada estándar. */
    static EntradaUsuario delSistema(PrintStream salida) {
        Console consola = System.console();
        if (consola != null) {
            return new EntradaUsuario() {
                @Override
                public String leerLinea(String mensaje) {
                    return consola.readLine("%s", mensaje);
                }

                @Override
                public String leerClave(String mensaje) {
                    char[] clave = consola.readPassword("%s", mensaje);
                    return clave == null ? null : new String(clave);
                }
            };
        }
        BufferedReader lector = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        return new EntradaUsuario() {
            @Override
            public String leerLinea(String mensaje) {
                salida.print(mensaje);
                salida.flush();
                try {
                    return lector.readLine();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public String leerClave(String mensaje) {
                return leerLinea(mensaje);
            }
        };
    }
}
