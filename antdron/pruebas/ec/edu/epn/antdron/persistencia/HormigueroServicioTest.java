package ec.edu.epn.antdron.persistencia;


import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import ec.edu.epn.xp.pruebas.AntesDeCada;
import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Prueba;
import ec.edu.epn.xp.pruebas.Temporal;

import ec.edu.epn.antdron.hormiga.TipoHormiga;
import ec.edu.epn.xp.pruebas.Historia;

@Historia({"HU-11", "HU-12"})
class HormigueroServicioTest {


    Path dir = Temporal.directorio();

    private HormigueroServicio servicio;

    @AntesDeCada
    void preparar() {
        servicio = new HormigueroServicio(dir.resolve("sub"), TipoHormiga.ZANGANO);
    }

    @Prueba
    void registraYAutenticaUsuariosConClaveConSal() {
        int id = servicio.registrar("kgd_1", "secreta");
        Afirmar.igual(Optional.of(id), servicio.autenticar("kgd_1", "secreta"));
        Afirmar.igual(Optional.empty(), servicio.autenticar("kgd_1", "otra"));
        Afirmar.igual(Optional.empty(), servicio.autenticar("nadie", "secreta"));
        Afirmar.igual("kgd_1", servicio.nombreUsuario(id));
        Afirmar.lanza(IllegalArgumentException.class, () -> servicio.registrar("kgd_1", "secreta"));
        Afirmar.lanza(IllegalArgumentException.class, () -> servicio.registrar("a", "secreta"));
        Afirmar.lanza(IllegalArgumentException.class, () -> servicio.registrar("valido", "123"));
        // La misma clave genera hashes distintos (sal aleatoria).
        int otro = servicio.registrar("kgd_2", "secreta");
        Afirmar.igual(Optional.of(otro), servicio.autenticar("kgd_2", "secreta"));
    }

    @Prueba
    void cicloDeVidaDeLaHormigaSePersiste() {
        int u = servicio.registrar("kgd", "clave");
        int h = servicio.crearHormiga(u, "MACHO");
        Afirmar.igual(List.of(new HormigaRegistro(h, "HLarva", "MACHO", "VIVA",
                servicio.listar(u).get(0).creadaEn())), servicio.listar(u));

        Afirmar.verdadero(servicio.alimentar(u, h, "nectar").contains("sigue viva"));
        Afirmar.verdadero(servicio.alimentar(u, h, "Omnívoro").contains("se transformó en HZángano"));
        Afirmar.igual("HZángano", servicio.listar(u).get(0).tipo());
        Afirmar.igual("VIVA", servicio.listar(u).get(0).estado());

        Afirmar.verdadero(servicio.alimentar(u, h, "omnivoro").contains("sigue viva"));
        Afirmar.verdadero(servicio.alimentar(u, h, "Carnívoro").contains("murió"));
        Afirmar.igual("MUERTA", servicio.listar(u).get(0).estado());
        Afirmar.verdadero(servicio.alimentar(u, h, "omnivoro").contains("está muerta"));
    }

    @Prueba
    void cadaUsuarioSoloVeYAlimentaSusHormigas() {
        int ana = servicio.registrar("ana", "clave");
        int luis = servicio.registrar("luis", "clave");
        int h = servicio.crearHormiga(ana, "HEMBRA");
        Afirmar.verdadero(servicio.listar(luis).isEmpty());
        Afirmar.lanza(IllegalArgumentException.class, () -> servicio.alimentar(luis, h, "nectar"));
        Afirmar.lanza(IllegalArgumentException.class, () -> servicio.alimentar(ana, h, "pizza"));
        Afirmar.lanza(IllegalArgumentException.class, () -> servicio.crearHormiga(ana, "OTRO"));
    }
}
