package ec.edu.epn.antdron.pkAntCiberDronContract;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Utilidades compartidas (reciclaje): manejo de líneas CSV separadas por ';' y normalización de textos.
 * Antes este código estaba repetido en LectorConfiguracion, SistemaRuso, la tabla CSV del hormiguero y
 * FabricaAlimentos.
 */
public final class Utility {

    public static final String SEPARADOR_CSV = ";";

    private Utility() {
    }

    /** Campos de una línea CSV; conserva los campos vacíos del final ("a;;" → 3 campos). */
    public static String[] camposCsv(String linea) {
        return linea.split(SEPARADOR_CSV, -1);
    }

    /** Línea vacía, comentario ('#') o cabecera (empieza con {@code cabecera}, sin importar mayúsculas). */
    public static boolean esLineaIgnorable(String linea, String cabecera) {
        String l = quitarBom(linea).strip();
        return l.isEmpty() || l.startsWith("#")
                || (cabecera != null && l.toLowerCase(Locale.ROOT).startsWith(cabecera.toLowerCase(Locale.ROOT)));
    }

    public static String quitarBom(String texto) {
        return texto.startsWith("\uFEFF") ? texto.substring(1) : texto;
    }

    /**
     * Lee un archivo editado a mano en Windows: acepta UTF-8 (con o sin BOM) y, si no lo es,
     * ANSI/windows-1252, que es como Excel guarda «CSV (delimitado por comas)».
     */
    public static String leerTexto(Path archivo) throws IOException {
        byte[] bytes = Files.readAllBytes(archivo);
        String texto;
        try {
            texto = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            texto = new String(bytes, Charset.forName("windows-1252"));
        }
        return quitarBom(texto);
    }

    /** Sin tildes, sin espacios a los lados y en minúsculas: "  Carnívoro " → "carnivoro". */
    public static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
