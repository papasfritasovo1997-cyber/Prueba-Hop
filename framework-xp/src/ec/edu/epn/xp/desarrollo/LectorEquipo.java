package ec.edu.epn.xp.desarrollo;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ec.edu.epn.xp.directrices.RolXP;

/**
 * Lee el equipo desde el directorio de configuración del proyecto:
 * {@code equipo.properties} (grupo, nombre, metafora) e {@code integrantes.csv}
 * ({@code cedula;nombre;usuario;clave;rol}). El framework no usa la clave.
 */
public final class LectorEquipo {

    public static final String ARCHIVO_EQUIPO = "equipo.properties";
    public static final String ARCHIVO_INTEGRANTES = "integrantes.csv";

    public Equipo leer(Path directorio) throws IOException {
        Path propiedades = directorio.resolve(ARCHIVO_EQUIPO);
        Path csv = directorio.resolve(ARCHIVO_INTEGRANTES);
        if (!Files.exists(propiedades) || !Files.exists(csv)) {
            throw new IOException("Falta la configuración del equipo en " + directorio
                    + " (" + ARCHIVO_EQUIPO + " y " + ARCHIVO_INTEGRANTES + ")");
        }
        Properties p = new Properties();
        try (StringReader r = new StringReader(leerTexto(propiedades))) {
            p.load(r);
        }
        int grupo;
        try {
            grupo = Integer.parseInt(p.getProperty("grupo", "").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ARCHIVO_EQUIPO + ": 'grupo' debe ser un número", e);
        }
        return new Equipo(grupo, p.getProperty("nombre"), p.getProperty("metafora"),
                parsearIntegrantes(leerTexto(csv).lines().toList()));
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
                        + ": se esperaban 5 campos (cedula;nombre;usuario;clave;rol)");
            }
            try {
                integrantes.add(new Integrante(c[0], c[1], c[2], RolXP.desde(c[4])));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(ARCHIVO_INTEGRANTES + " línea " + (i + 1) + ": "
                        + e.getMessage(), e);
            }
        }
        return integrantes;
    }

    /**
     * Lee un archivo editado a mano en Windows: acepta UTF-8 (con o sin BOM) y, si no lo es,
     * ANSI/windows-1252, que es como Excel guarda «CSV (delimitado por comas)».
     */
    static String leerTexto(Path archivo) throws IOException {
        byte[] bytes = Files.readAllBytes(archivo);
        String texto;
        try {
            texto = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            texto = new String(bytes, Charset.forName("windows-1252"));
        }
        return texto.startsWith("\uFEFF") ? texto.substring(1) : texto;
    }
}
