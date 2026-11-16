package ec.edu.epn.xp;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.TreeMap;

import ec.edu.epn.xp.directrices.ComandoDirectrices;
import ec.edu.epn.xp.directrices.ComandoVerificar;

/**
 * Línea de comandos del framework: {@code java -jar xp-framework.jar [--raiz DIR] <comando> [args]}.
 * Trae los comandos del núcleo y descubre los de los demás módulos con {@link ServiceLoader}.
 */
public final class XpCli {

    private final Map<String, ComandoXP> comandos = new TreeMap<>();
    private final List<String> avisos = new ArrayList<>();

    public XpCli() {
        this(cargarExtensiones());
    }

    /** Permite inyectar extensiones (pruebas). Los comandos del núcleo tienen prioridad. */
    public XpCli(Iterable<ComandoXP> extensiones) {
        registrar(new ComandoDirectrices(), true);
        registrar(new ComandoVerificar(), true);
        for (ComandoXP c : extensiones) {
            registrar(c, false);
        }
    }

    private static List<ComandoXP> cargarExtensiones() {
        List<ComandoXP> lista = new ArrayList<>();
        try {
            ServiceLoader.load(ComandoXP.class).forEach(lista::add);
        } catch (ServiceConfigurationError e) {
            // Un módulo mal registrado no debe impedir usar el resto del framework.
            System.err.println("Aviso: no se pudo cargar una extensión: " + e.getMessage());
        }
        return lista;
    }

    private void registrar(ComandoXP comando, boolean nucleo) {
        String nombre = comando.nombre();
        if (nombre == null || !nombre.matches("[a-z][a-z0-9-]*") || nombre.equals("ayuda")) {
            avisos.add("Se ignora el comando con nombre inválido: '" + nombre + "' (" + comando.getClass().getName() + ")");
            return;
        }
        ComandoXP previo = comandos.get(nombre);
        if (previo != null) {
            avisos.add("Comando duplicado '" + nombre + "': se usa " + previo.getClass().getName()
                    + " y se ignora " + comando.getClass().getName());
            return;
        }
        comandos.put(nombre, comando);
    }

    public static void main(String[] args) {
        int codigo = new XpCli().ejecutar(args, System.out, System.err);
        // Siempre se sale explícitamente: "probar" puede dejar hilos vivos de las pruebas.
        System.exit(codigo);
    }

    /** @return 0 éxito, 1 uso incorrecto o verificación fallida, 2 error al ejecutar. */
    public int ejecutar(String[] args, PrintStream out, PrintStream err) {
        avisos.forEach(a -> err.println("Aviso: " + a));
        Path raiz = Paths.get(".");
        List<String> resto = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--raiz")) {
                if (i + 1 >= args.length) {
                    err.println("Error: falta el directorio después de --raiz");
                    return 1;
                }
                raiz = Paths.get(args[++i]);
            } else {
                resto.add(args[i]);
            }
        }
        if (!Files.isDirectory(raiz)) {
            err.println("Error: la raíz del proyecto no existe: " + raiz.toAbsolutePath());
            return 1;
        }
        if (resto.isEmpty() || List.of("ayuda", "--help", "-h").contains(resto.get(0))) {
            ayuda(out);
            return resto.isEmpty() ? 1 : 0;
        }
        ComandoXP comando = comandos.get(resto.get(0));
        if (comando == null) {
            err.println("Comando desconocido: " + resto.get(0));
            ayuda(err);
            return 1;
        }
        try {
            return comando.ejecutar(List.copyOf(resto.subList(1, resto.size())), ContextoXP.cargar(raiz, out, err));
        } catch (IllegalArgumentException e) {
            err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException | UncheckedIOException e) {
            err.println("Error: " + e.getMessage());
            return 2;
        }
    }

    public List<String> nombresDeComandos() {
        return List.copyOf(comandos.keySet());
    }

    private void ayuda(PrintStream out) {
        out.println("Framework XP · uso: xp [--raiz DIR] <comando> [argumentos]");
        out.println();
        out.println("Comandos:");
        out.printf("  %-14s %s%n", "ayuda", "Muestra esta ayuda");
        comandos.forEach((nombre, c) -> out.printf("  %-14s %s%n", nombre, c.descripcion()));
        out.println();
        out.println("La configuración del proyecto se lee de <raíz>/" + ContextoXP.ARCHIVO_CONFIGURACION + ".");
    }
}
