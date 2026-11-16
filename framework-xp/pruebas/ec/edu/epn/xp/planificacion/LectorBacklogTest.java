package ec.edu.epn.xp.planificacion;


import java.util.List;

import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Prueba;

import ec.edu.epn.xp.pruebas.Historia;

@Historia("XP-02")
class LectorBacklogTest {

    private final LectorBacklog lector = new LectorBacklog();

    @Prueba
    void leeHistoriasIgnorandoCabeceraYComentarios() {
        List<HistoriaUsuario> h = lector.parsear(List.of(
                LectorBacklog.CABECERA,
                "# comentario",
                "",
                "HU-01;Login;profesor;autenticarme;usar el sistema;ALTA;3;1;TERMINADA;Ana,Luis;3 intentos|Mensaje de error"));
        Afirmar.igual(1, h.size());
        HistoriaUsuario hu = h.get(0);
        Afirmar.igual("HU-01", hu.getId());
        Afirmar.igual(Prioridad.ALTA, hu.getPrioridad());
        Afirmar.igual(List.of("Ana", "Luis"), hu.getResponsables());
        Afirmar.igual(List.of("3 intentos", "Mensaje de error"), hu.getCriteriosAceptacion());
        Afirmar.verdadero(hu.estaTerminada());
        Afirmar.igual("Como profesor, quiero autenticarme para usar el sistema.", hu.enunciado());
    }

    @Prueba
    void iteracionVaciaSignificaSinAsignar() {
        HistoriaUsuario hu = lector.parsear(List.of("HU-02;X;a;b;c;BAJA;2;;;;")).get(0);
        Afirmar.igual(0, hu.getIteracion());
        Afirmar.igual(EstadoHistoria.PENDIENTE, hu.getEstado());
    }

    @Prueba
    void rechazaLineasMalFormadasIndicandoLaLinea() {
        IllegalArgumentException e = Afirmar.lanza(IllegalArgumentException.class,
                () -> lector.parsear(List.of("HU-01;solo;tres")));
        Afirmar.verdadero(e.getMessage().contains("línea 1"));
        Afirmar.lanza(IllegalArgumentException.class,
                () -> lector.parsear(List.of("HU-01;X;a;b;c;URGENTE;3;1;;;")));
        Afirmar.lanza(IllegalArgumentException.class,
                () -> lector.parsear(List.of("HU-01;X;a;b;c;ALTA;0;1;;;")));
    }
}
