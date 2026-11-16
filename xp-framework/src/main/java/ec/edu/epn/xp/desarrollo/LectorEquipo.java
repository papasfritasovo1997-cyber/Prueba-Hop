package ec.edu.epn.xp.desarrollo;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Carga el equipo desde un directorio de configuración:
 * <ul>
 *   <li>{@code equipo.properties}: grupo, nombre, metafora</li>
 *   <li>{@code integrantes.csv}: cedula;nombre;usuario;claveSha256;rol</li>
 * </ul>
 */
public final class LectorEquipo {

    public static final String ARCHIVO_EQUIPO = "equipo.properties";
    public static final String ARCHIVO_INTEGRANTES = "integrantes.csv";

    public Equipo leer(Path directorio) throws IOException {
        Path propiedades = directorio.resolve(ARCHIVO_EQUIPO);
        Path csv = directorio.resolve(ARCHIVO_INTEGRANTES);
        if (!Files.exists(propiedades) || !Files.exists(csv)) {
            throw new IOException("Falta la configuración del equipo en " + directorio.toAbsolutePath()
                    + " (" + ARCHIVO_EQUIPO + " y " + ARCHIVO_INTEGRANTES + ")");
        }
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(propiedades, StandardCharsets.UTF_8)) {
            p.load(r);
        }
        int grupo;
        try {
            grupo = Integer.parseInt(p.getProperty("grupo", "").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ARCHIVO_EQUIPO + ": 'grupo' debe ser un número", e);
        }
        return new Equipo(grupo, p.getProperty("nombre"), p.getProperty("metafora"),
                parsearIntegrantes(Files.readAllLines(csv, StandardCharsets.UTF_8)));
    }

    public List<Integrante> parsearIntegrantes(List<String> lineas) {
        List<Integrante> integrantes = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i).strip();
            if (linea.isEmpty() || linea.startsWith("#") || linea.toLowerCase().startsWith("cedula;")) {
                continue;
            }
            String[] c = linea.split(";", -1);
            if (c.length != 5) {
                throw new IllegalArgumentException(ARCHIVO_INTEGRANTES + " línea " + (i + 1)
                        + ": se esperaban 5 campos (cedula;nombre;usuario;claveSha256;rol)");
            }
            try {
                integrantes.add(new Integrante(c[0], c[1], c[2], c[3], RolXP.desde(c[4])));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(ARCHIVO_INTEGRANTES + " línea " + (i + 1) + ": "
                        + e.getMessage(), e);
            }
        }
        return integrantes;
    }
}
