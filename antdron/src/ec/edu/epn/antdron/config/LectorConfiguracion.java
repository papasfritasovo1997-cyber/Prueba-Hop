package ec.edu.epn.antdron.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Lee la configuración que el equipo edita a mano:
 * <ul>
 *   <li>{@code config/equipo.properties}: grupo, nombre, metafora</li>
 *   <li>{@code config/integrantes.csv}: cedula;nombre;usuario;clave;rol (la cédula puede quedar vacía)</li>
 * </ul>
 * Los errores indican el archivo y la línea para que se puedan corregir sin leer el código.
 */
public final class LectorConfiguracion {

    public static final String ARCHIVO_EQUIPO = "equipo.properties";
    public static final String ARCHIVO_INTEGRANTES = "integrantes.csv";

    public Equipo leer(Path directorio) throws IOException {
        Path propiedades = directorio.resolve(ARCHIVO_EQUIPO);
        Path csv = directorio.resolve(ARCHIVO_INTEGRANTES);
        if (!Files.exists(propiedades)) {
            throw new IOException("No existe " + propiedades);
        }
        if (!Files.exists(csv)) {
            throw new IOException("No existe " + csv);
        }
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(propiedades, StandardCharsets.UTF_8)) {
            p.load(r);
        }
        String grupo = p.getProperty("grupo", "").trim();
        int numero;
        try {
            numero = Integer.parseInt(grupo);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ARCHIVO_EQUIPO + ": 'grupo' debe ser un número (ahora: '" + grupo + "')");
        }
        return new Equipo(numero, p.getProperty("nombre"), p.getProperty("metafora"),
                parsearIntegrantes(Files.readAllLines(csv, StandardCharsets.UTF_8)));
    }

    public List<Integrante> parsearIntegrantes(List<String> lineas) {
        List<Integrante> integrantes = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i).replace("\uFEFF", "").strip();
            if (linea.isEmpty() || linea.startsWith("#") || linea.toLowerCase().startsWith("cedula;")) {
                continue;
            }
            String[] c = linea.split(";", -1);
            if (c.length < 4 || c.length > 5) {
                throw new IllegalArgumentException(ARCHIVO_INTEGRANTES + " línea " + (i + 1)
                        + ": se esperaban 5 campos separados por ';' (cedula;nombre;usuario;clave;rol)");
            }
            try {
                integrantes.add(new Integrante(c[0], c[1], c[2], c[3], c.length == 5 ? c[4] : ""));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(ARCHIVO_INTEGRANTES + " línea " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
        return integrantes;
    }
}
