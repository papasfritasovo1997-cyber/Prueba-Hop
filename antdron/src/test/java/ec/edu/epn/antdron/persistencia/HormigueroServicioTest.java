package ec.edu.epn.antdron.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ec.edu.epn.antdron.hormiga.TipoHormiga;
import ec.edu.epn.xp.pruebas.Historia;

@Historia({"HU-11", "HU-12"})
class HormigueroServicioTest {

    @TempDir
    Path dir;

    private HormigueroServicio servicio;

    @BeforeEach
    void preparar() throws IOException, SQLException {
        BaseDatos bd = new BaseDatos(dir.resolve("sub/antdron.db"));
        bd.inicializar();
        bd.inicializar(); // idempotente
        servicio = new HormigueroServicio(bd, TipoHormiga.ZANGANO);
    }

    @Test
    void registraYAutenticaUsuariosConClaveConSal() throws SQLException {
        int id = servicio.registrar("kgd_1", "secreta");
        assertEquals(Optional.of(id), servicio.autenticar("kgd_1", "secreta"));
        assertEquals(Optional.empty(), servicio.autenticar("kgd_1", "otra"));
        assertEquals(Optional.empty(), servicio.autenticar("nadie", "secreta"));
        assertEquals("kgd_1", servicio.nombreUsuario(id));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("kgd_1", "secreta"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("a", "secreta"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("valido", "123"));
        // La misma clave genera hashes distintos (sal aleatoria).
        int otro = servicio.registrar("kgd_2", "secreta");
        assertEquals(Optional.of(otro), servicio.autenticar("kgd_2", "secreta"));
    }

    @Test
    void cicloDeVidaDeLaHormigaSePersiste() throws SQLException {
        int u = servicio.registrar("kgd", "clave");
        int h = servicio.crearHormiga(u, "MACHO");
        assertEquals(List.of(new HormigaRegistro(h, "HLarva", "MACHO", "VIVA",
                servicio.listar(u).get(0).creadaEn())), servicio.listar(u));

        assertTrue(servicio.alimentar(u, h, "nectar").contains("sigue viva"));
        assertTrue(servicio.alimentar(u, h, "Omnívoro").contains("se transformó en HZángano"));
        assertEquals("HZángano", servicio.listar(u).get(0).tipo());
        assertEquals("VIVA", servicio.listar(u).get(0).estado());

        assertTrue(servicio.alimentar(u, h, "omnivoro").contains("sigue viva"));
        assertTrue(servicio.alimentar(u, h, "Carnívoro").contains("murió"));
        assertEquals("MUERTA", servicio.listar(u).get(0).estado());
        assertTrue(servicio.alimentar(u, h, "omnivoro").contains("está muerta"));
    }

    @Test
    void cadaUsuarioSoloVeYAlimentaSusHormigas() throws SQLException {
        int ana = servicio.registrar("ana", "clave");
        int luis = servicio.registrar("luis", "clave");
        int h = servicio.crearHormiga(ana, "HEMBRA");
        assertTrue(servicio.listar(luis).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> servicio.alimentar(luis, h, "nectar"));
        assertThrows(IllegalArgumentException.class, () -> servicio.alimentar(ana, h, "pizza"));
        assertThrows(IllegalArgumentException.class, () -> servicio.crearHormiga(ana, "OTRO"));
    }
}
