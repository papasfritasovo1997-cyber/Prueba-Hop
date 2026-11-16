package ec.edu.epn.antdron;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Historia;
import ec.edu.epn.xp.pruebas.Prueba;

/** Los archivos de Docker siguen alineados con la herramienta de construcción y los modos de la aplicación. */
@Historia("HU-13")
class DockerizacionTest {

    private static Path raiz() {
        Path dir = MotorAntdron.raizPorDefecto();
        while (dir != null && !Files.exists(dir.resolve("Dockerfile"))) {
            dir = dir.getParent();
        }
        Afirmar.noNulo(dir);
        return dir;
    }

    @Prueba
    void laImagenEjecutaLasPruebasYMuestraLaGuiEnElNavegador() throws IOException {
        String dockerfile = Files.readString(raiz().resolve("Dockerfile"));
        Afirmar.contiene(dockerfile, "java herramientas/Construir.java probar");
        Afirmar.contiene(dockerfile, "EXPOSE 6080");
        Afirmar.contiene(dockerfile, "novnc");
        String inicio = Files.readString(raiz().resolve("docker/iniciar.sh"));
        Afirmar.contiene(inicio, "Xvfb");
        Afirmar.contiene(inicio, "Construir.java ejecutar");
        Afirmar.contiene(inicio, "Construir.java consola");
    }

    @Prueba
    void composeMontaConfigYDatos() throws IOException {
        String compose = Files.readString(raiz().resolve("docker-compose.yml"));
        Afirmar.contiene(compose, "\"6080:6080\"");
        Afirmar.contiene(compose, "./config:/opt/antdron/config");
        Afirmar.contiene(compose, "./datos:/opt/antdron/datos");
        Afirmar.contiene(compose, "stdin_open: true");
    }
}
