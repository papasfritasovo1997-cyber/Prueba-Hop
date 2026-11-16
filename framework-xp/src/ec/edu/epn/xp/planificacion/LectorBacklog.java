package ec.edu.epn.xp.planificacion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lee el backlog (historias de usuario) desde un CSV separado por ';'.
 * Listas internas: responsables separados por ',' y criterios de aceptación por '|'.
 * Las líneas vacías y las que empiezan con '#' se ignoran.
 */
public final class LectorBacklog {

    public static final String CABECERA =
            "id;titulo;como;quiero;para;prioridad;puntos;iteracion;estado;responsables;criterios";
    private static final int CAMPOS = 11;

    public List<HistoriaUsuario> leer(Path archivo) throws IOException {
        return parsear(Files.readAllLines(archivo, StandardCharsets.UTF_8));
    }

    public List<HistoriaUsuario> parsear(List<String> lineas) {
        List<HistoriaUsuario> historias = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i).strip();
            if (linea.isEmpty() || linea.startsWith("#") || linea.toLowerCase().startsWith("id;")) {
                continue;
            }
            String[] c = linea.split(";", -1);
            if (c.length != CAMPOS) {
                throw new IllegalArgumentException("Backlog línea " + (i + 1) + ": se esperaban " + CAMPOS
                        + " campos y hay " + c.length);
            }
            try {
                historias.add(new HistoriaUsuario(c[0], c[1], c[2], c[3], c[4],
                        Prioridad.desde(c[5]), entero(c[6]), c[7].isBlank() ? 0 : entero(c[7]),
                        EstadoHistoria.desde(c[8]), lista(c[9], ","), lista(c[10], "\\|")));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Backlog línea " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
        return historias;
    }

    private static int entero(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("número inválido '" + texto + "'", e);
        }
    }

    private static List<String> lista(String texto, String separador) {
        if (texto.isBlank()) {
            return List.of();
        }
        return Arrays.stream(texto.split(separador)).map(String::strip).filter(s -> !s.isEmpty()).toList();
    }
}
