package ec.edu.epn.antdron.antciberdron;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import ec.edu.epn.antdron.bomba.Bomba;
import ec.edu.epn.antdron.hormiga.Extremidad;
import ec.edu.epn.antdron.hormiga.HLarva;
import ec.edu.epn.antdron.hormiga.Hormiga;
import ec.edu.epn.antdron.pkAntCiberDronContract.IIA;
import ec.edu.epn.antdron.sistema.CoordenadaUK;
import ec.edu.epn.xp.diseno.TarjetaCRC;

/**
 * Exoesqueleto que usa una hormiga adulta de su TIPO_HORMIGA. Se compone de las mismas extremidades
 * de la hormiga, integra la bomba y su inteligencia artificial (IIA) busca el tipo de arsenal.
 */
@TarjetaCRC(responsabilidades = {"Buscar el tipo de arsenal (IIA)", "Detonar la bomba solo en objetivos",
        "Cargar metralleta y láser", "Volar con el turbo reactor", "Recargar/reemplazar la fuente de poder",
        "Aprender inglés y español"},
        colaboradores = {"Hormiga", "Bomba", "CoordenadaUK", "FuentePoder", "Arma", "TurboReactor",
                "ModuloIdiomas"})
public class AntCiberDron implements IIA {

    private static final String FORMATO_FILA = "%-12s| %-13s| %s%n";

    private final Hormiga piloto;
    private final List<Extremidad> extremidades;
    private final Map<Extremidad, Arma> cargas = new EnumMap<>(Extremidad.class);
    private final TurboReactor turboReactor;
    private final Bomba bomba;
    private final List<CoordenadaUK> coordenadas;
    private final ModuloIdiomas idiomas = new ModuloIdiomas();
    private final PrintStream salida;
    private FuentePoder fuente;

    public AntCiberDron(Hormiga piloto, Bomba bomba, List<CoordenadaUK> coordenadas, FuentePoder fuente,
                        TurboReactor turboReactor, PrintStream salida) {
        Objects.requireNonNull(piloto, "piloto");
        if (piloto instanceof HLarva) {
            throw new IllegalArgumentException("Una HLarva no puede usar el exoesqueleto: debe transformarse "
                    + "primero en su TIPO_HORMIGA");
        }
        if (!piloto.estaViva()) {
            throw new IllegalArgumentException("La hormiga piloto debe estar viva");
        }
        this.piloto = piloto;
        this.extremidades = List.copyOf(piloto.getExtremidades());
        this.bomba = Objects.requireNonNull(bomba, "bomba");
        this.coordenadas = List.copyOf(coordenadas);
        this.fuente = Objects.requireNonNull(fuente, "fuente");
        this.turboReactor = Objects.requireNonNull(turboReactor, "turboReactor");
        this.salida = Objects.requireNonNull(salida, "salida");
    }

    // ------------------------------------------------------------------ IIA

    @Override
    public boolean buscar(String tipoArsenal) {
        boolean explota = false;
        for (CoordenadaUK c : coordenadas) {
            if (c.getTipoArsenal().equals(tipoArsenal) && bomba.debeExplotar(c)) {
                salida.printf(FORMATO_FILA, c.getGeoposicion(), c.getTipoArsenal(), true);
                explota = true;
            }
        }
        return explota;
    }

    /** Busca cada tipo de arsenal del archivo y presenta la tabla de coordenadas a destruir. */
    public List<CoordenadaUK> buscarObjetivos() {
        salida.println();
        salida.println("[+] COORDENADAS UCRANIANAS A DESTRUIR:");
        salida.printf(FORMATO_FILA, "Geoposición", "Tipo Arsenal", "Acción");
        Set<String> tipos = new LinkedHashSet<>();
        coordenadas.forEach(c -> tipos.add(c.getTipoArsenal()));
        tipos.forEach(this::buscar);
        List<CoordenadaUK> destruidas = coordenadas.stream().filter(bomba::debeExplotar).toList();
        salida.println("Objetivos destruidos: " + destruidas.size());
        return destruidas;
    }

    // ------------------------------------------------------------- Armas

    /** Metralleta solo en la pata delantera derecha y láser solo en la izquierda. */
    public void equipar(Extremidad extremidad, Arma arma) {
        Objects.requireNonNull(arma, "arma");
        if (!extremidades.contains(extremidad)) {
            throw new IllegalArgumentException("El exoesqueleto no tiene la extremidad " + extremidad);
        }
        if (arma instanceof Metralleta && extremidad != Extremidad.PATA_DELANTERA_DERECHA) {
            throw new IllegalArgumentException("La metralleta solo se carga en la pata delantera derecha");
        }
        if (arma instanceof Laser && extremidad != Extremidad.PATA_DELANTERA_IZQUIERDA) {
            throw new IllegalArgumentException("El láser solo se carga en la pata delantera izquierda");
        }
        if (!(arma instanceof Metralleta) && !(arma instanceof Laser)) {
            throw new IllegalArgumentException("Arma no soportada: " + arma.getNombre());
        }
        cargas.put(extremidad, arma);
    }

    public void equiparMetralleta(Metralleta metralleta) {
        equipar(Extremidad.PATA_DELANTERA_DERECHA, metralleta);
    }

    public void equiparLaser(Laser laser) {
        equipar(Extremidad.PATA_DELANTERA_IZQUIERDA, laser);
    }

    public String dispararMetralleta() {
        return arma(Extremidad.PATA_DELANTERA_DERECHA, "metralleta").disparar(fuente);
    }

    public String dispararLaser() {
        return arma(Extremidad.PATA_DELANTERA_IZQUIERDA, "láser").disparar(fuente);
    }

    /** Potencia actual del láser según la energía de la fuente de poder. */
    public double potenciaLaser() {
        return ((Laser) arma(Extremidad.PATA_DELANTERA_IZQUIERDA, "láser")).potencia(fuente);
    }

    private Arma arma(Extremidad extremidad, String nombre) {
        Arma arma = cargas.get(extremidad);
        if (arma == null) {
            throw new IllegalStateException("No hay " + nombre + " cargada en " + extremidad);
        }
        return arma;
    }

    public Optional<Arma> armaEn(Extremidad extremidad) {
        return Optional.ofNullable(cargas.get(extremidad));
    }

    // ------------------------------------------------- Vuelo y energía

    public String volar(int minutos) {
        return turboReactor.volar(minutos, fuente);
    }

    /** La IIA asiste la recarga de la fuente de poder. */
    public String recargarFuente() {
        fuente.recargar();
        return "[IIA] Fuente de poder recargada: " + fuente;
    }

    /** La IIA asiste el reemplazo: solo acepta una fuente con energía. */
    public String reemplazarFuente(FuentePoder nueva) {
        Objects.requireNonNull(nueva, "nueva");
        if (nueva == fuente) {
            throw new IllegalArgumentException("La fuente nueva es la misma que la instalada");
        }
        if (nueva.getEnergia() <= 0) {
            throw new IllegalArgumentException("[IIA] Reemplazo rechazado: la fuente nueva está descargada");
        }
        fuente = nueva;
        return "[IIA] Fuente de poder reemplazada: " + fuente;
    }

    // --------------------------------------------------------- Idiomas

    public void aprender(ExpertoParlante experto, Idioma idioma, AspectoLinguistico aspecto, String contenido) {
        idiomas.entrenar(experto, idioma, aspecto, contenido);
    }

    public ModuloIdiomas getIdiomas() {
        return idiomas;
    }

    // ------------------------------------------------------------ Datos

    public Hormiga getPiloto() { return piloto; }
    public List<Extremidad> getExtremidades() { return extremidades; }
    public FuentePoder getFuente() { return fuente; }
    public Bomba getBomba() { return bomba; }
    public String getUbicacionTurboReactor() { return TurboReactor.UBICACION; }

    public List<CoordenadaUK> getCoordenadas() {
        return Collections.unmodifiableList(new ArrayList<>(coordenadas));
    }
}
