package ec.edu.epn.antdron.pkAntCiberDronContract;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Historia;
import ec.edu.epn.xp.pruebas.Prueba;
import ec.edu.epn.xp.pruebas.Temporal;

/** Reciclaje: el código común de CSV y textos vive en Utility y se comporta igual que antes. */
@Historia("HU-15")
class UtilityTest {

    @Prueba
    void separaCamposCsvConservandoLosVacios() {
        Afirmar.igual(3, Utility.camposCsv("a;;").length);
        Afirmar.igual("", Utility.camposCsv(";Ana;ana;1234;CLIENTE")[0]);
        Afirmar.igual("Ana", Utility.camposCsv(";Ana;ana;1234;CLIENTE")[1]);
    }

    @Prueba
    void ignoraVaciasComentariosYCabecera() {
        Afirmar.verdadero(Utility.esLineaIgnorable("   ", "cedula;"));
        Afirmar.verdadero(Utility.esLineaIgnorable("# comentario", "cedula;"));
        Afirmar.verdadero(Utility.esLineaIgnorable("\uFEFFCEDULA;nombre", "cedula;"));
        Afirmar.falso(Utility.esLineaIgnorable(";Ana;ana;1234;CLIENTE", "cedula;"));
    }

    @Prueba
    void normalizaSinTildesNiMayusculas() {
        Afirmar.igual("carnivoro", Utility.normalizar("  Carnívoro "));
        Afirmar.igual("zangano", Utility.normalizar("ZÁNGANO"));
    }

    @Prueba
    void leeArchivosGuardadosEnUtf8ConBomOEnWindows1252() throws Exception {
        Path dir = Temporal.directorio();
        Path utf8 = dir.resolve("utf8.csv");
        Files.writeString(utf8, "\uFEFFSebastián;Lugmaña");
        Afirmar.igual("Sebastián;Lugmaña", Utility.leerTexto(utf8));
        Path ansi = dir.resolve("ansi.csv");
        Files.write(ansi, "Sebastián;Lugmaña".getBytes(Charset.forName("windows-1252")));
        Afirmar.igual("Sebastián;Lugmaña", Utility.leerTexto(ansi));
    }
}
