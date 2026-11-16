package ec.edu.epn.antdron.hormiga;


import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Prueba;

import ec.edu.epn.antdron.alimento.ACarnivoro;
import ec.edu.epn.antdron.alimento.AHerbivoro;
import ec.edu.epn.antdron.alimento.ANectar;
import ec.edu.epn.antdron.alimento.AOmnivoro;
import ec.edu.epn.antdron.alimento.FabricaAlimentos;
import ec.edu.epn.antdron.pkAntCiberDronContract.IHormiga;
import ec.edu.epn.xp.pruebas.Historia;

@Historia("HU-05")
class HormigaTest {

    @Prueba
    void laLarvaViveConNectar() {
        HLarva larva = new HLarva(TipoHormiga.SOLDADO);
        Afirmar.verdadero(larva.comer(new ANectar()));
        Afirmar.verdadero(larva.comer(new ANectar()));
        Afirmar.igual(Hormiga.VIVA, larva.getEstado());
        Afirmar.verdadero(larva.getEvolucion().isEmpty());
    }

    @Prueba
    void laLarvaSeTransformaAlComerSuAlimento() {
        HLarva larva = new HLarva(7, "MACHO", TipoHormiga.SOLDADO);
        Afirmar.verdadero(larva.comer(new ACarnivoro()));
        Afirmar.igual(Hormiga.TRANSFORMADA, larva.getEstado());
        Hormiga soldado = larva.getEvolucion().orElseThrow();
        Afirmar.esInstancia(HSoldado.class, soldado);
        Afirmar.igual(7, soldado.getId());
        Afirmar.igual("MACHO", soldado.getSexo());
        Afirmar.verdadero(soldado.estaViva());
        Afirmar.falso(larva.comer(new ANectar()), "la larva ya no existe como tal");
    }

    @Prueba
    void laLarvaMuereConOtroAlimento() {
        HLarva larva = new HLarva(TipoHormiga.SOLDADO);
        Afirmar.falso(larva.comer(new AHerbivoro()));
        Afirmar.igual(Hormiga.MUERTA, larva.getEstado());
        Afirmar.falso(larva.comer(new ANectar()), "una hormiga muerta no come");
    }

    @Prueba
    void elTipoHormigaViveSoloConSuAlimento() {
        IHormiga soldado = new HSoldado();
        Afirmar.verdadero(soldado.comer(new ACarnivoro()));
        Afirmar.falso(soldado.comer(new ANectar()));
        Afirmar.falso(soldado.comer(new ACarnivoro()));

        Afirmar.verdadero(new HZangano().comer(new AOmnivoro()));
        Afirmar.falso(new HZangano().comer(new ACarnivoro()));
        Afirmar.verdadero(new HRastreadora().comer(new AHerbivoro()));
        Afirmar.falso(new HRastreadora().comer(new AOmnivoro()));
    }

    @Prueba
    void tipoSegunGrupo() {
        Afirmar.igual(TipoHormiga.SOLDADO, TipoHormiga.paraGrupo(1));
        Afirmar.igual(TipoHormiga.SOLDADO, TipoHormiga.paraGrupo(2));
        Afirmar.igual(TipoHormiga.ZANGANO, TipoHormiga.paraGrupo(3));
        Afirmar.igual(TipoHormiga.ZANGANO, TipoHormiga.paraGrupo(4));
        Afirmar.igual(TipoHormiga.RASTREADORA, TipoHormiga.paraGrupo(5));
        Afirmar.lanza(IllegalArgumentException.class, () -> TipoHormiga.paraGrupo(6));
        Afirmar.esInstancia(HZangano.class, TipoHormiga.ZANGANO.crear(1, "HEMBRA"));
    }

    @Prueba
    void validaSexoYTieneSeisPatas() {
        Afirmar.lanza(IllegalArgumentException.class, () -> new HSoldado(1, "X"));
        Afirmar.igual(6L, new HSoldado().getExtremidades().stream().filter(Extremidad::esPata).count());
        Afirmar.igual("HSoldado{id=3, sexo=HEMBRA, estado=VIVA}", new HSoldado(3, "hembra").toString());
    }

    @Prueba
    void fabricaDeAlimentosAceptaNombresSinTilde() {
        Afirmar.esInstancia(ACarnivoro.class, FabricaAlimentos.desde("carnivoro"));
        Afirmar.esInstancia(ANectar.class, FabricaAlimentos.desde("Néctar"));
        Afirmar.lanza(IllegalArgumentException.class, () -> FabricaAlimentos.desde("pizza"));
        Afirmar.igual("Omnívoro", new AOmnivoro().toString());
    }
}
