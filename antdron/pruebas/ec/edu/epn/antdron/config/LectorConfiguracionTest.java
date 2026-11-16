package ec.edu.epn.antdron.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Historia;
import ec.edu.epn.xp.pruebas.Prueba;
import ec.edu.epn.xp.pruebas.Temporal;

@Historia("HU-15")
class LectorConfiguracionTest {

    private final LectorConfiguracion lector = new LectorConfiguracion();
    Path dir = Temporal.directorio();

    private void escribir(String propiedades, String... integrantes) throws IOException {
        Files.writeString(dir.resolve("equipo.properties"), propiedades);
        Files.writeString(dir.resolve("integrantes.csv"), "cedula;nombre;usuario;clave;rol\n"
                + String.join("\n", integrantes));
    }

    @Prueba
    void lasCedulasPuedenQuedarVacias() throws IOException {
        escribir("grupo=3\nnombre=Transformers\n", ";Integrante 1;i1;1234;ENTRENADOR", "1754265385;Ana;ana;x;CLIENTE");
        Equipo e = lector.leer(dir);
        Afirmar.igual(3, e.grupo());
        Afirmar.igual("Grupo03.csv", e.archivoCoordenadas());
        Afirmar.igual(List.of("1754265385"), e.cedulas());
        Afirmar.igual(1, e.cedulasPendientes());
        Afirmar.igual("(cédula pendiente)", e.integrantes().get(0).cedulaVisible());
        Afirmar.igual(5, e.integrantes().get(1).ultimoDigito());
        Afirmar.lanza(IllegalStateException.class, () -> e.integrantes().get(0).ultimoDigito());
    }

    @Prueba
    void ignoraComentariosLineasVaciasYBomDeExcel() throws IOException {
        Files.writeString(dir.resolve("equipo.properties"), "grupo=1\n");
        Files.writeString(dir.resolve("integrantes.csv"),
                "\uFEFFcedula;nombre;usuario;clave;rol\n# comentario\n\n 1503639 ;Juan; juan ;1;GESTOR\n");
        Equipo e = lector.leer(dir);
        Afirmar.igual(List.of("1503639"), e.cedulas());
        Afirmar.igual("juan", e.integrantes().get(0).usuario());
        Afirmar.igual("Grupo 1", e.nombre(), "nombre por defecto");
    }

    @Prueba
    void erroresIndicanArchivoYLinea() throws IOException {
        escribir("grupo=1\n", ";Ana;ana;1;CLIENTE", "17A4;Luis;luis;1;GESTOR");
        Afirmar.contiene(Afirmar.lanza(IllegalArgumentException.class, () -> lector.leer(dir)).getMessage(),
                "integrantes.csv línea 3: Cédula inválida '17A4'");
        escribir("grupo=1\n", "solo;dos");
        Afirmar.contiene(Afirmar.lanza(IllegalArgumentException.class, () -> lector.leer(dir)).getMessage(),
                "se esperaban 5 campos");
        escribir("grupo=uno\n", ";Ana;ana;1;CLIENTE");
        Afirmar.contiene(Afirmar.lanza(IllegalArgumentException.class, () -> lector.leer(dir)).getMessage(),
                "'grupo' debe ser un número");
        escribir("grupo=1\n", ";Ana;ana;1;CLIENTE", ";Luis;ana;2;GESTOR");
        Afirmar.contiene(Afirmar.lanza(IllegalArgumentException.class, () -> lector.leer(dir)).getMessage(),
                "usuarios repetidos");
        escribir("grupo=1\n");
        Afirmar.contiene(Afirmar.lanza(IllegalArgumentException.class, () -> lector.leer(dir)).getMessage(),
                "no tiene integrantes");
        Files.delete(dir.resolve("integrantes.csv"));
        Afirmar.lanza(IOException.class, () -> lector.leer(dir));
    }

    @Prueba
    void clavesEnTextoOEnSha256() {
        Afirmar.verdadero(Claves.coincide("1234", "1234"));
        Afirmar.verdadero(Claves.coincide("1234", "sha256:" + Claves.sha256("1234")));
        Afirmar.verdadero(Claves.coincide("1234", "SHA256:" + Claves.sha256("1234").toUpperCase()));
        Afirmar.falso(Claves.coincide("123", "1234"));
        Afirmar.falso(Claves.coincide("1234", ""));
        Afirmar.falso(Claves.coincide(null, "1234"));
    }
}
