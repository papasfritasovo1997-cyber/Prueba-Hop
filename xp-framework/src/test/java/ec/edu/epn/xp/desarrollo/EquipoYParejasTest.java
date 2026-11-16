package ec.edu.epn.xp.desarrollo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ec.edu.epn.xp.pruebas.Historia;

@Historia("XP-03")
class EquipoYParejasTest {

    @TempDir
    Path dir;

    @Test
    void leeEquipoDesdeConfiguracion() throws IOException {
        Files.writeString(dir.resolve("equipo.properties"), "grupo=3\nnombre=Transformers\nmetafora=Hormiguero\n");
        Files.writeString(dir.resolve("integrantes.csv"), String.join("\n",
                "cedula;nombre;usuario;claveSha256;rol",
                "11036359;Pepito Alimaña;pepito;" + Credenciales.sha256("1") + ";ENTRENADOR",
                "11036353;Juana Galarza;juana;" + Credenciales.sha256("2") + ";cliente"));
        Equipo e = new LectorEquipo().leer(dir);
        assertEquals("03", e.numeroConFormato());
        assertEquals("Transformers", e.getNombre());
        assertEquals(2, e.getIntegrantes().size());
        assertEquals(9, e.getIntegrantes().get(0).ultimoDigito());
        assertEquals(RolXP.CLIENTE, e.buscarPorUsuario("juana").orElseThrow().getRol());
    }

    @Test
    void validaCedulaYFaltaDeArchivos() {
        assertThrows(IllegalArgumentException.class,
                () -> new LectorEquipo().parsearIntegrantes(List.of("12AB;X;x;h;CLIENTE")));
        assertThrows(IOException.class, () -> new LectorEquipo().leer(dir));
    }

    @Test
    void credencialesComparanElHash() {
        String hash = Credenciales.sha256("123");
        assertTrue(Credenciales.coincide("123", hash));
        assertFalse(Credenciales.coincide("1234", hash));
        assertFalse(Credenciales.coincide(null, hash));
    }

    @Test
    void cadaIntegranteTrabajaUnaVezConCadaCompanero() {
        List<String> seis = List.of("A", "B", "C", "D", "E", "F");
        RotacionParejas r = new RotacionParejas(seis);
        Set<Set<String>> vistas = new HashSet<>();
        for (int ronda = 1; ronda <= r.totalRondas(); ronda++) {
            List<List<String>> parejas = r.parejas(ronda);
            assertEquals(3, parejas.size());
            Set<String> enRonda = new HashSet<>();
            parejas.forEach(enRonda::addAll);
            assertEquals(6, enRonda.size(), "nadie repite en la misma ronda");
            parejas.forEach(p -> assertTrue(vistas.add(Set.copyOf(p)), "pareja repetida " + p));
        }
        assertEquals(15, vistas.size());
        assertEquals(r.parejas(1), r.parejas(6));
    }

    @Test
    void rotacionRequiereNumeroPar() {
        assertThrows(IllegalArgumentException.class, () -> new RotacionParejas(List.of("A", "B", "C")));
    }
}
