package ec.edu.epn.antdron.hormiga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ec.edu.epn.antdron.alimento.ACarnivoro;
import ec.edu.epn.antdron.alimento.AHerbivoro;
import ec.edu.epn.antdron.alimento.ANectar;
import ec.edu.epn.antdron.alimento.AOmnivoro;
import ec.edu.epn.antdron.alimento.FabricaAlimentos;
import ec.edu.epn.antdron.pkAntCiberDronContract.IHormiga;
import ec.edu.epn.xp.pruebas.Historia;

@Historia("HU-05")
class HormigaTest {

    @Test
    void laLarvaViveConNectar() {
        HLarva larva = new HLarva(TipoHormiga.SOLDADO);
        assertTrue(larva.comer(new ANectar()));
        assertTrue(larva.comer(new ANectar()));
        assertEquals(Hormiga.VIVA, larva.getEstado());
        assertTrue(larva.getEvolucion().isEmpty());
    }

    @Test
    void laLarvaSeTransformaAlComerSuAlimento() {
        HLarva larva = new HLarva(7, "MACHO", TipoHormiga.SOLDADO);
        assertTrue(larva.comer(new ACarnivoro()));
        assertEquals(Hormiga.TRANSFORMADA, larva.getEstado());
        Hormiga soldado = larva.getEvolucion().orElseThrow();
        assertInstanceOf(HSoldado.class, soldado);
        assertEquals(7, soldado.getId());
        assertEquals("MACHO", soldado.getSexo());
        assertTrue(soldado.estaViva());
        assertFalse(larva.comer(new ANectar()), "la larva ya no existe como tal");
    }

    @Test
    void laLarvaMuereConOtroAlimento() {
        HLarva larva = new HLarva(TipoHormiga.SOLDADO);
        assertFalse(larva.comer(new AHerbivoro()));
        assertEquals(Hormiga.MUERTA, larva.getEstado());
        assertFalse(larva.comer(new ANectar()), "una hormiga muerta no come");
    }

    @Test
    void elTipoHormigaViveSoloConSuAlimento() {
        IHormiga soldado = new HSoldado();
        assertTrue(soldado.comer(new ACarnivoro()));
        assertFalse(soldado.comer(new ANectar()));
        assertFalse(soldado.comer(new ACarnivoro()));

        assertTrue(new HZangano().comer(new AOmnivoro()));
        assertFalse(new HZangano().comer(new ACarnivoro()));
        assertTrue(new HRastreadora().comer(new AHerbivoro()));
        assertFalse(new HRastreadora().comer(new AOmnivoro()));
    }

    @Test
    void tipoSegunGrupo() {
        assertEquals(TipoHormiga.SOLDADO, TipoHormiga.paraGrupo(1));
        assertEquals(TipoHormiga.SOLDADO, TipoHormiga.paraGrupo(2));
        assertEquals(TipoHormiga.ZANGANO, TipoHormiga.paraGrupo(3));
        assertEquals(TipoHormiga.ZANGANO, TipoHormiga.paraGrupo(4));
        assertEquals(TipoHormiga.RASTREADORA, TipoHormiga.paraGrupo(5));
        assertThrows(IllegalArgumentException.class, () -> TipoHormiga.paraGrupo(6));
        assertInstanceOf(HZangano.class, TipoHormiga.ZANGANO.crear(1, "HEMBRA"));
    }

    @Test
    void validaSexoYTieneSeisPatas() {
        assertThrows(IllegalArgumentException.class, () -> new HSoldado(1, "X"));
        assertEquals(6, new HSoldado().getExtremidades().stream().filter(Extremidad::esPata).count());
        assertEquals("HSoldado{id=3, sexo=HEMBRA, estado=VIVA}", new HSoldado(3, "hembra").toString());
    }

    @Test
    void fabricaDeAlimentosAceptaNombresSinTilde() {
        assertInstanceOf(ACarnivoro.class, FabricaAlimentos.desde("carnivoro"));
        assertInstanceOf(ANectar.class, FabricaAlimentos.desde("Néctar"));
        assertThrows(IllegalArgumentException.class, () -> FabricaAlimentos.desde("pizza"));
        assertEquals("Omnívoro", new AOmnivoro().toString());
    }
}
