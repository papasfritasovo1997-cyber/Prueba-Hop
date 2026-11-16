package ec.edu.epn.xp;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import ec.edu.epn.xp.pruebas.Historia;
import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Prueba;
import ec.edu.epn.xp.pruebas.Temporal;

@Historia("XP-01")
class XpCliTest {


    Path raiz = Temporal.directorio();

    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errores = new ByteArrayOutputStream();

    /** Simula el comando que aportaría otro módulo (p. ej. "backlog"). */
    static final class ComandoEco implements ComandoXP {
        private final String nombre;

        ComandoEco(String nombre) {
            this.nombre = nombre;
        }

        @Override public String nombre() { return nombre; }
        @Override public String descripcion() { return "Repite los argumentos"; }

        @Override
        public int ejecutar(List<String> argumentos, ContextoXP contexto) {
            if (argumentos.contains("mal")) {
                throw new IllegalArgumentException("argumento inválido");
            }
            contexto.getSalida().println("eco " + argumentos + " en " + contexto.getRaiz().getFileName()
                    + " backlog=" + contexto.propiedad("backlog", "?"));
            return 0;
        }
    }

    private int ejecutar(XpCli cli, String... args) {
        String[] todos = new String[args.length + 2];
        todos[0] = "--raiz";
        todos[1] = raiz.toString();
        System.arraycopy(args, 0, todos, 2, args.length);
        return cli.ejecutar(todos, new PrintStream(salida, true, StandardCharsets.UTF_8),
                new PrintStream(errores, true, StandardCharsets.UTF_8));
    }

    private String out() {
        return salida.toString(StandardCharsets.UTF_8);
    }

    private String err() {
        return errores.toString(StandardCharsets.UTF_8);
    }

    @Prueba
    void ejecutaComandosDeExtensionConLaConfiguracionDelProyecto() throws IOException {
        Files.writeString(raiz.resolve("xp.properties"), "backlog=xp/historias.csv\n");
        XpCli cli = new XpCli(List.of(new ComandoEco("eco")));
        Afirmar.igual(0, ejecutar(cli, "eco", "a", "b"));
        Afirmar.verdadero(out().contains("eco [a, b] en " + raiz.getFileName() + " backlog=xp/historias.csv"));
    }

    @Prueba
    void ayudaListaNucleoYExtensiones() {
        XpCli cli = new XpCli(List.of(new ComandoEco("eco")));
        Afirmar.igual(0, ejecutar(cli, "ayuda"));
        Afirmar.verdadero(out().contains("directrices"));
        Afirmar.verdadero(out().contains("verificar"));
        Afirmar.verdadero(out().contains("eco"));
        Afirmar.igual(List.of("directrices", "eco", "verificar"), cli.nombresDeComandos());
    }

    @Prueba
    void sinComandoMuestraAyudaConCodigo1() {
        Afirmar.igual(1, ejecutar(new XpCli(List.of())));
        Afirmar.verdadero(out().contains("uso: xp"));
    }

    @Prueba
    void erroresDeUsoNoLanzanExcepciones() {
        XpCli cli = new XpCli(List.of(new ComandoEco("eco")));
        Afirmar.igual(1, ejecutar(cli, "desconocido"));
        Afirmar.verdadero(err().contains("Comando desconocido: desconocido"));
        Afirmar.igual(1, ejecutar(cli, "eco", "mal"));
        Afirmar.verdadero(err().contains("Error: argumento inválido"));
        Afirmar.igual(1, ejecutar(cli, "directrices", "FASE_X"));
        Afirmar.verdadero(err().contains("Fase desconocida"));
        Afirmar.igual(1, cli.ejecutar(new String[] {"--raiz"}, System.out,
                new PrintStream(errores, true, StandardCharsets.UTF_8)));
        Afirmar.igual(1, cli.ejecutar(new String[] {"--raiz", raiz.resolve("no-existe").toString(), "verificar"},
                System.out, new PrintStream(errores, true, StandardCharsets.UTF_8)));
    }

    @Prueba
    void elNucleoTienePrioridadYSeIgnoranDuplicadosONombresInvalidos() {
        XpCli cli = new XpCli(List.of(new ComandoEco("verificar"), new ComandoEco("eco"), new ComandoEco("eco"),
                new ComandoEco("Mal Nombre"), new ComandoEco("ayuda")));
        Afirmar.igual(List.of("directrices", "eco", "verificar"), cli.nombresDeComandos());
        ejecutar(cli, "ayuda");
        Afirmar.verdadero(err().contains("Comando duplicado 'verificar'"));
        Afirmar.verdadero(err().contains("Comando duplicado 'eco'"));
        Afirmar.verdadero(err().contains("nombre inválido: 'Mal Nombre'"));
        Afirmar.verdadero(err().contains("nombre inválido: 'ayuda'"));
    }

    @Prueba
    void elConstructorPorDefectoFuncionaSinModulosRegistrados() {
        Afirmar.falso(new XpCli().nombresDeComandos().isEmpty());
    }
}
