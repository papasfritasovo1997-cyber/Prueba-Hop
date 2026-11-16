package ec.edu.epn.xp.pruebas;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** Directorios temporales para las pruebas; el ejecutor los borra después de cada prueba. */
public final class Temporal {

    private static final List<Path> CREADOS = new ArrayList<>();

    private Temporal() {
    }

    public static synchronized Path directorio() {
        try {
            Path dir = Files.createTempDirectory("xp-prueba-");
            CREADOS.add(dir);
            return dir;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Borra todos los directorios creados hasta ahora. */
    static synchronized void limpiar() {
        for (Path dir : CREADOS) {
            try (Stream<Path> rutas = Files.walk(dir)) {
                rutas.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            } catch (IOException | UncheckedIOException e) {
                // Un archivo bloqueado no debe hacer fallar la prueba siguiente.
            }
        }
        CREADOS.clear();
    }
}
