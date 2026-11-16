package ec.edu.epn.xp.pruebas;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import ec.edu.epn.xp.ComandoXP;
import ec.edu.epn.xp.ContextoXP;

/**
 * {@code xp probar}: ejecuta todas las clases de prueba compiladas que estén en el classpath
 * (en VS Code, la carpeta de salida del proyecto), guarda los resultados por historia y escribe
 * el reporte de aceptación.
 */
public final class ComandoProbar implements ComandoXP {

    @Override
    public String nombre() {
        return "probar";
    }

    @Override
    public String descripcion() {
        return "Ejecuta todas las pruebas del classpath y escribe el reporte de aceptación";
    }

    @Override
    public int ejecutar(List<String> argumentos, ContextoXP contexto) throws IOException {
        if (!argumentos.isEmpty()) {
            throw new IllegalArgumentException("Uso: xp probar");
        }
        List<Path> carpetas = carpetasDeClases(System.getProperty("java.class.path", ""));
        if (carpetas.isEmpty()) {
            throw new IllegalStateException("No hay carpetas de clases compiladas en el classpath.");
        }
        Path resultados = contexto.ruta("resultados", "build/pruebas/resultados.csv");
        Files.deleteIfExists(resultados);

        EjecutorPruebas ejecutor = new EjecutorPruebas(contexto.getSalida());
        List<EjecutorPruebas.ResultadoPrueba> todos = new ArrayList<>();
        for (Path carpeta : carpetas) {
            todos.addAll(ejecutor.ejecutarDirectorio(carpeta));
        }
        EjecutorPruebas.registrar(todos, resultados);

        contexto.getSalida().println();
        new ComandoAceptacion().ejecutar(List.of("--escribir"), contexto);
        boolean ok = !todos.isEmpty() && todos.stream().allMatch(EjecutorPruebas.ResultadoPrueba::paso);
        contexto.getSalida().println(ok ? "\nTODAS LAS PRUEBAS PASARON" : "\nHAY PRUEBAS FALLIDAS (vea el detalle arriba)");
        return ok ? 0 : 1;
    }

    /** Las carpetas (no los .jar) del classpath, que es donde están las clases compiladas del proyecto. */
    static List<Path> carpetasDeClases(String classpath) {
        List<Path> carpetas = new ArrayList<>();
        for (String entrada : classpath.split(File.pathSeparator)) {
            if (!entrada.isBlank() && Files.isDirectory(Paths.get(entrada))) {
                carpetas.add(Paths.get(entrada));
            }
        }
        return carpetas;
    }
}
