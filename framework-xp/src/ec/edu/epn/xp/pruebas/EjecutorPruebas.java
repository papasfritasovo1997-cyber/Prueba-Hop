package ec.edu.epn.xp.pruebas;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Ejecutor de pruebas del framework (sustituye a JUnit para no depender de herramientas externas).
 * Busca en un directorio de clases compiladas las clases cuyo nombre termina en "Prueba" o "Test",
 * ejecuta sus métodos {@link Prueba} con una instancia nueva cada vez y registra el resultado
 * por historia en {@code resultados.csv} (historia;Clase#metodo;PASO|FALLO).
 * <p>
 * Uso: {@code java -cp <clases>:<pruebas> ec.edu.epn.xp.pruebas.EjecutorPruebas <dirPruebas> <resultados.csv>}
 */
public final class EjecutorPruebas {

    /** Resultado de una prueba. */
    public record ResultadoPrueba(String clase, String metodo, List<String> historias, boolean paso,
                                  String detalle, long milisegundos) {
    }

    private final PrintStream salida;

    public EjecutorPruebas(PrintStream salida) {
        this.salida = salida;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Uso: EjecutorPruebas <directorio-de-clases-de-prueba> <resultados.csv>");
            System.exit(2);
        }
        EjecutorPruebas ejecutor = new EjecutorPruebas(System.out);
        List<ResultadoPrueba> resultados = ejecutor.ejecutarDirectorio(Paths.get(args[0]));
        registrar(resultados, Paths.get(args[1]));
        long fallidas = resultados.stream().filter(r -> !r.paso()).count();
        System.exit(fallidas == 0 && !resultados.isEmpty() ? 0 : 1);
    }

    /** Ejecuta todas las clases de prueba compiladas en {@code directorio} (deben estar en el classpath). */
    public List<ResultadoPrueba> ejecutarDirectorio(Path directorio) throws IOException {
        List<String> nombres;
        try (Stream<Path> rutas = Files.walk(directorio)) {
            nombres = rutas.filter(p -> p.toString().endsWith(".class") && !p.getFileName().toString().contains("$"))
                    .map(p -> directorio.relativize(p).toString().replace('\\', '/').replace('/', '.'))
                    .map(n -> n.substring(0, n.length() - ".class".length()))
                    .filter(n -> n.endsWith("Prueba") || n.endsWith("Test"))
                    .sorted()
                    .toList();
        }
        List<Class<?>> clases = new ArrayList<>();
        for (String nombre : nombres) {
            try {
                clases.add(Class.forName(nombre, false, Thread.currentThread().getContextClassLoader()));
            } catch (ClassNotFoundException | LinkageError e) {
                salida.println("  ✘ No se pudo cargar " + nombre + ": " + e);
                clases.add(null);
            }
        }
        List<ResultadoPrueba> todos = new ArrayList<>();
        for (int i = 0; i < clases.size(); i++) {
            if (clases.get(i) == null) {
                todos.add(new ResultadoPrueba(nombres.get(i), "(carga)", List.of(), false, "no se pudo cargar", 0));
            } else {
                todos.addAll(ejecutar(clases.get(i)));
            }
        }
        resumen(todos);
        return todos;
    }

    public List<ResultadoPrueba> ejecutar(Class<?> clase) {
        List<Method> pruebas = Arrays.stream(clase.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Prueba.class))
                .sorted(Comparator.comparing(Method::getName))
                .toList();
        List<ResultadoPrueba> resultados = new ArrayList<>();
        if (pruebas.isEmpty()) {
            return resultados;
        }
        salida.println(clase.getSimpleName());
        List<Method> antes = anotados(clase, AntesDeCada.class);
        List<Method> despues = anotados(clase, DespuesDeCada.class);
        for (Method m : pruebas) {
            long inicio = System.nanoTime();
            String detalle = null;
            if (m.getParameterCount() != 0 || Modifier.isStatic(m.getModifiers())) {
                detalle = "un método @Prueba debe ser de instancia y sin parámetros";
            } else {
                detalle = invocar(clase, m, antes, despues);
            }
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            ResultadoPrueba r = new ResultadoPrueba(clase.getSimpleName(), m.getName(), historias(clase, m),
                    detalle == null, detalle, ms);
            salida.println((r.paso() ? "  ✔ " : "  ✘ ") + m.getName()
                    + (r.historias().isEmpty() ? "" : "  " + r.historias()) + (r.paso() ? "" : "\n      " + detalle));
            resultados.add(r);
        }
        return resultados;
    }

    /** @return null si pasó, o la descripción del fallo. */
    private static String invocar(Class<?> clase, Method prueba, List<Method> antes, List<Method> despues) {
        Object instancia;
        try {
            var constructor = clase.getDeclaredConstructor();
            constructor.setAccessible(true);
            instancia = constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            return "no se pudo crear la clase de prueba: " + causa(e);
        }
        String fallo = null;
        try {
            for (Method m : antes) {
                m.setAccessible(true);
                m.invoke(instancia);
            }
            prueba.setAccessible(true);
            prueba.invoke(instancia);
        } catch (InvocationTargetException e) {
            fallo = causa(e.getCause());
        } catch (ReflectiveOperationException e) {
            fallo = causa(e);
        } finally {
            for (Method m : despues) {
                try {
                    m.setAccessible(true);
                    m.invoke(instancia);
                } catch (ReflectiveOperationException e) {
                    fallo = fallo != null ? fallo : "falló @DespuesDeCada: " + causa(e);
                }
            }
            Temporal.limpiar();
        }
        return fallo;
    }

    private static String causa(Throwable t) {
        Throwable c = t instanceof InvocationTargetException ite && ite.getCause() != null ? ite.getCause() : t;
        String ubicacion = Arrays.stream(c.getStackTrace())
                .filter(e -> !e.getClassName().startsWith("java.") && !e.getClassName().startsWith("jdk.")
                        && !e.getClassName().startsWith("ec.edu.epn.xp.pruebas."))
                .findFirst().map(e -> "  (" + e.getFileName() + ":" + e.getLineNumber() + ")").orElse("");
        String tipo = c instanceof AssertionError ? "" : c.getClass().getSimpleName() + ": ";
        return tipo + c.getMessage() + ubicacion;
    }

    private static List<Method> anotados(Class<?> clase, Class<? extends java.lang.annotation.Annotation> a) {
        return Arrays.stream(clase.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(a)).toList();
    }

    private static List<String> historias(Class<?> clase, Method m) {
        Historia h = m.isAnnotationPresent(Historia.class) ? m.getAnnotation(Historia.class)
                : clase.getAnnotation(Historia.class);
        return h == null ? List.of() : Arrays.stream(h.value()).map(String::trim).toList();
    }

    private void resumen(List<ResultadoPrueba> resultados) {
        long pasadas = resultados.stream().filter(ResultadoPrueba::paso).count();
        long fallidas = resultados.size() - pasadas;
        salida.println();
        salida.println("Pruebas: " + resultados.size() + "  ✔ " + pasadas + "  ✘ " + fallidas);
        if (fallidas > 0) {
            salida.println("Fallidas:");
            resultados.stream().filter(r -> !r.paso())
                    .forEach(r -> salida.println("  - " + r.clase() + "#" + r.metodo() + ": " + r.detalle()));
        }
    }

    /** Agrega los resultados a {@code archivo} (formato que lee {@link ReporteAceptacion}). */
    public static void registrar(List<ResultadoPrueba> resultados, Path archivo) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (ResultadoPrueba r : resultados) {
            for (String h : r.historias()) {
                sb.append(h).append(';').append(r.clase()).append('#').append(r.metodo()).append(';')
                  .append(r.paso() ? Resultado.PASO : Resultado.FALLO).append('\n');
            }
        }
        Path padre = archivo.toAbsolutePath().getParent();
        if (padre != null) {
            Files.createDirectories(padre);
        }
        Files.writeString(archivo, sb.toString(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
