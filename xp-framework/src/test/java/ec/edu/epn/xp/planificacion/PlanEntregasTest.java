package ec.edu.epn.xp.planificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import ec.edu.epn.xp.pruebas.Historia;

@Historia("XP-02")
class PlanEntregasTest {

    private static HistoriaUsuario hu(String id, Prioridad p, int pts, int iter, EstadoHistoria e) {
        return new HistoriaUsuario(id, "t" + id, "rol", "algo", "valor", p, pts, iter, e, List.of(), List.of());
    }

    @Test
    void agrupaPorIteracionYCalculaAvance() {
        List<Iteracion> its = PlanEntregas.agrupar(List.of(
                hu("HU-03", Prioridad.ALTA, 3, 2, EstadoHistoria.PENDIENTE),
                hu("HU-01", Prioridad.ALTA, 2, 1, EstadoHistoria.TERMINADA),
                hu("HU-02", Prioridad.MEDIA, 2, 1, EstadoHistoria.EN_PROGRESO),
                hu("HU-04", Prioridad.BAJA, 1, 0, EstadoHistoria.PENDIENTE)));
        assertEquals(2, its.size());
        assertEquals(1, its.get(0).getNumero());
        assertEquals(4, its.get(0).puntosPlanificados());
        assertEquals(2, its.get(0).puntosCompletados());
        assertEquals(50, its.get(0).avance());
        assertEquals(3, its.get(1).puntosPlanificados());
    }

    @Test
    void proponeIteracionesPorPrioridadSinSuperarLaVelocidad() {
        List<Iteracion> plan = PlanEntregas.proponer(List.of(
                hu("HU-01", Prioridad.BAJA, 3, 0, EstadoHistoria.PENDIENTE),
                hu("HU-02", Prioridad.ALTA, 3, 0, EstadoHistoria.PENDIENTE),
                hu("HU-03", Prioridad.ALTA, 2, 0, EstadoHistoria.PENDIENTE),
                hu("HU-04", Prioridad.MEDIA, 2, 0, EstadoHistoria.PENDIENTE)), 5, 1);
        assertEquals(2, plan.size());
        assertEquals(List.of("HU-02", "HU-03"), plan.get(0).getHistorias().stream().map(HistoriaUsuario::getId).toList());
        assertEquals(List.of("HU-04", "HU-01"), plan.get(1).getHistorias().stream().map(HistoriaUsuario::getId).toList());
        assertTrue(plan.stream().allMatch(it -> it.puntosPlanificados() <= 5));
    }

    @Test
    void historiaMasGrandeQueLaVelocidadDebeDividirse() {
        assertThrows(IllegalArgumentException.class, () -> PlanEntregas.proponer(
                List.of(hu("HU-01", Prioridad.ALTA, 8, 0, EstadoHistoria.PENDIENTE)), 5, 1));
    }

    @Test
    void velocidadClimaDeAyerYPromedio() {
        List<Iteracion> its = List.of(
                new Iteracion(1, List.of(hu("A", Prioridad.ALTA, 4, 1, EstadoHistoria.TERMINADA))),
                new Iteracion(2, List.of(hu("B", Prioridad.ALTA, 6, 2, EstadoHistoria.TERMINADA))),
                new Iteracion(3, List.of(hu("C", Prioridad.ALTA, 5, 3, EstadoHistoria.PENDIENTE))));
        assertEquals(6, Velocidad.climaDeAyer(its));
        assertEquals(5.0, Velocidad.promedio(its));
        assertEquals(0, Velocidad.climaDeAyer(List.of()));
    }
}
