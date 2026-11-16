package ec.edu.epn.xp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import ec.edu.epn.xp.diseno.RevisorCRC;
import ec.edu.epn.xp.pruebas.AntesDeCada;
import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Historia;
import ec.edu.epn.xp.pruebas.Prueba;
import ec.edu.epn.xp.pruebas.Temporal;

/** Comandos de los módulos, cargados con ServiceLoader, sobre un proyecto de ejemplo. */
@Historia({"XP-02", "XP-03", "XP-04", "XP-05", "XP-06"})
class ComandosTest {

    Path raiz = Temporal.directorio();
    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errores = new ByteArrayOutputStream();

    @AntesDeCada
    void preparar() throws IOException {
        Files.createDirectories(raiz.resolve("config"));
        Files.createDirectories(raiz.resolve("xp"));
        Files.createDirectories(raiz.resolve("src/a"));
        Files.writeString(raiz.resolve("config/equipo.properties"), "grupo=1\nnombre=Transformers\nmetafora=Colonia\n");
        StringBuilder csv = new StringBuilder("cedula;nombre;usuario;clave;rol\n");
        String[] roles = {"ENTRENADOR", "CLIENTE", "ENCARGADO_PRUEBAS", "ENCARGADO_SEGUIMIENTO", "CONSULTOR", "GESTOR"};
        for (int i = 0; i < 6; i++) {
            csv.append(i == 0 ? "1754265385" : "").append(";Alumno ").append(i + 1).append(";u").append(i)
               .append(";1234;").append(roles[i]).append('\n');
        }
        Files.writeString(raiz.resolve("config/integrantes.csv"), csv);
        Files.writeString(raiz.resolve("xp/historias.csv"), String.join("\n",
                "HU-01;Login;profesor;autenticarme;entrar;ALTA;3;1;TERMINADA;Alumno 1;3 intentos",
                "HU-02;CSV;KGD;leer coordenadas;atacar;ALTA;5;1;EN_PROGRESO;Alumno 2;loading",
                "HU-03;Idiomas;experto;entrenar;hablar;BAJA;2;0;PENDIENTE;Alumno 3;solo expertos"));
        Files.writeString(raiz.resolve("src/a/Bomba.java"), "class Bomba {}");
        Files.writeString(raiz.resolve("src/a/Hormiga.java"), "class Hormiga {}");
        Files.writeString(raiz.resolve("xp/tarjetas-crc.md"), "### Clase: Bomba\n| R | C |\n### Clase: Vieja\n");
        Files.createDirectories(raiz.resolve("build/pruebas"));
        Files.writeString(raiz.resolve("build/pruebas/resultados.csv"), "HU-01;A#a;PASO\nHU-02;B#b;FALLO\n");
    }

    private int xp(String... args) {
        String[] todos = new String[args.length + 2];
        todos[0] = "--raiz";
        todos[1] = raiz.toString();
        System.arraycopy(args, 0, todos, 2, args.length);
        return new XpCli().ejecutar(todos, new PrintStream(salida, true, StandardCharsets.UTF_8),
                new PrintStream(errores, true, StandardCharsets.UTF_8));
    }

    private String out() {
        return salida.toString(StandardCharsets.UTF_8);
    }

    @Prueba
    void losModulosSeRegistranConServiceLoader() {
        Afirmar.igual(java.util.List.of("aceptacion", "backlog", "crc", "directrices", "equipo", "parejas", "plan",
                "velocidad", "verificar"), new XpCli().nombresDeComandos());
    }

    @Prueba
    void backlogPlanYVelocidad() {
        Afirmar.igual(0, xp("backlog"));
        Afirmar.contiene(out(), "Total: 3 historias, 10 puntos");
        Afirmar.igual(0, xp("plan"));
        Afirmar.contiene(out(), "Iteración 1 (3/8 pts, 38%)");
        Afirmar.contiene(out(), "Sin iteración asignada:");
        Afirmar.igual(0, xp("plan", "--proponer", "5"));
        Afirmar.contiene(out(), "Propuesta con velocidad 5");
        Afirmar.igual(0, xp("velocidad"));
        Afirmar.contiene(out(), "Velocidad (clima de ayer): 3 pts");
        Afirmar.igual(1, xp("plan", "--proponer", "x"));
    }

    @Prueba
    void equipoYParejas() {
        Afirmar.igual(0, xp("equipo"));
        Afirmar.contiene(out(), "[+] GRUPO 01: Transformers");
        Afirmar.contiene(out(), "1754265385");
        Afirmar.contiene(out(), "Faltan 5 cédulas");
        Afirmar.igual(0, xp("parejas", "2"));
        Afirmar.contiene(out(), "Ronda 2:");
        Afirmar.igual(1, xp("parejas", "dos"));
    }

    @Prueba
    void crcIndicaClasesSinTarjeta() throws IOException {
        Afirmar.igual(0, xp("crc", "src"));
        Afirmar.contiene(out(), "Con tarjeta: 1/2 (50%)");
        Afirmar.contiene(out(), "Sin tarjeta: Hormiga");
        Afirmar.contiene(out(), "ya no existen: Vieja");
        RevisorCRC.Revision r = RevisorCRC.revisar(raiz.resolve("src"), raiz.resolve("xp/tarjetas-crc.md"));
        Afirmar.igual(50, r.cobertura());
        Afirmar.igual(1, xp("crc", "no-existe"));
    }

    @Prueba
    void aceptacionEscribeElReporte() throws IOException {
        Afirmar.igual(0, xp("aceptacion", "--escribir"));
        Afirmar.contiene(out(), "| HU-01 | Login | 1 | 0 | 0 | ACEPTADA |");
        Afirmar.contiene(out(), "| HU-02 | CSV | 0 | 1 | 0 | RECHAZADA |");
        Afirmar.contiene(out(), "| HU-03 | Idiomas | 0 | 0 | 0 | SIN_PRUEBAS |");
        Afirmar.contiene(Files.readString(raiz.resolve("xp/pruebas-aceptacion.md")), "Historias aceptadas: 1/3");
    }
}
