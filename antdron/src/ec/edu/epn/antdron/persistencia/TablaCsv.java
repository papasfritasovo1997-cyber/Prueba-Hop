package ec.edu.epn.antdron.persistencia;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Tabla guardada como archivo CSV separado por ';' con una cabecera. La primera columna es el id numérico.
 * Sin dependencias externas: suficiente para el hormiguero virtual de cada usuario.
 */
final class TablaCsv {

    private final Path archivo;
    private final String[] columnas;

    TablaCsv(Path archivo, String... columnas) {
        this.archivo = archivo;
        this.columnas = columnas;
        try {
            Files.createDirectories(archivo.toAbsolutePath().getParent());
            if (!Files.exists(archivo)) {
                Files.writeString(archivo, String.join(";", columnas) + "\n", StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear " + archivo, e);
        }
    }

    synchronized List<String[]> filas() {
        try {
            List<String[]> filas = new ArrayList<>();
            List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
            for (int i = 1; i < lineas.size(); i++) {
                if (!lineas.get(i).isBlank()) {
                    String[] f = lineas.get(i).split(";", -1);
                    if (f.length == columnas.length) {
                        filas.add(f);
                    }
                }
            }
            return filas;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + archivo, e);
        }
    }

    /** Agrega una fila con el siguiente id y lo devuelve. Los valores no incluyen el id. */
    synchronized int insertar(String... valores) {
        int id = filas().stream().mapToInt(f -> Integer.parseInt(f[0])).max().orElse(0) + 1;
        String[] fila = new String[valores.length + 1];
        fila[0] = String.valueOf(id);
        for (int i = 0; i < valores.length; i++) {
            fila[i + 1] = limpiar(valores[i]);
        }
        try {
            Files.writeString(archivo, String.join(";", fila) + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo escribir " + archivo, e);
        }
        return id;
    }

    /** Reescribe la fila con el id dado aplicando {@code cambio}. */
    synchronized void actualizar(int id, UnaryOperator<String[]> cambio) {
        List<String[]> filas = filas();
        StringBuilder sb = new StringBuilder(String.join(";", columnas)).append('\n');
        for (String[] f : filas) {
            String[] fila = Integer.parseInt(f[0]) == id ? cambio.apply(f.clone()) : f;
            for (int i = 0; i < fila.length; i++) {
                fila[i] = limpiar(fila[i]);
            }
            sb.append(String.join(";", fila)).append('\n');
        }
        try {
            Files.writeString(archivo, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo escribir " + archivo, e);
        }
    }

    private static String limpiar(String valor) {
        return valor == null ? "" : valor.replace(";", ",").replace("\n", " ").replace("\r", " ");
    }
}
