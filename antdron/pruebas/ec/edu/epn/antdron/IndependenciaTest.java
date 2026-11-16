package ec.edu.epn.antdron;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Historia;
import ec.edu.epn.xp.pruebas.Prueba;

/**
 * La aplicación sigue las directrices XP pero no depende del framework: en VS Code todo se compila
 * junto, así que esta prueba impide que el código de antdron/src importe el framework.
 */
@Historia("XP-01")
class IndependenciaTest {

    @Prueba
    void laAplicacionNoImportaElFramework() throws IOException {
        Path fuentes = MotorAntdron.raizPorDefecto().resolve("antdron/src");
        Afirmar.verdadero(Files.isDirectory(fuentes), "no se encontró " + fuentes);
        List<String> infractores;
        try (Stream<Path> s = Files.walk(fuentes)) {
            infractores = s.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> leer(p).contains("ec.edu.epn.xp"))
                    .map(p -> fuentes.relativize(p).toString())
                    .toList();
        }
        Afirmar.igual(List.of(), infractores, "clases de la aplicación que usan el framework");
    }

    private static String leer(Path p) {
        try {
            return Files.readString(p);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
