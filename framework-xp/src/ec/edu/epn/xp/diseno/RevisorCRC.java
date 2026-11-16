package ec.edu.epn.xp.diseno;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Compara las clases de un directorio de fuentes con las tarjetas CRC documentadas.
 * Una tarjeta es una sección Markdown que empieza con {@code ### Clase: Nombre}.
 */
public final class RevisorCRC {

    private static final Pattern ENCABEZADO = Pattern.compile("^#{2,4}\\s*Clase:\\s*([A-Za-z_][A-Za-z0-9_]*)");

    /** Resultado de la revisión. */
    public record Revision(Set<String> clases, Set<String> conTarjeta, Set<String> sinTarjeta, Set<String> sobrantes) {
        public int cobertura() {
            return clases.isEmpty() ? 100 : Math.round(conTarjeta.size() * 100f / clases.size());
        }
    }

    private RevisorCRC() {
    }

    public static Set<String> tarjetas(List<String> lineas) {
        Set<String> nombres = new TreeSet<>();
        for (String l : lineas) {
            Matcher m = ENCABEZADO.matcher(l.strip());
            if (m.find()) {
                nombres.add(m.group(1));
            }
        }
        return nombres;
    }

    /** Clases principales: un archivo .java por clase, sin contar pruebas ni package-info. */
    public static Set<String> clases(Path fuentes) throws IOException {
        Set<String> nombres = new TreeSet<>();
        if (!Files.isDirectory(fuentes)) {
            throw new IllegalArgumentException("No existe el directorio de fuentes: " + fuentes);
        }
        try (Stream<Path> rutas = Files.walk(fuentes)) {
            rutas.map(p -> p.getFileName().toString())
                 .filter(n -> n.endsWith(".java") && !n.equals("package-info.java"))
                 .map(n -> n.substring(0, n.length() - 5))
                 .forEach(nombres::add);
        }
        return nombres;
    }

    public static Revision revisar(Path fuentes, Path archivoTarjetas) throws IOException {
        Set<String> clases = clases(fuentes);
        Set<String> tarjetas = Files.exists(archivoTarjetas)
                ? tarjetas(Files.readAllLines(archivoTarjetas, StandardCharsets.UTF_8)) : Set.of();
        Set<String> con = new TreeSet<>(clases);
        con.retainAll(tarjetas);
        Set<String> sin = new TreeSet<>(clases);
        sin.removeAll(tarjetas);
        Set<String> sobrantes = new TreeSet<>(tarjetas);
        sobrantes.removeAll(clases);
        return new Revision(clases, con, sin, sobrantes);
    }
}
