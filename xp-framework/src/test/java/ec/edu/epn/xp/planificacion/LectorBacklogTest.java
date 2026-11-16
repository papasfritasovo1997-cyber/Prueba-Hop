package ec.edu.epn.xp.planificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import ec.edu.epn.xp.pruebas.Historia;

@Historia("XP-01")
class LectorBacklogTest {

    private final LectorBacklog lector = new LectorBacklog();

    @Test
    void leeHistoriasIgnorandoCabeceraYComentarios() {
        List<HistoriaUsuario> h = lector.parsear(List.of(
                LectorBacklog.CABECERA,
                "# comentario",
                "",
                "HU-01;Login;profesor;autenticarme;usar el sistema;ALTA;3;1;TERMINADA;Ana,Luis;3 intentos|Mensaje de error"));
        assertEquals(1, h.size());
        HistoriaUsuario hu = h.get(0);
        assertEquals("HU-01", hu.getId());
        assertEquals(Prioridad.ALTA, hu.getPrioridad());
        assertEquals(List.of("Ana", "Luis"), hu.getResponsables());
        assertEquals(List.of("3 intentos", "Mensaje de error"), hu.getCriteriosAceptacion());
        assertTrue(hu.estaTerminada());
        assertEquals("Como profesor, quiero autenticarme para usar el sistema.", hu.enunciado());
    }

    @Test
    void iteracionVaciaSignificaSinAsignar() {
        HistoriaUsuario hu = lector.parsear(List.of("HU-02;X;a;b;c;BAJA;2;;;;")).get(0);
        assertEquals(0, hu.getIteracion());
        assertEquals(EstadoHistoria.PENDIENTE, hu.getEstado());
    }

    @Test
    void rechazaLineasMalFormadasIndicandoLaLinea() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> lector.parsear(List.of("HU-01;solo;tres")));
        assertTrue(e.getMessage().contains("línea 1"));
        assertThrows(IllegalArgumentException.class,
                () -> lector.parsear(List.of("HU-01;X;a;b;c;URGENTE;3;1;;;")));
        assertThrows(IllegalArgumentException.class,
                () -> lector.parsear(List.of("HU-01;X;a;b;c;ALTA;0;1;;;")));
    }
}
