package ec.edu.epn.antdron;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

import ec.edu.epn.antdron.antciberdron.AntCiberDron;
import ec.edu.epn.antdron.antciberdron.AspectoLinguistico;
import ec.edu.epn.antdron.antciberdron.ExpertoParlante;
import ec.edu.epn.antdron.antciberdron.FuentePoder;
import ec.edu.epn.antdron.antciberdron.Idioma;
import ec.edu.epn.antdron.auth.Autenticador;
import ec.edu.epn.antdron.auth.EntradaUsuario;
import ec.edu.epn.antdron.auth.PresentadorEquipo;
import ec.edu.epn.antdron.automata.AutomataMain;
import ec.edu.epn.antdron.bomba.Bomba;
import ec.edu.epn.antdron.sistema.CoordenadaUK;
import ec.edu.epn.antdron.sistema.Loading;
import ec.edu.epn.antdron.sistema.PresentadorCoordenadas;
import ec.edu.epn.antdron.ui.AntdronFx;

/**
 * Punto de entrada.
 * <pre>
 *   (sin argumentos)    pantallas JavaFX
 *   consola             flujo del enunciado en consola
 *   generar             solo regenera datos/Grupo##.csv y datos/automata-grupo##.md
 *   automata CÉDULAS... muestra el autómata de unas cédulas
 * </pre>
 */
public final class App {

    private static final String FORMATO_DESTRUIR = "%-12s| %-13s| %s%n";

    private final PrintStream out;
    private final PrintStream err;
    private final EntradaUsuario entrada;

    public App(PrintStream out, PrintStream err, EntradaUsuario entrada) {
        this.out = out;
        this.err = err;
        this.entrada = entrada;
    }

    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("gui")) {
            try {
                AntdronFx.iniciar(args);
            } catch (UnsupportedClassVersionError e) {
                System.err.println("El JavaFX del classpath necesita un Java más nuevo que el instalado ("
                        + System.getProperty("java.version") + ").\nUse solo el JavaFX 21.0.5 de lib/: quite "
                        + "cualquier otro JavaFX de «Referenced Libraries» en VS Code y de la carpeta lib/.");
                System.exit(1);
            }
            return;
        }
        if (args[0].equals("automata")) {
            AutomataMain.main(java.util.Arrays.copyOfRange(args, 1, args.length));
            return;
        }
        int codigo = new App(System.out, System.err, EntradaUsuario.delSistema(System.out)).ejecutar(args[0]);
        if (codigo != 0) {
            System.exit(codigo);
        }
    }

    public int ejecutar(String modo) {
        try {
            return switch (modo) {
                case "consola" -> consola(MotorAntdron.cargar(MotorAntdron.raizPorDefecto()));
                case "generar" -> generar(MotorAntdron.cargar(MotorAntdron.raizPorDefecto()));
                default -> {
                    err.println("Modo desconocido: " + modo + " (use: consola, generar, automata o nada para la GUI)");
                    yield 1;
                }
            };
        } catch (IOException | RuntimeException e) {
            err.println("Error: " + e.getMessage());
            return 2;
        }
    }

    private int generar(MotorAntdron motor) {
        motor.getAvisos().forEach(a -> out.println("Aviso: " + a));
        motor.getArchivoCoordenadas().ifPresent(p -> out.println("Generado: " + motor.getRaiz().relativize(p)));
        motor.getArchivoAutomata().ifPresent(p -> out.println("Generado: " + motor.getRaiz().relativize(p)));
        return 0;
    }

    int consola(MotorAntdron motor) {
        out.println("==========================================================");
        out.println("  ANTDRON · KGD · AntCiberDron            Grupo " + motor.getEquipo().grupoConFormato());
        out.println("==========================================================");
        Optional<String> usuario = new Autenticador(motor.getEquipo()).autenticar(entrada, out);
        if (usuario.isEmpty()) {
            return 1;
        }
        out.println();
        PresentadorEquipo.lineas(motor.getEquipo()).forEach(out::println);
        motor.getAvisos().forEach(a -> out.println("\n(!) " + a));

        if (motor.getBomba().isEmpty()) {
            out.println("\nSin cédulas no hay coordenadas ni bomba. Complete config/integrantes.csv.");
            return 0;
        }
        new PresentadorCoordenadas(out, Loading.desdeSistema(out)).presentar(motor.getCoordenadas());

        Bomba bomba = motor.getBomba().get();
        out.println();
        out.println("[+] BOMBA · coordenadas objetivo: " + String.join(", ", bomba.coordenadasObjetivo()));
        out.println("    " + bomba.lenguaje());
        motor.getArchivoAutomata().ifPresent(p -> out.println("    Autómata y gramática: "
                + motor.getRaiz().relativize(p)));

        if (motor.getTipoHormiga().isEmpty()) {
            return 0;
        }
        MotorAntdron.Piloto piloto = motor.prepararPiloto();
        out.println();
        out.println("[+] HORMIGA DEL GRUPO: " + motor.getTipoHormiga().get().getNombre());
        piloto.pasos().forEach(p -> out.println("    " + p));

        AntCiberDron dron = motor.crearAntCiberDron(piloto.hormiga());
        out.println();
        out.println("[+] COORDENADAS UCRANIANAS A DESTRUIR:");
        out.printf(FORMATO_DESTRUIR, "Geoposición", "Tipo Arsenal", "Acción");
        dron.setPresentador(c -> out.printf(FORMATO_DESTRUIR, c.getGeoposicion(), c.getTipoArsenal(), true));
        List<CoordenadaUK> destruidas = dron.buscarObjetivos();
        out.println("Objetivos destruidos: " + destruidas.size());

        out.println();
        out.println("[+] ANTCIBERDRON");
        out.println("    Extremidades: " + dron.getExtremidades().size() + " (las mismas de la hormiga)");
        out.println("    Pata delantera derecha: " + dron.dispararMetralleta());
        out.println("    Pata delantera izquierda: " + dron.dispararLaser());
        out.println("    " + dron.volar(20));
        out.printf("    Potencia del láser con %s: %.1f W%n", dron.getFuente(), dron.potenciaLaser());
        out.println("    " + dron.recargarFuente());
        out.println("    " + dron.reemplazarFuente(new FuentePoder(150)));
        ExpertoParlante ingles = new ExpertoParlante("John Smith", Idioma.INGLES);
        ExpertoParlante espanol = new ExpertoParlante("María Pérez", Idioma.ESPANOL);
        dron.aprender(ingles, Idioma.INGLES, AspectoLinguistico.LEXICO, "target, drone, tank");
        dron.aprender(espanol, Idioma.ESPANOL, AspectoLinguistico.LEXICO, "objetivo, dron, tanque");
        out.println("    Idiomas: Inglés " + dron.getIdiomas().dominio(Idioma.INGLES) + "%, Español "
                + dron.getIdiomas().dominio(Idioma.ESPANOL) + "% (entrenados solo por expertos parlantes)");
        out.println();
        out.println("Misión completada. Code is life, life is code.");
        return 0;
    }
}
