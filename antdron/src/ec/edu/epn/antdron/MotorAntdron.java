package ec.edu.epn.antdron;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ec.edu.epn.antdron.alimento.ANectar;
import ec.edu.epn.antdron.antciberdron.AntCiberDron;
import ec.edu.epn.antdron.antciberdron.FuentePoder;
import ec.edu.epn.antdron.antciberdron.Laser;
import ec.edu.epn.antdron.antciberdron.Metralleta;
import ec.edu.epn.antdron.antciberdron.TurboReactor;
import ec.edu.epn.antdron.automata.Automata;
import ec.edu.epn.antdron.automata.ExportadorGrafo;
import ec.edu.epn.antdron.bomba.Bomba;
import ec.edu.epn.antdron.config.Equipo;
import ec.edu.epn.antdron.config.LectorConfiguracion;
import ec.edu.epn.antdron.hormiga.HLarva;
import ec.edu.epn.antdron.hormiga.Hormiga;
import ec.edu.epn.antdron.hormiga.TipoHormiga;
import ec.edu.epn.antdron.persistencia.HormigueroServicio;
import ec.edu.epn.antdron.sistema.CoordenadaUK;
import ec.edu.epn.antdron.sistema.GeneradorCsvGrupo;
import ec.edu.epn.antdron.sistema.SistemaRuso;

/**
 * Hace todo lo que depende de la configuración del equipo, a partir de {@code config/}:
 * <ol>
 *   <li>lee el equipo y las cédulas;</li>
 *   <li>genera {@code datos/Grupo##.csv} con las coordenadas de las cédulas (siempre igual para las mismas cédulas);</li>
 *   <li>lee el archivo con el {@link SistemaRuso};</li>
 *   <li>construye la bomba con el último dígito de cada cédula y su autómata;</li>
 *   <li>exporta la definición formal, la gramática y el grafo a {@code datos/automata-grupo##.md}.</li>
 * </ol>
 * Si faltan cédulas no falla: deja avisos y desactiva lo que las necesita.
 */
public final class MotorAntdron {

    private final Path raiz;
    private final Equipo equipo;
    private final List<String> avisos = new ArrayList<>();
    private final List<CoordenadaUK> coordenadas;
    private final Bomba bomba;
    private final TipoHormiga tipoHormiga;
    private final Path archivoCoordenadas;
    private final Path archivoAutomata;

    private MotorAntdron(Path raiz, Equipo equipo, List<CoordenadaUK> coordenadas, Bomba bomba,
                         TipoHormiga tipoHormiga, Path archivoCoordenadas, Path archivoAutomata, List<String> avisos) {
        this.raiz = raiz;
        this.equipo = equipo;
        this.coordenadas = List.copyOf(coordenadas);
        this.bomba = bomba;
        this.tipoHormiga = tipoHormiga;
        this.archivoCoordenadas = archivoCoordenadas;
        this.archivoAutomata = archivoAutomata;
        this.avisos.addAll(avisos);
    }

    /** Raíz del proyecto: ANTDRON_RAIZ o el directorio actual. */
    public static Path raizPorDefecto() {
        String r = System.getenv("ANTDRON_RAIZ");
        return Path.of(r == null || r.isBlank() ? "" : r).toAbsolutePath();
    }

    /**
     * @throws IOException o IllegalArgumentException con un mensaje claro si la configuración no se puede leer
     */
    public static MotorAntdron cargar(Path raiz) throws IOException {
        Equipo equipo = new LectorConfiguracion().leer(raiz.resolve("config"));
        List<String> avisos = new ArrayList<>();
        Path datos = raiz.resolve("datos");

        TipoHormiga tipo = null;
        try {
            tipo = TipoHormiga.paraGrupo(equipo.grupo());
        } catch (IllegalArgumentException e) {
            avisos.add(e.getMessage() + ". Las hormigas y el AntCiberDron quedan desactivados.");
        }

        List<CoordenadaUK> coordenadas = List.of();
        Bomba bomba = null;
        Path csv = null;
        Path automata = null;
        if (equipo.cedulas().isEmpty()) {
            avisos.add("No hay cédulas en config/integrantes.csv. Escríbalas en la primera columna y pulse "
                    + "«Recargar configuración» (o reinicie) para generar " + equipo.archivoCoordenadas()
                    + ", la bomba y el autómata.");
        } else {
            if (equipo.cedulasPendientes() > 0) {
                avisos.add("Faltan " + equipo.cedulasPendientes() + " cédulas en config/integrantes.csv: "
                        + (equipo.cedulas().size() == 1 ? "por ahora se usa solo la cédula registrada."
                        : "por ahora se usan solo las " + equipo.cedulas().size() + " registradas."));
            }
            csv = new GeneradorCsvGrupo().escribir(equipo, datos);
            coordenadas = new SistemaRuso().readCoord(csv.toString());
            bomba = new Bomba(equipo.cedulas());
            automata = datos.resolve("automata-grupo" + equipo.grupoConFormato() + ".md");
            Files.writeString(automata, documentoAutomata(equipo, bomba), StandardCharsets.UTF_8);
        }
        return new MotorAntdron(raiz, equipo, coordenadas, bomba, tipo, csv, automata, avisos);
    }

    /** Documento Markdown con el autómata del equipo (se regenera al cambiar las cédulas). */
    static String documentoAutomata(Equipo equipo, Bomba bomba) {
        Automata a = bomba.getAutomata();
        StringBuilder sb = new StringBuilder();
        sb.append("# Autómata de la bomba · Grupo ").append(equipo.grupoConFormato()).append("\n\n");
        sb.append("> Archivo generado por la aplicación a partir de config/integrantes.csv. No lo edite a mano.\n\n");
        sb.append("| Integrante | Cédula | Último dígito | Coordenada |\n|---|---|---|---|\n");
        equipo.integrantes().forEach(i -> sb.append("| ").append(i.nombre()).append(" | ")
                .append(i.cedulaVisible()).append(" | ")
                .append(i.tieneCedula() ? String.valueOf(i.ultimoDigito()) : "—").append(" | ")
                .append(i.tieneCedula() ? String.format("Coord-%02d", i.ultimoDigito()) : "—").append(" |\n"));
        sb.append("\nCoordenadas objetivo: ").append(String.join(", ", bomba.coordenadasObjetivo())).append("\n\n");
        sb.append("**").append(bomba.lenguaje()).append("**\n\n");
        sb.append("## Definición formal\n\n```\n").append(a.descripcionFormal()).append("```\n\n");
        sb.append("## Gramática\n\n```\n").append(a.gramatica()).append("```\n\n");
        sb.append("## Grafo\n\n```mermaid\n").append(ExportadorGrafo.mermaid(a)).append("```\n\n");
        sb.append("Graphviz (pegar en https://dreampuf.github.io/GraphvizOnline):\n\n```dot\n")
          .append(ExportadorGrafo.dot(a, bomba.lenguaje())).append("```\n");
        return sb.toString();
    }

    /** Resultado de preparar al piloto: la hormiga adulta y los pasos (para mostrarlos). */
    public record Piloto(Hormiga hormiga, List<String> pasos) {
    }

    /** Toda hormiga nace como HLarva: come néctar (vive) y luego su ALIMENTO (se transforma). */
    public Piloto prepararPiloto() {
        TipoHormiga tipo = getTipoHormiga().orElseThrow(() -> new IllegalStateException(
                "El grupo " + equipo.grupo() + " no tiene TIPO_HORMIGA en el enunciado"));
        HLarva larva = new HLarva(tipo);
        List<String> pasos = new ArrayList<>();
        pasos.add("Nace: " + larva);
        pasos.add("Come Néctar → vive: " + larva.comer(new ANectar()));
        pasos.add("Come " + tipo.alimento() + " → vive: " + larva.comer(tipo.alimento()));
        Hormiga adulta = larva.getEvolucion().orElseThrow();
        pasos.add("Se transformó en: " + adulta);
        return new Piloto(adulta, pasos);
    }

    /** AntCiberDron con la metralleta (pata delantera derecha) y el láser (izquierda) ya cargados. */
    public AntCiberDron crearAntCiberDron(Hormiga piloto) {
        AntCiberDron dron = new AntCiberDron(piloto, bomba, coordenadas, new FuentePoder(100), new TurboReactor(2));
        dron.equiparMetralleta(new Metralleta(100));
        dron.equiparLaser(new Laser(500));
        return dron;
    }

    public HormigueroServicio hormiguero() {
        TipoHormiga tipo = getTipoHormiga().orElseThrow(() -> new IllegalStateException(
                "El grupo " + equipo.grupo() + " no tiene TIPO_HORMIGA en el enunciado"));
        return new HormigueroServicio(raiz.resolve("datos").resolve("hormiguero"), tipo);
    }

    public Path getRaiz() { return raiz; }
    public Equipo getEquipo() { return equipo; }
    public List<String> getAvisos() { return Collections.unmodifiableList(avisos); }
    public List<CoordenadaUK> getCoordenadas() { return coordenadas; }
    public Optional<Bomba> getBomba() { return Optional.ofNullable(bomba); }
    public Optional<TipoHormiga> getTipoHormiga() { return Optional.ofNullable(tipoHormiga); }
    public Optional<Path> getArchivoCoordenadas() { return Optional.ofNullable(archivoCoordenadas); }
    public Optional<Path> getArchivoAutomata() { return Optional.ofNullable(archivoAutomata); }
}
