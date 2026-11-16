package ec.edu.epn.antdron.antciberdron;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ec.edu.epn.antdron.bomba.Bomba;
import ec.edu.epn.antdron.hormiga.Extremidad;
import ec.edu.epn.antdron.hormiga.HLarva;
import ec.edu.epn.antdron.hormiga.HSoldado;
import ec.edu.epn.antdron.hormiga.Hormiga;
import ec.edu.epn.antdron.hormiga.TipoHormiga;
import ec.edu.epn.antdron.alimento.ANectar;
import ec.edu.epn.antdron.pkAntCiberDronContract.IIA;
import ec.edu.epn.antdron.sistema.CoordenadaUK;
import ec.edu.epn.antdron.sistema.SistemaRuso;
import ec.edu.epn.xp.pruebas.Historia;

class AntCiberDronTest {

    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private AntCiberDron dron;
    private FuentePoder fuente;

    private static final List<CoordenadaUK> COORDENADAS = new SistemaRuso().parsear(List.of(
            "Coord-01;01-02;;;;;aaaa",
            "Coord-01;01-02;;;;;a",
            "Coord-00;;;;;;aa",
            "Coord-03;;;03-06;;;abc",
            "Coord-06;;;;04-08;;abcdttttt",
            "Coord-03;;;03-06;;;abc",
            "Coord-05;;;;;05-10;abcdttt",
            "Coord-09;01-02;;;;;ab"));

    @BeforeEach
    void preparar() {
        fuente = new FuentePoder(100);
        dron = new AntCiberDron(new HSoldado(), new Bomba(Set.of(5, 9)), COORDENADAS, fuente,
                new TurboReactor(2), new PrintStream(bytes, true, StandardCharsets.UTF_8));
    }

    private String salida() {
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    @Historia("HU-07")
    void buscarPresentaLaCoordenadaDondeExplota() {
        IIA ia = dron;
        assertTrue(ia.buscar("abcdttt"));
        assertTrue(salida().contains("Coord-05    | abcdttt      | true"));
        assertFalse(ia.buscar("abc"), "Coord-03 no es objetivo");
        assertFalse(ia.buscar("a"), "'a' está en ab* pero Coord-01 no es objetivo");
        assertFalse(ia.buscar("zzz"));
    }

    @Test
    @Historia("HU-07")
    void buscarObjetivosReproduceLaTablaDelEnunciado() {
        List<CoordenadaUK> destruidas = dron.buscarObjetivos();
        assertEquals(List.of("Coord-05", "Coord-09"), destruidas.stream().map(CoordenadaUK::getGeoposicion).toList());
        String s = salida();
        assertTrue(s.contains("[+] COORDENADAS UCRANIANAS A DESTRUIR:"));
        assertTrue(s.contains("Geoposición | Tipo Arsenal | Acción"));
        assertTrue(s.indexOf("Coord-05") < s.indexOf("Coord-09"));
        assertTrue(s.contains("Coord-09    | ab           | true"));
        assertFalse(s.contains("Coord-01"));
    }

    @Test
    @Historia("HU-07")
    void soloUnaHormigaAdultaYVivaUsaElExoesqueleto() {
        PrintStream out = new PrintStream(new ByteArrayOutputStream());
        Bomba bomba = new Bomba(Set.of(5));
        assertThrows(IllegalArgumentException.class, () -> new AntCiberDron(new HLarva(TipoHormiga.SOLDADO),
                bomba, COORDENADAS, new FuentePoder(10), new TurboReactor(1), out));
        Hormiga muerta = new HSoldado();
        muerta.comer(new ANectar());
        assertThrows(IllegalArgumentException.class, () -> new AntCiberDron(muerta, bomba, COORDENADAS,
                new FuentePoder(10), new TurboReactor(1), out));
        assertEquals(new HSoldado().getExtremidades(), dron.getExtremidades());
    }

    @Test
    @Historia("HU-08")
    void metralletaDerechaYLaserIzquierdo() {
        assertThrows(IllegalArgumentException.class,
                () -> dron.equipar(Extremidad.PATA_DELANTERA_IZQUIERDA, new Metralleta(10)));
        assertThrows(IllegalArgumentException.class,
                () -> dron.equipar(Extremidad.PATA_DELANTERA_DERECHA, new Laser(100)));
        assertThrows(IllegalArgumentException.class,
                () -> dron.equipar(Extremidad.PATA_TRASERA_DERECHA, new Metralleta(10)));
        assertThrows(IllegalStateException.class, dron::dispararMetralleta);

        dron.equiparMetralleta(new Metralleta(15));
        dron.equiparLaser(new Laser(200));
        assertEquals("Ráfaga de 10 balas (quedan 5)", dron.dispararMetralleta());
        assertEquals("Ráfaga de 5 balas (quedan 0)", dron.dispararMetralleta());
        assertThrows(IllegalStateException.class, dron::dispararMetralleta);
        assertTrue(dron.armaEn(Extremidad.PATA_DELANTERA_IZQUIERDA).isPresent());
    }

    @Test
    @Historia("HU-08")
    void laPotenciaDelLaserVariaConLaEnergia() {
        dron.equiparLaser(new Laser(200));
        assertEquals(200.0, dron.potenciaLaser(), 1e-9);
        assertEquals("Disparo láser de 200,0 W".replace(',', '.'), dron.dispararLaser().replace(',', '.'));
        assertEquals(95.0, fuente.getEnergia(), 1e-9);
        dron.volar(20);
        assertEquals(110.0, dron.potenciaLaser(), 1e-9);
    }

    @Test
    @Historia("HU-09")
    void volarConsumeEnergiaDelTurboReactorEnLaEspalda() {
        assertEquals("espalda", dron.getUbicacionTurboReactor());
        assertEquals("Vuelo de 10 min con el turbo reactor de la espalda", dron.volar(10));
        assertEquals(80.0, fuente.getEnergia(), 1e-9);
        assertThrows(IllegalStateException.class, () -> dron.volar(41));
        assertEquals(80.0, fuente.getEnergia(), 1e-9, "un vuelo fallido no consume");
        assertThrows(IllegalArgumentException.class, () -> dron.volar(0));
    }

    @Test
    @Historia("HU-09")
    void recargaYReemplazoAsistidosPorLaIIA() {
        dron.volar(30);
        assertTrue(dron.recargarFuente().startsWith("[IIA]"));
        assertEquals(100.0, dron.getFuente().getEnergia(), 1e-9);

        FuentePoder descargada = new FuentePoder(10);
        descargada.consumir(10);
        assertThrows(IllegalArgumentException.class, () -> dron.reemplazarFuente(descargada));
        assertThrows(IllegalArgumentException.class, () -> dron.reemplazarFuente(dron.getFuente()));

        FuentePoder nueva = new FuentePoder(300);
        assertTrue(dron.reemplazarFuente(nueva).startsWith("[IIA]"));
        assertEquals(nueva, dron.getFuente());
    }

    @Test
    @Historia("HU-10")
    void soloExpertosParlantesEntrenanSuIdioma() {
        ExpertoParlante john = new ExpertoParlante("John", Idioma.INGLES);
        ExpertoParlante maria = new ExpertoParlante("María", Idioma.ESPANOL);
        assertThrows(EntrenamientoNoAutorizadoException.class,
                () -> dron.aprender(john, Idioma.ESPANOL, AspectoLinguistico.LEXICO, "hola"));
        assertThrows(EntrenamientoNoAutorizadoException.class,
                () -> dron.aprender(null, Idioma.INGLES, AspectoLinguistico.LEXICO, "hi"));
        assertThrows(IllegalArgumentException.class,
                () -> dron.aprender(john, Idioma.INGLES, AspectoLinguistico.LEXICO, " "));

        dron.aprender(john, Idioma.INGLES, AspectoLinguistico.LEXICO, "tank");
        dron.aprender(john, Idioma.INGLES, AspectoLinguistico.GRAMATICA, "past simple");
        assertEquals(66, dron.getIdiomas().dominio(Idioma.INGLES));
        assertFalse(dron.getIdiomas().puedeHablar(Idioma.INGLES));
        dron.aprender(john, Idioma.INGLES, AspectoLinguistico.FONETICA, "/tæŋk/");
        assertTrue(dron.getIdiomas().puedeHablar(Idioma.INGLES));

        dron.aprender(maria, Idioma.ESPANOL, AspectoLinguistico.FONETICA, "tanque");
        assertEquals(List.of("tanque"), dron.getIdiomas().conocimiento(Idioma.ESPANOL, AspectoLinguistico.FONETICA));
        assertEquals(33, dron.getIdiomas().dominio(Idioma.ESPANOL));
    }
}
