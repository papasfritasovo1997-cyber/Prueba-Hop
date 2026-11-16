package ec.edu.epn.xp.pruebas;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ec.edu.epn.xp.planificacion.HistoriaUsuario;

/** Consolida {@code resultados.csv} en el estado de aceptación de cada historia de usuario. */
public final class ReporteAceptacion {

    public enum Estado { ACEPTADA, RECHAZADA, SIN_PRUEBAS }

    /** Resumen por historia. */
    public record Resumen(String historia, int pasadas, int fallidas, int otras) {
        public Estado estado() {
            if (pasadas + fallidas + otras == 0) {
                return Estado.SIN_PRUEBAS;
            }
            return fallidas == 0 && pasadas > 0 ? Estado.ACEPTADA : Estado.RECHAZADA;
        }
    }

    private ReporteAceptacion() {
    }

    /** Si una prueba aparece varias veces (ejecuciones repetidas) prevalece su último resultado. */
    public static Map<String, Resumen> resumir(List<String> lineas) {
        Map<String, Resultado> ultimo = new LinkedHashMap<>();
        for (String linea : lineas) {
            String[] c = linea.strip().split(";");
            if (c.length != 3) {
                continue;
            }
            ultimo.put(c[0] + ";" + c[1], Resultado.valueOf(c[2]));
        }
        Map<String, int[]> conteo = new TreeMap<>();
        ultimo.forEach((clave, resultado) -> {
            int[] n = conteo.computeIfAbsent(clave.substring(0, clave.indexOf(';')), k -> new int[3]);
            n[resultado == Resultado.PASO ? 0 : resultado == Resultado.FALLO ? 1 : 2]++;
        });
        Map<String, Resumen> resumen = new TreeMap<>();
        conteo.forEach((h, n) -> resumen.put(h, new Resumen(h, n[0], n[1], n[2])));
        return resumen;
    }

    /** Tabla Markdown; si se pasa el backlog, también lista las historias sin pruebas. */
    public static String markdown(Map<String, Resumen> resumen, List<HistoriaUsuario> backlog) {
        Map<String, Resumen> todas = new TreeMap<>(resumen);
        Map<String, String> titulos = new TreeMap<>();
        if (backlog != null) {
            for (HistoriaUsuario h : backlog) {
                titulos.put(h.getId(), h.getTitulo());
                todas.putIfAbsent(h.getId(), new Resumen(h.getId(), 0, 0, 0));
            }
        }
        StringBuilder sb = new StringBuilder("# Reporte de pruebas de aceptación\n\n");
        sb.append("| Historia | Título | Pasadas | Fallidas | Otras | Estado |\n|---|---|---|---|---|---|\n");
        todas.values().forEach(r -> sb.append("| ").append(r.historia()).append(" | ")
                .append(titulos.getOrDefault(r.historia(), "")).append(" | ")
                .append(r.pasadas()).append(" | ").append(r.fallidas()).append(" | ")
                .append(r.otras()).append(" | ").append(r.estado()).append(" |\n"));
        long aceptadas = todas.values().stream().filter(r -> r.estado() == Estado.ACEPTADA).count();
        sb.append("\nHistorias aceptadas: ").append(aceptadas).append('/').append(todas.size()).append('\n');
        return sb.toString();
    }

    public static String generar(Path resultados, List<HistoriaUsuario> backlog) throws IOException {
        if (!Files.exists(resultados)) {
            throw new IOException("No existe " + resultados + ": ejecute primero las pruebas (mvn verify)");
        }
        return markdown(resumir(Files.readAllLines(resultados, StandardCharsets.UTF_8)), backlog);
    }
}
