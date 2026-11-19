package ec.edu.epn.antdron;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Historia;
import ec.edu.epn.xp.pruebas.Prueba;

/** Docker compila igual que VS Code y conserva los modos de la aplicación. */
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
        Afirmar.contiene(dockerfile, "javac -encoding UTF-8");
        Afirmar.contiene(dockerfile, "ec.edu.epn.xp.XpCli probar");
        Afirmar.contiene(dockerfile, "javafx-graphics-${JAVAFX}-linux.jar");
        Afirmar.contiene(dockerfile, "EXPOSE 6080");
        Afirmar.contiene(dockerfile, "novnc");
        String inicio = Files.readString(raiz().resolve("docker/iniciar.sh"));
        Afirmar.contiene(inicio, "Xvfb");
        Afirmar.contiene(inicio, "ec.edu.epn.antdron.App consola");
        Afirmar.contiene(inicio, "-Dprism.order=sw -cp \"$CP\" ec.edu.epn.antdron.App");
    }

    @Prueba
    void vsCodeCompilaLasMismasCarpetasQueDocker() throws IOException {
        String ajustes = Files.readString(raiz().resolve(".vscode/settings.json"));
        String dockerfile = Files.readString(raiz().resolve("Dockerfile"));
        for (String carpeta : new String[] {"framework-xp/src", "framework-xp/pruebas", "antdron/src", "antdron/pruebas"}) {
            Afirmar.contiene(ajustes, "\"" + carpeta + "\"");
            Afirmar.contiene(dockerfile, carpeta);
        }
        Afirmar.contiene(ajustes, "lib/javafx-graphics-21.0.5-win.jar");
    }

    @Prueba
    void vsCodeYDockerIncluyenElControladorSqlite() throws IOException {
        String jar = "lib/sqlite-jdbc-3.54.1.0.jar";
        Afirmar.verdadero(Files.isRegularFile(raiz().resolve(jar)), "falta " + jar);
        Afirmar.contiene(Files.readString(raiz().resolve(".vscode/settings.json")), jar);
        String dockerfile = Files.readString(raiz().resolve("Dockerfile"));
        Afirmar.contiene(dockerfile, "COPY " + jar + " lib/");
        Afirmar.contiene(dockerfile, "-cp 'bin:/opt/javafx/*:lib/*' ec.edu.epn.xp.XpCli probar");
        Afirmar.contiene(Files.readString(raiz().resolve("docker/iniciar.sh")), "CP=\"bin:/opt/javafx/*:lib/*\"");
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
