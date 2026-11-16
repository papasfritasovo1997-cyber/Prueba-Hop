package ec.edu.epn.xp.pruebas;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Historia("XP-05")
class ComandoProbarTest {

    @Prueba
    void soloBuscaPruebasEnLasCarpetasDelClasspath() throws IOException {
        Path dir = Temporal.directorio();
        Path clases = Files.createDirectories(dir.resolve("bin"));
        Path jar = Files.writeString(dir.resolve("javafx-base.jar"), "");
        String classpath = String.join(File.pathSeparator, clases.toString(), jar.toString(),
                dir.resolve("no-existe").toString(), "");
        Afirmar.igual(List.of(clases), ComandoProbar.carpetasDeClases(classpath));
    }
}
