package ec.edu.epn.antdron;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import ec.edu.epn.antdron.auth.EntradaUsuario;
import ec.edu.epn.antdron.hormiga.HSoldado;
import ec.edu.epn.antdron.hormiga.HZangano;
import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Historia;
import ec.edu.epn.xp.pruebas.Prueba;
import ec.edu.epn.xp.pruebas.Temporal;

/** El programa hace todo a partir de config/: basta con escribir las cédulas. */
@Historia("HU-15")
class MotorAntdronTest {

    Path raiz = Temporal.directorio();

    private void configurar(int grupo, String... cedulas) throws IOException {
        Files.createDirectories(raiz.resolve("config"));
        Files.writeString(raiz.resolve("config/equipo.properties"), "grupo=" + grupo + "\nnombre=Transformers\n");
        StringBuilder csv = new StringBuilder("cedula;nombre;usuario;clave;rol\n");
        for (int i = 0; i < 6; i++) {
            csv.append(i < cedulas.length ? cedulas[i] : "").append(";Integrante ").append(i + 1)
               .append(";integrante").append(i + 1).append(";1234;CLIENTE\n");
        }
        Files.writeString(raiz.resolve("config/integrantes.csv"), csv);
    }

    @Prueba
    void sinCedulasNoFallaYAvisa() throws IOException {
        configurar(1);
        MotorAntdron m = MotorAntdron.cargar(raiz);
        Afirmar.verdadero(m.getBomba().isEmpty());
        Afirmar.verdadero(m.getCoordenadas().isEmpty());
        Afirmar.verdadero(m.getArchivoCoordenadas().isEmpty());
        Afirmar.contiene(m.getAvisos().get(0), "No hay cédulas en config/integrantes.csv");
        Afirmar.falso(Files.exists(raiz.resolve("datos/Grupo01.csv")));
        Afirmar.esInstancia(HSoldado.class, m.prepararPiloto().hormiga());
        Afirmar.falso(m.crearAntCiberDron(m.prepararPiloto().hormiga()).buscar("abcdt"));
    }

    @Prueba
    @Historia({"HU-03", "HU-15"})
    void conCedulasGeneraCsvBombaYAutomata() throws IOException {
        configurar(3, "1754265385", "1503639");
        MotorAntdron m = MotorAntdron.cargar(raiz);
        Path csv = raiz.resolve("datos/Grupo03.csv");
        Afirmar.igual(csv, m.getArchivoCoordenadas().orElseThrow());
        Afirmar.igual(10 + 7 + 1, Files.readAllLines(csv).size(), "cabecera + un dígito por línea");
        Afirmar.igual(17, m.getCoordenadas().size());
        Afirmar.igual("L = { abcdt+ , ab* }", m.getBomba().orElseThrow().lenguaje());
        Afirmar.contiene(m.getAvisos().get(0), "Faltan 4 cédulas");
        String doc = Files.readString(raiz.resolve("datos/automata-grupo03.md"));
        Afirmar.contiene(doc, "| Integrante 1 | 1754265385 | 5 | Coord-05 |");
        Afirmar.contiene(doc, "S → aA | a");
        Afirmar.contiene(doc, "stateDiagram-v2");
        Afirmar.contiene(doc, "digraph AFD");
        Afirmar.esInstancia(HZangano.class, m.prepararPiloto().hormiga());
    }

    @Prueba
    void alCambiarLasCedulasSeRegeneraTodo() throws IOException {
        configurar(1, "1754265385");
        Afirmar.igual("L = { abcdt+ }", MotorAntdron.cargar(raiz).getBomba().orElseThrow().lenguaje());
        String antes = Files.readString(raiz.resolve("datos/Grupo01.csv"));
        configurar(1, "1754265385", "1111112");
        MotorAntdron m = MotorAntdron.cargar(raiz);
        Afirmar.igual("L = { abcdt+ , ab* }", m.getBomba().orElseThrow().lenguaje());
        Afirmar.igual(List.of("Coord-05", "Coord-02"), m.getBomba().orElseThrow().coordenadasObjetivo());
        Afirmar.verdadero(Files.readString(raiz.resolve("datos/Grupo01.csv")).startsWith(antes),
                "las mismas cédulas generan las mismas líneas");
        Afirmar.verdadero(m.getAvisos().stream().noneMatch(a -> a.contains("No hay cédulas")));
    }

    @Prueba
    void grupoSinTipoHormigaNoRompeElPrograma() throws IOException {
        configurar(7, "1754265385");
        MotorAntdron m = MotorAntdron.cargar(raiz);
        Afirmar.verdadero(m.getTipoHormiga().isEmpty());
        Afirmar.verdadero(m.getBomba().isPresent());
        Afirmar.contiene(String.join(" ", m.getAvisos()), "El grupo 7 no tiene TIPO_HORMIGA");
        Afirmar.lanza(IllegalStateException.class, m::prepararPiloto);
        Afirmar.lanza(IllegalStateException.class, m::hormiguero);
    }

    // ------------------------------------------------------------ Consola

    private String consola(String... teclado) throws IOException {
        Deque<String> cola = new ArrayDeque<>(List.of(teclado));
        EntradaUsuario entrada = new EntradaUsuario() {
            @Override
            public String leerLinea(String m) {
                return cola.poll();
            }

            @Override
            public String leerClave(String m) {
                return cola.poll();
            }
        };
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        System.setProperty("antdron.loading.ms", "0");
        int codigo = new App(out, out, entrada).consola(MotorAntdron.cargar(raiz));
        return codigo + "\n" + bytes.toString(StandardCharsets.UTF_8);
    }

    @Prueba
    @Historia({"HU-01", "HU-02", "HU-04", "HU-06", "HU-07"})
    void flujoDeConsolaDelEnunciado() throws IOException {
        configurar(1, "1103635", "1503639");
        String s = consola("integrante1", "mal", "patmic", "123");
        Afirmar.verdadero(s.startsWith("0\n"));
        Afirmar.contiene(s, "Intentos restantes: 2");
        Afirmar.contiene(s, "Bienvenido, Profesor (patmic).");
        Afirmar.contiene(s, "[+] TEAM [1]: Transformers");
        Afirmar.contiene(s, "• Alumno 2: 1503639 | Integrante 2");
        Afirmar.contiene(s, "[+] COORDENADAS UCRANIANAS:");
        Afirmar.contiene(s, "100%    Coord-01    | 01-02 |");
        Afirmar.contiene(s, "Total: 14 coordenadas");
        Afirmar.contiene(s, "L = { abcdt+ , ab* }");
        Afirmar.contiene(s, "Se transformó en: HSoldado");
        Afirmar.contiene(s, "[+] COORDENADAS UCRANIANAS A DESTRUIR:");
        Afirmar.contiene(s, "Misión completada.");
    }

    @Prueba
    @Historia("HU-01")
    void tresIntentosFallidosTerminanLaConsola() throws IOException {
        configurar(1);
        String s = consola("a", "1", "b", "2", "c", "3");
        Afirmar.verdadero(s.startsWith("1\n"));
        Afirmar.contiene(s, "Acceso denegado");
    }

    @Prueba
    void consolaSinCedulasMuestraElEquipoYUnAviso() throws IOException {
        configurar(1);
        String s = consola("integrante3", "1234");
        Afirmar.verdadero(s.startsWith("0\n"));
        Afirmar.contiene(s, "• Alumno 3: (cédula pendiente) | Integrante 3");
        Afirmar.contiene(s, "Complete config/integrantes.csv");
        Afirmar.noContiene(s, "Misión completada");
    }
}
