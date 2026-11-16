package ec.edu.epn.antdron;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import ec.edu.epn.antdron.alimento.ANectar;
import ec.edu.epn.antdron.antciberdron.AntCiberDron;
import ec.edu.epn.antdron.antciberdron.AspectoLinguistico;
import ec.edu.epn.antdron.antciberdron.ExpertoParlante;
import ec.edu.epn.antdron.antciberdron.FuentePoder;
import ec.edu.epn.antdron.antciberdron.Idioma;
import ec.edu.epn.antdron.antciberdron.Laser;
import ec.edu.epn.antdron.antciberdron.Metralleta;
import ec.edu.epn.antdron.antciberdron.TurboReactor;
import ec.edu.epn.antdron.auth.Autenticador;
import ec.edu.epn.antdron.auth.EntradaUsuario;
import ec.edu.epn.antdron.auth.PresentadorEquipo;
import ec.edu.epn.antdron.automata.ExportadorGrafo;
import ec.edu.epn.antdron.bomba.Bomba;
import ec.edu.epn.antdron.hormiga.HLarva;
import ec.edu.epn.antdron.hormiga.Hormiga;
import ec.edu.epn.antdron.hormiga.TipoHormiga;
import ec.edu.epn.antdron.persistencia.BaseDatos;
import ec.edu.epn.antdron.persistencia.HormigueroServicio;
import ec.edu.epn.antdron.sistema.CoordenadaUK;
import ec.edu.epn.antdron.sistema.GeneradorCsvGrupo;
import ec.edu.epn.antdron.sistema.Loading;
import ec.edu.epn.antdron.sistema.PresentadorCoordenadas;
import ec.edu.epn.antdron.sistema.SistemaRuso;
import ec.edu.epn.antdron.web.ServidorWeb;
import ec.edu.epn.xp.desarrollo.Credenciales;
import ec.edu.epn.xp.desarrollo.Equipo;
import ec.edu.epn.xp.desarrollo.LectorEquipo;

/**
 * Punto de entrada. Modos:
 * <pre>
 *   consola       (por defecto) login → equipo → coordenadas → bomba → AntCiberDron
 *   web           pantallas del hormiguero virtual (puerto PORT, por defecto 8080)
 *   generar-csv   crea data/Grupo##.csv a partir de las cédulas del equipo
 *   automata      muestra la definición formal, la gramática y el grafo (DOT y Mermaid)
 *   hash CLAVE    imprime el SHA-256 de una clave para config/integrantes.csv
 * </pre>
 * Directorios configurables con ANTDRON_CONFIG, ANTDRON_DATA y ANTDRON_DB.
 */
public final class App {

    private final PrintStream out;
    private final PrintStream err;
    private final Map<String, String> entorno;
    private final EntradaUsuario entrada;

    public App(PrintStream out, PrintStream err, Map<String, String> entorno, EntradaUsuario entrada) {
        this.out = out;
        this.err = err;
        this.entorno = entorno;
        this.entrada = entrada;
    }

    public static void main(String[] args) {
        App app = new App(System.out, System.err, System.getenv(), EntradaUsuario.delSistema(System.out));
        int codigo = app.ejecutar(args);
        if (codigo != 0) {
            System.exit(codigo);
        }
    }

    public int ejecutar(String[] args) {
        String modo = args.length == 0 ? "consola" : args[0];
        try {
            return switch (modo) {
                case "consola" -> consola();
                case "web" -> web();
                case "generar-csv" -> generarCsv();
                case "automata" -> automata();
                case "hash" -> hash(args);
                case "ayuda", "--help", "-h" -> ayuda(0);
                default -> {
                    err.println("Modo desconocido: " + modo);
                    yield ayuda(1);
                }
            };
        } catch (IOException | SQLException | IllegalArgumentException | IllegalStateException e) {
            err.println("Error: " + e.getMessage());
            return 2;
        }
    }

    private int ayuda(int codigo) {
        out.println("Uso: java -jar antdron.jar [consola | web | generar-csv | automata | hash CLAVE]");
        return codigo;
    }

    private Path config() {
        return Paths.get(entorno.getOrDefault("ANTDRON_CONFIG", "config"));
    }

    private Path data() {
        return Paths.get(entorno.getOrDefault("ANTDRON_DATA", "data"));
    }

    private Equipo equipo() throws IOException {
        return new LectorEquipo().leer(config());
    }

    // -------------------------------------------------------------- Consola

    private int consola() throws IOException {
        Equipo equipo = equipo();
        out.println("==========================================================");
        out.println("  ANTDRON · KGD · AntCiberDron            Grupo " + equipo.numeroConFormato());
        out.println("==========================================================");

        Optional<String> usuario = new Autenticador(equipo).autenticar(entrada, out);
        if (usuario.isEmpty()) {
            return 1;
        }
        PresentadorEquipo.presentar(equipo, out);

        Path csv = data().resolve(GeneradorCsvGrupo.nombreArchivo(equipo));
        if (!Files.exists(csv)) {
            new GeneradorCsvGrupo().escribir(equipo, data());
            out.println();
            out.println("(No existía " + csv.getFileName() + ": se generó a partir de las cédulas del equipo)");
        }
        List<CoordenadaUK> coordenadas = new SistemaRuso().readCoord(csv.toString());
        new PresentadorCoordenadas(out, Loading.desdeSistema(out)).presentar(coordenadas);

        Bomba bomba = Bomba.paraEquipo(equipo);
        out.println();
        out.println("[+] BOMBA · coordenadas objetivo: " + bomba.coordenadasObjetivo());
        out.println("    " + bomba.lenguaje());

        TipoHormiga tipo = TipoHormiga.paraGrupo(equipo.getNumero());
        HLarva larva = new HLarva(tipo);
        out.println();
        out.println("[+] HORMIGA DEL GRUPO: " + tipo.getNombre() + " (vive con " + tipo.alimento() + ")");
        out.println("    Nace: " + larva);
        out.println("    Come Néctar → vive: " + larva.comer(new ANectar()));
        out.println("    Come " + tipo.alimento() + " → vive: " + larva.comer(tipo.alimento()));
        Hormiga piloto = larva.getEvolucion().orElseThrow();
        out.println("    Se transformó en: " + piloto);

        AntCiberDron dron = new AntCiberDron(piloto, bomba, coordenadas, new FuentePoder(100),
                new TurboReactor(2), out);
        dron.equiparMetralleta(new Metralleta(100));
        dron.equiparLaser(new Laser(500));
        dron.buscarObjetivos();

        out.println();
        out.println("[+] ANTCIBERDRON");
        out.println("    Extremidades: " + dron.getExtremidades().size() + " (las mismas de la hormiga)");
        out.println("    Pata delantera derecha: " + dron.dispararMetralleta());
        out.printf("    Pata delantera izquierda: %s%n", dron.dispararLaser());
        out.println("    " + dron.volar(20));
        out.printf("    Potencia del láser con %s: %.1f W%n", dron.getFuente(), dron.potenciaLaser());
        out.println("    " + dron.recargarFuente());
        out.println("    " + dron.reemplazarFuente(new FuentePoder(150)));

        ExpertoParlante ingles = new ExpertoParlante("John Smith", Idioma.INGLES);
        ExpertoParlante espanol = new ExpertoParlante("María Pérez", Idioma.ESPANOL);
        dron.aprender(ingles, Idioma.INGLES, AspectoLinguistico.LEXICO, "target, drone, tank");
        dron.aprender(ingles, Idioma.INGLES, AspectoLinguistico.GRAMATICA, "present simple");
        dron.aprender(ingles, Idioma.INGLES, AspectoLinguistico.FONETICA, "/ˈtɑːrɡɪt/");
        dron.aprender(espanol, Idioma.ESPANOL, AspectoLinguistico.LEXICO, "objetivo, dron, tanque");
        out.println("    Idiomas: Inglés " + dron.getIdiomas().dominio(Idioma.INGLES) + "%, Español "
                + dron.getIdiomas().dominio(Idioma.ESPANOL) + "% (entrenados solo por expertos parlantes)");
        out.println();
        out.println("Misión completada. Code is life, life is code.");
        return 0;
    }

    // ------------------------------------------------------------------ Web

    private int web() throws IOException, SQLException {
        Equipo equipo = equipo();
        BaseDatos bd = new BaseDatos(Paths.get(entorno.getOrDefault("ANTDRON_DB", "db/antdron.db")));
        bd.inicializar();
        HormigueroServicio servicio = new HormigueroServicio(bd, TipoHormiga.paraGrupo(equipo.getNumero()));
        ServidorWeb servidor = new ServidorWeb(servicio, "Grupo " + equipo.numeroConFormato() + " · "
                + equipo.getNombre());
        int puerto = Integer.parseInt(entorno.getOrDefault("PORT", "8080"));
        servidor.iniciar(puerto);
        out.println("Hormiguero virtual en http://localhost:" + servidor.puerto() + " (Ctrl+C para salir)");
        CountDownLatch fin = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            servidor.detener();
            fin.countDown();
        }));
        try {
            fin.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return 0;
    }

    // ---------------------------------------------------------- Utilidades

    private int generarCsv() throws IOException {
        Path archivo = new GeneradorCsvGrupo().escribir(equipo(), data());
        out.println("Generado: " + archivo);
        return 0;
    }

    private int automata() throws IOException {
        Bomba bomba = Bomba.paraEquipo(equipo());
        out.println("Coordenadas objetivo: " + bomba.coordenadasObjetivo());
        out.println(bomba.lenguaje());
        out.println();
        out.println(bomba.getAutomata().descripcionFormal());
        out.println(bomba.getAutomata().gramatica());
        out.println("--- Graphviz (DOT) ---");
        out.println(ExportadorGrafo.dot(bomba.getAutomata(), bomba.lenguaje()));
        out.println("--- Mermaid ---");
        out.println(ExportadorGrafo.mermaid(bomba.getAutomata()));
        return 0;
    }

    private int hash(String[] args) {
        if (args.length < 2) {
            err.println("Uso: java -jar antdron.jar hash CLAVE");
            return 1;
        }
        out.println(Credenciales.sha256(args[1]));
        return 0;
    }
}
