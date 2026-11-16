package ec.edu.epn.antdron.antciberdron;

import java.util.ArrayList;
import java.util.List;

import ec.edu.epn.antdron.alimento.ANectar;
import ec.edu.epn.antdron.bomba.Bomba;
import ec.edu.epn.antdron.hormiga.Extremidad;
import ec.edu.epn.antdron.hormiga.HLarva;
import ec.edu.epn.antdron.hormiga.HSoldado;
import ec.edu.epn.antdron.hormiga.Hormiga;
import ec.edu.epn.antdron.hormiga.TipoHormiga;
import ec.edu.epn.antdron.pkAntCiberDronContract.IIA;
import ec.edu.epn.antdron.sistema.CoordenadaUK;
import ec.edu.epn.antdron.sistema.SistemaRuso;
import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.AntesDeCada;
import ec.edu.epn.xp.pruebas.Historia;
import ec.edu.epn.xp.pruebas.Prueba;

class AntCiberDronTest {

    private AntCiberDron dron;
    private FuentePoder fuente;
    private final List<String> presentadas = new ArrayList<>();

    /** Archivo del ejemplo del enunciado. */
    private static final List<CoordenadaUK> COORDENADAS = new SistemaRuso().parsear(List.of(
            "Coord-01;01-02;;;;;aaaa",
            "Coord-01;01-02;;;;;a",
            "Coord-00;;;;;;aa",
            "Coord-03;;;03-06;;;abc",
            "Coord-06;;;;04-08;;abcdttttt",
            "Coord-03;;;03-06;;;abc",
            "Coord-05;;;;;05-10;abcdttt",
            "Coord-09;01-02;;;;;ab"));

    /** Cédulas del ejemplo: 1103635 y 1503639 → Coord-05 y Coord-09. */
    private static final Bomba BOMBA = new Bomba(List.of("1103635", "1503639"));

    @AntesDeCada
    void preparar() {
        fuente = new FuentePoder(100);
        dron = new AntCiberDron(new HSoldado(), BOMBA, COORDENADAS, fuente, new TurboReactor(2));
        dron.setPresentador(c -> presentadas.add(c.getGeoposicion() + " | " + c.getTipoArsenal() + " | true"));
    }

    @Prueba
    @Historia("HU-07")
    void buscarPresentaLaCoordenadaDondeExplota() {
        IIA ia = dron;
        Afirmar.verdadero(ia.buscar("abcdttt"));
        Afirmar.igual(List.of("Coord-05 | abcdttt | true"), presentadas);
        Afirmar.falso(ia.buscar("abc"), "Coord-03 no es objetivo");
        Afirmar.falso(ia.buscar("a"), "'a' está en ab* pero Coord-01 no es objetivo");
        Afirmar.falso(ia.buscar("zzz"));
        Afirmar.falso(ia.buscar(null));
        Afirmar.igual(1, presentadas.size());
    }

    @Prueba
    @Historia("HU-07")
    void buscarObjetivosReproduceLaTablaDelEnunciado() {
        List<CoordenadaUK> destruidas = dron.buscarObjetivos();
        Afirmar.igual(List.of("Coord-05", "Coord-09"), destruidas.stream().map(CoordenadaUK::getGeoposicion).toList());
        Afirmar.igual(List.of("Coord-05 | abcdttt | true", "Coord-09 | ab | true"), presentadas);
    }

    @Prueba
    @Historia("HU-07")
    void sinBombaLaIaNuncaExplota() {
        AntCiberDron sinBomba = new AntCiberDron(new HSoldado(), null, COORDENADAS, new FuentePoder(10),
                new TurboReactor(1));
        Afirmar.falso(sinBomba.buscar("abcdttt"));
        Afirmar.verdadero(sinBomba.buscarObjetivos().isEmpty());
        Afirmar.verdadero(sinBomba.getBomba().isEmpty());
    }

    @Prueba
    @Historia("HU-07")
    void soloUnaHormigaAdultaYVivaUsaElExoesqueleto() {
        Afirmar.lanza(IllegalArgumentException.class, () -> new AntCiberDron(new HLarva(TipoHormiga.SOLDADO),
                BOMBA, COORDENADAS, new FuentePoder(10), new TurboReactor(1)));
        Hormiga muerta = new HSoldado();
        muerta.comer(new ANectar());
        Afirmar.lanza(IllegalArgumentException.class, () -> new AntCiberDron(muerta, BOMBA, COORDENADAS,
                new FuentePoder(10), new TurboReactor(1)));
        Afirmar.igual(new HSoldado().getExtremidades(), dron.getExtremidades());
    }
    @Prueba
    @Historia("HU-08")
    void metralletaDerechaYLaserIzquierdo() {
        Afirmar.lanza(IllegalArgumentException.class,
                () -> dron.equipar(Extremidad.PATA_DELANTERA_IZQUIERDA, new Metralleta(10)));
        Afirmar.lanza(IllegalArgumentException.class,
                () -> dron.equipar(Extremidad.PATA_DELANTERA_DERECHA, new Laser(100)));
        Afirmar.lanza(IllegalArgumentException.class,
                () -> dron.equipar(Extremidad.PATA_TRASERA_DERECHA, new Metralleta(10)));
        Afirmar.lanza(IllegalStateException.class, dron::dispararMetralleta);

        dron.equiparMetralleta(new Metralleta(15));
        dron.equiparLaser(new Laser(200));
        Afirmar.igual("Ráfaga de 10 balas (quedan 5)", dron.dispararMetralleta());
        Afirmar.igual("Ráfaga de 5 balas (quedan 0)", dron.dispararMetralleta());
        Afirmar.lanza(IllegalStateException.class, dron::dispararMetralleta);
        Afirmar.verdadero(dron.armaEn(Extremidad.PATA_DELANTERA_IZQUIERDA).isPresent());
    }

    @Prueba
    @Historia("HU-08")
    void laPotenciaDelLaserVariaConLaEnergia() {
        dron.equiparLaser(new Laser(200));
        Afirmar.igual(200.0, dron.potenciaLaser(), 1e-9);
        Afirmar.igual("Disparo láser de 200,0 W".replace(',', '.'), dron.dispararLaser().replace(',', '.'));
        Afirmar.igual(95.0, fuente.getEnergia(), 1e-9);
        dron.volar(20);
        Afirmar.igual(110.0, dron.potenciaLaser(), 1e-9);
    }

    @Prueba
    @Historia("HU-09")
    void volarConsumeEnergiaDelTurboReactorEnLaEspalda() {
        Afirmar.igual("espalda", dron.getUbicacionTurboReactor());
        Afirmar.igual("Vuelo de 10 min con el turbo reactor de la espalda", dron.volar(10));
        Afirmar.igual(80.0, fuente.getEnergia(), 1e-9);
        Afirmar.lanza(IllegalStateException.class, () -> dron.volar(41));
        Afirmar.igual(80.0, fuente.getEnergia(), 1e-9); // un vuelo fallido no consume
        Afirmar.lanza(IllegalArgumentException.class, () -> dron.volar(0));
    }

    @Prueba
    @Historia("HU-09")
    void recargaYReemplazoAsistidosPorLaIIA() {
        dron.volar(30);
        Afirmar.verdadero(dron.recargarFuente().startsWith("[IIA]"));
        Afirmar.igual(100.0, dron.getFuente().getEnergia(), 1e-9);

        FuentePoder descargada = new FuentePoder(10);
        descargada.consumir(10);
        Afirmar.lanza(IllegalArgumentException.class, () -> dron.reemplazarFuente(descargada));
        Afirmar.lanza(IllegalArgumentException.class, () -> dron.reemplazarFuente(dron.getFuente()));

        FuentePoder nueva = new FuentePoder(300);
        Afirmar.verdadero(dron.reemplazarFuente(nueva).startsWith("[IIA]"));
        Afirmar.igual(nueva, dron.getFuente());
    }

    @Prueba
    @Historia("HU-10")
    void soloExpertosParlantesEntrenanSuIdioma() {
        ExpertoParlante john = new ExpertoParlante("John", Idioma.INGLES);
        ExpertoParlante maria = new ExpertoParlante("María", Idioma.ESPANOL);
        Afirmar.lanza(EntrenamientoNoAutorizadoException.class,
                () -> dron.aprender(john, Idioma.ESPANOL, AspectoLinguistico.LEXICO, "hola"));
        Afirmar.lanza(EntrenamientoNoAutorizadoException.class,
                () -> dron.aprender(null, Idioma.INGLES, AspectoLinguistico.LEXICO, "hi"));
        Afirmar.lanza(IllegalArgumentException.class,
                () -> dron.aprender(john, Idioma.INGLES, AspectoLinguistico.LEXICO, " "));

        dron.aprender(john, Idioma.INGLES, AspectoLinguistico.LEXICO, "tank");
        dron.aprender(john, Idioma.INGLES, AspectoLinguistico.GRAMATICA, "past simple");
        Afirmar.igual(66, dron.getIdiomas().dominio(Idioma.INGLES));
        Afirmar.falso(dron.getIdiomas().puedeHablar(Idioma.INGLES));
        dron.aprender(john, Idioma.INGLES, AspectoLinguistico.FONETICA, "/tæŋk/");
        Afirmar.verdadero(dron.getIdiomas().puedeHablar(Idioma.INGLES));

        dron.aprender(maria, Idioma.ESPANOL, AspectoLinguistico.FONETICA, "tanque");
        Afirmar.igual(List.of("tanque"), dron.getIdiomas().conocimiento(Idioma.ESPANOL, AspectoLinguistico.FONETICA));
        Afirmar.igual(33, dron.getIdiomas().dominio(Idioma.ESPANOL));
    }
}
