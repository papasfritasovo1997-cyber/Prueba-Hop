package ec.edu.epn.xp.seguimiento;

import ec.edu.epn.xp.planificacion.EstadoHistoria;
import ec.edu.epn.xp.planificacion.HistoriaUsuario;
import ec.edu.epn.xp.planificacion.Prioridad;


import java.util.List;

import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Prueba;

import ec.edu.epn.xp.pruebas.Historia;

@Historia("XP-03")
class PlanEntregasTest {

    private static HistoriaUsuario hu(String id, Prioridad p, int pts, int iter, EstadoHistoria e) {
        return new HistoriaUsuario(id, "t" + id, "rol", "algo", "valor", p, pts, iter, e, List.of(), List.of());
    }

    @Prueba
    void agrupaPorIteracionYCalculaAvance() {
        List<Iteracion> its = PlanEntregas.agrupar(List.of(
                hu("HU-03", Prioridad.ALTA, 3, 2, EstadoHistoria.PENDIENTE),
                hu("HU-01", Prioridad.ALTA, 2, 1, EstadoHistoria.TERMINADA),
                hu("HU-02", Prioridad.MEDIA, 2, 1, EstadoHistoria.EN_PROGRESO),
                hu("HU-04", Prioridad.BAJA, 1, 0, EstadoHistoria.PENDIENTE)));
        Afirmar.igual(2, its.size());
        Afirmar.igual(1, its.get(0).getNumero());
        Afirmar.igual(4, its.get(0).puntosPlanificados());
        Afirmar.igual(2, its.get(0).puntosCompletados());
        Afirmar.igual(50, its.get(0).avance());
        Afirmar.igual(3, its.get(1).puntosPlanificados());
    }

    @Prueba
    void proponeIteracionesPorPrioridadSinSuperarLaVelocidad() {
        List<Iteracion> plan = PlanEntregas.proponer(List.of(
                hu("HU-01", Prioridad.BAJA, 3, 0, EstadoHistoria.PENDIENTE),
                hu("HU-02", Prioridad.ALTA, 3, 0, EstadoHistoria.PENDIENTE),
                hu("HU-03", Prioridad.ALTA, 2, 0, EstadoHistoria.PENDIENTE),
                hu("HU-04", Prioridad.MEDIA, 2, 0, EstadoHistoria.PENDIENTE)), 5, 1);
        Afirmar.igual(2, plan.size());
        Afirmar.igual(List.of("HU-02", "HU-03"), plan.get(0).getHistorias().stream().map(HistoriaUsuario::getId).toList());
        Afirmar.igual(List.of("HU-04", "HU-01"), plan.get(1).getHistorias().stream().map(HistoriaUsuario::getId).toList());
        Afirmar.verdadero(plan.stream().allMatch(it -> it.puntosPlanificados() <= 5));
    }

    @Prueba
    void historiaMasGrandeQueLaVelocidadDebeDividirse() {
        Afirmar.lanza(IllegalArgumentException.class, () -> PlanEntregas.proponer(
                List.of(hu("HU-01", Prioridad.ALTA, 8, 0, EstadoHistoria.PENDIENTE)), 5, 1));
    }

    @Prueba
    void velocidadClimaDeAyerYPromedio() {
        List<Iteracion> its = List.of(
                new Iteracion(1, List.of(hu("A", Prioridad.ALTA, 4, 1, EstadoHistoria.TERMINADA))),
                new Iteracion(2, List.of(hu("B", Prioridad.ALTA, 6, 2, EstadoHistoria.TERMINADA))),
                new Iteracion(3, List.of(hu("C", Prioridad.ALTA, 5, 3, EstadoHistoria.PENDIENTE))));
        Afirmar.igual(6, Velocidad.climaDeAyer(its));
        Afirmar.igual(5.0, Velocidad.promedio(its));
        Afirmar.igual(0, Velocidad.climaDeAyer(List.of()));
    }
}
