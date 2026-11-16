package ec.edu.epn.xp.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.io.TempDir;

import ec.edu.epn.xp.planificacion.EstadoHistoria;
import ec.edu.epn.xp.planificacion.HistoriaUsuario;
import ec.edu.epn.xp.planificacion.Prioridad;

@Historia("XP-05")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReporteAceptacionTest {

    @Test
    void resumePorHistoriaConUltimoResultadoDeCadaPrueba() {
        Map<String, ReporteAceptacion.Resumen> r = ReporteAceptacion.resumir(List.of(
                "HU-01;A#a;FALLO",
                "HU-01;A#a;PASO",
                "HU-01;A#b;PASO",
                "HU-02;B#a;FALLO",
                "HU-03;C#a;DESHABILITADA",
                "basura"));
        assertEquals(new ReporteAceptacion.Resumen("HU-01", 2, 0, 0), r.get("HU-01"));
        assertEquals(ReporteAceptacion.Estado.ACEPTADA, r.get("HU-01").estado());
        assertEquals(ReporteAceptacion.Estado.RECHAZADA, r.get("HU-02").estado());
        assertEquals(ReporteAceptacion.Estado.RECHAZADA, r.get("HU-03").estado());
    }

    @Test
    void markdownIncluyeHistoriasSinPruebas() {
        HistoriaUsuario hu = new HistoriaUsuario("HU-09", "Idiomas", "a", "b", "c", Prioridad.BAJA, 1, 3,
                EstadoHistoria.PENDIENTE, List.of(), List.of());
        String md = ReporteAceptacion.markdown(ReporteAceptacion.resumir(List.of("HU-01;A#a;PASO")), List.of(hu));
        assertTrue(md.contains("| HU-01 |  | 1 | 0 | 0 | ACEPTADA |"));
        assertTrue(md.contains("| HU-09 | Idiomas | 0 | 0 | 0 | SIN_PRUEBAS |"));
        assertTrue(md.contains("Historias aceptadas: 1/2"));
    }

    @Test
    void generarExigeQueExistanResultados(@TempDir Path dir) {
        assertThrows(IOException.class, () -> ReporteAceptacion.generar(dir.resolve("no.csv"), null));
    }

    @Test
    @Order(Integer.MAX_VALUE)
    void laExtensionRegistraEstaPrueba() throws IOException {
        // Las pruebas previas de esta clase ya pasaron por TrazabilidadExtension (@Historia en la clase).
        Path archivo = TrazabilidadExtension.archivoResultados();
        assertTrue(Files.exists(archivo), "debe existir " + archivo);
        assertTrue(Files.readString(archivo).contains("XP-05;ReporteAceptacionTest#"));
    }
}
