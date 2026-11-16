package ec.edu.epn.xp.directrices;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import ec.edu.epn.xp.ComandoXP;
import ec.edu.epn.xp.ContextoXP;

/**
 * {@code xp verificar [fase]}: comprueba que exista el artefacto de cada práctica.
 * Un archivo cuenta si no está vacío y un directorio si contiene al menos un archivo.
 * Termina con 1 si falta alguno, para poder usarlo en la integración continua.
 */
public final class ComandoVerificar implements ComandoXP {

    /** Resultado de verificar una práctica. */
    public record Resultado(Practica practica, Optional<Path> artefacto, boolean cumple) {
        public boolean verificable() {
            return artefacto.isPresent();
        }
    }

    @Override
    public String nombre() {
        return "verificar";
    }

    @Override
    public String descripcion() {
        return "Comprueba que el proyecto tenga el artefacto de cada práctica XP (opcional: una fase)";
    }

    public static List<Resultado> verificar(ContextoXP contexto, List<Fase> fases) {
        List<Resultado> resultados = new ArrayList<>();
        for (Fase fase : fases) {
            for (Practica p : Practica.deFase(fase)) {
                Optional<Path> artefacto = contexto.artefacto(p);
                resultados.add(new Resultado(p, artefacto, artefacto.map(ComandoVerificar::existe).orElse(true)));
            }
        }
        return resultados;
    }

    static boolean existe(Path ruta) {
        try {
            if (Files.isRegularFile(ruta)) {
                return Files.size(ruta) > 0;
            }
            if (Files.isDirectory(ruta)) {
                try (Stream<Path> contenido = Files.walk(ruta)) {
                    return contenido.anyMatch(Files::isRegularFile);
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public int ejecutar(List<String> argumentos, ContextoXP contexto) {
        if (argumentos.size() > 1) {
            throw new IllegalArgumentException("Uso: xp verificar [PLANIFICACION|DISENO|DESARROLLO|PRUEBAS]");
        }
        List<Fase> fases = argumentos.isEmpty() ? Arrays.asList(Fase.values()) : List.of(Fase.desde(argumentos.get(0)));
        List<Resultado> resultados = verificar(contexto, fases);
        PrintStream out = contexto.getSalida();
        Fase actual = null;
        for (Resultado r : resultados) {
            if (r.practica().getFase() != actual) {
                actual = r.practica().getFase();
                out.println("== " + actual.titulo() + " ==");
            }
            if (!r.verificable()) {
                out.println("  [•] " + r.practica().getNombre() + " (directriz sin artefacto)");
            } else if (r.cumple()) {
                out.println("  [OK] " + r.practica().getNombre() + " → " + contexto.relativa(r.artefacto().get()));
            } else {
                out.println("  [FALTA] " + r.practica().getNombre() + " → " + contexto.relativa(r.artefacto().get())
                        + "  (responsable: " + r.practica().getResponsable().getEtiqueta() + ")");
            }
        }
        long verificables = resultados.stream().filter(Resultado::verificable).count();
        long cumplen = resultados.stream().filter(r -> r.verificable() && r.cumple()).count();
        out.println();
        out.println("Cumplimiento de directrices: " + cumplen + "/" + verificables);
        return cumplen == verificables ? 0 : 1;
    }
}
