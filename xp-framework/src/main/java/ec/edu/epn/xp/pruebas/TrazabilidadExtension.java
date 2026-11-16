package ec.edu.epn.xp.pruebas;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * Registra en {@code resultados.csv} (historia;prueba;resultado) cada prueba anotada con {@link Historia}.
 * Directorio configurable con la propiedad de sistema {@code xp.reporte.dir}.
 */
public class TrazabilidadExtension implements TestWatcher {

    public static final String PROPIEDAD_DIRECTORIO = "xp.reporte.dir";
    public static final String ARCHIVO = "resultados.csv";

    public static Path archivoResultados() {
        return Paths.get(System.getProperty(PROPIEDAD_DIRECTORIO, "target/xp-aceptacion")).resolve(ARCHIVO);
    }

    @Override
    public void testSuccessful(ExtensionContext contexto) {
        registrar(contexto, Resultado.PASO);
    }

    @Override
    public void testFailed(ExtensionContext contexto, Throwable causa) {
        registrar(contexto, Resultado.FALLO);
    }

    @Override
    public void testAborted(ExtensionContext contexto, Throwable causa) {
        registrar(contexto, Resultado.ABORTADA);
    }

    @Override
    public void testDisabled(ExtensionContext contexto, Optional<String> motivo) {
        registrar(contexto, Resultado.DESHABILITADA);
    }

    private void registrar(ExtensionContext contexto, Resultado resultado) {
        Optional<Historia> historia = contexto.getTestMethod()
                .flatMap(m -> AnnotationSupport.findAnnotation(m, Historia.class))
                .or(() -> contexto.getTestClass().flatMap(c -> AnnotationSupport.findAnnotation(c, Historia.class)));
        if (historia.isEmpty()) {
            return;
        }
        String prueba = contexto.getTestClass().map(Class::getSimpleName).orElse("?") + "#"
                + contexto.getTestMethod().map(m -> m.getName()).orElse(contexto.getDisplayName());
        StringBuilder lineas = new StringBuilder();
        for (String id : historia.get().value()) {
            lineas.append(id.trim()).append(';').append(prueba).append(';').append(resultado).append('\n');
        }
        escribir(lineas.toString());
    }

    private static synchronized void escribir(String lineas) {
        Path archivo = archivoResultados();
        try {
            Files.createDirectories(archivo.toAbsolutePath().getParent());
            Files.writeString(archivo, lineas, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo registrar el resultado de aceptación", e);
        }
    }
}
