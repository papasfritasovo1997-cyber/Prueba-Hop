package ec.edu.epn.antdron.sistema;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Sistema ruso que lee el archivo ucraniano hackeado (diagrama de clases del enunciado). */
public class SistemaRuso {

    public List<CoordenadaUK> readCoord(String fileName) throws IOException {
        Path archivo = Paths.get(fileName);
        if (!Files.exists(archivo)) {
            throw new IOException("No existe el archivo de coordenadas: " + archivo.toAbsolutePath());
        }
        return parsear(Files.readAllLines(archivo, StandardCharsets.UTF_8));
    }

    public List<CoordenadaUK> parsear(List<String> lineas) {
        List<CoordenadaUK> coordenadas = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i).strip();
            if (linea.isEmpty() || linea.startsWith("Geopos")) {
                continue;
            }
            String[] c = linea.split(";", -1);
            if (c.length != 7) {
                throw new IllegalArgumentException("Línea " + (i + 1) + " del CSV: se esperaban 7 columnas y hay "
                        + c.length);
            }
            Map<DiaSemana, String> horarios = new EnumMap<>(DiaSemana.class);
            DiaSemana[] dias = DiaSemana.values();
            for (int d = 0; d < dias.length; d++) {
                String horario = c[d + 1].trim();
                if (!horario.isEmpty()) {
                    horarios.put(dias[d], horario);
                }
            }
            try {
                coordenadas.add(new CoordenadaUK(c[0], horarios, c[6]));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Línea " + (i + 1) + " del CSV: " + e.getMessage(), e);
            }
        }
        return coordenadas;
    }
}
