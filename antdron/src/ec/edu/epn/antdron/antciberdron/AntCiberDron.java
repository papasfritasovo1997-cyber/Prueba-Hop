package ec.edu.epn.antdron.antciberdron;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import ec.edu.epn.antdron.bomba.Bomba;
import ec.edu.epn.antdron.hormiga.Extremidad;
import ec.edu.epn.antdron.hormiga.HLarva;
import ec.edu.epn.antdron.hormiga.Hormiga;
import ec.edu.epn.antdron.pkAntCiberDronContract.IIA;
import ec.edu.epn.antdron.sistema.CoordenadaUK;

/**
 * Exoesqueleto que usa una hormiga adulta de su TIPO_HORMIGA. Se compone de las mismas extremidades
 * de la hormiga, integra la bomba y su inteligencia artificial (IIA) busca el tipo de arsenal.
 * La bomba puede faltar mientras el equipo no registre cédulas: en ese caso nunca explota.
 */
public class AntCiberDron implements IIA {

    private final Hormiga piloto;
    private final List<Extremidad> extremidades;
    private final Map<Extremidad, Arma> cargas = new EnumMap<>(Extremidad.class);
    private final TurboReactor turboReactor;
    private final Bomba bomba;
    private final List<CoordenadaUK> coordenadas;
    private final ModuloIdiomas idiomas = new ModuloIdiomas();
    private Consumer<CoordenadaUK> presentador = c -> { };
    private FuentePoder fuente;

    public AntCiberDron(Hormiga piloto, Bomba bomba, List<CoordenadaUK> coordenadas, FuentePoder fuente,
                        TurboReactor turboReactor) {
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
        this.bomba = bomba;
        this.coordenadas = List.copyOf(coordenadas);
        this.fuente = Objects.requireNonNull(fuente, "fuente");
        this.turboReactor = Objects.requireNonNull(turboReactor, "turboReactor");
    }

    /** Dónde presentar cada coordenada en la que explota la bomba (consola o pantalla). */
    public void setPresentador(Consumer<CoordenadaUK> presentador) {
        this.presentador = Objects.requireNonNull(presentador, "presentador");
    }

    // ------------------------------------------------------------------ IIA

    /** Busca el tipo de arsenal y presenta cada coordenada donde la bomba explota. */
    @Override
    public boolean buscar(String tipoArsenal) {
        if (bomba == null || tipoArsenal == null) {
            return false;
        }
        String w = tipoArsenal.trim();
        boolean explota = false;
        for (CoordenadaUK c : coordenadas) {
            if (c.getTipoArsenal().equals(w) && bomba.debeExplotar(c.getGeoposicion(), c.getTipoArsenal())) {
                presentador.accept(c);
                explota = true;
            }
        }
        return explota;
    }

    /** Busca cada tipo de arsenal del archivo (en orden de aparición) y devuelve las coordenadas destruidas. */
    public List<CoordenadaUK> buscarObjetivos() {
        List<CoordenadaUK> destruidas = new ArrayList<>();
        Consumer<CoordenadaUK> original = presentador;
        presentador = c -> {
            destruidas.add(c);
            original.accept(c);
        };
        try {
            Set<String> tipos = new LinkedHashSet<>();
            coordenadas.forEach(c -> tipos.add(c.getTipoArsenal()));
            tipos.forEach(this::buscar);
        } finally {
            presentador = original;
        }
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
    public Optional<Bomba> getBomba() { return Optional.ofNullable(bomba); }
    public String getUbicacionTurboReactor() { return TurboReactor.UBICACION; }

    public List<CoordenadaUK> getCoordenadas() {
        return Collections.unmodifiableList(new ArrayList<>(coordenadas));
    }
}
