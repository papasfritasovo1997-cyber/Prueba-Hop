package ec.edu.epn.antdron;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import ec.edu.epn.xp.pruebas.Historia;

/** Verifica que los archivos de Docker sigan alineados con el build (nombre del jar, modos y puertos). */
@Historia("HU-13")
class DockerizacionTest {

    private static Path raiz() {
        Path dir = Paths.get("").toAbsolutePath();
        while (dir != null && !Files.exists(dir.resolve("docker-compose.yml"))) {
            dir = dir.getParent();
        }
        assertTrue(dir != null, "no se encontró docker-compose.yml");
        return dir;
    }

    @Test
    void dockerfileCompilaPruebaYEjecutaElJarDeLaApp() throws IOException {
        String dockerfile = Files.readString(raiz().resolve("Dockerfile"));
        assertTrue(dockerfile.contains("mvn -B -q verify"), "la imagen debe pasar las pruebas");
        assertTrue(dockerfile.contains("/app/antdron/target/antdron.jar"));
        assertTrue(dockerfile.contains("ENTRYPOINT [\"java\", \"-jar\", \"app.jar\"]"));
        assertTrue(dockerfile.contains("EXPOSE 8080"));
        assertTrue(Files.exists(raiz().resolve("antdron/target/classes/ec/edu/epn/antdron/App.class")));
    }

    @Test
    void composeDefineWebYConsolaInteractiva() throws IOException {
        String compose = Files.readString(raiz().resolve("docker-compose.yml"));
        assertTrue(compose.contains("command: [\"web\"]"));
        assertTrue(compose.contains("\"8080:8080\""));
        assertTrue(compose.contains("command: [\"consola\"]"));
        assertTrue(compose.contains("stdin_open: true"));
        assertTrue(compose.contains("tty: true"));
    }
}
