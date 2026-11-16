package ec.edu.epn.xp.pruebas;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Prueba;
import ec.edu.epn.xp.pruebas.Temporal;

import ec.edu.epn.xp.planificacion.EstadoHistoria;
import ec.edu.epn.xp.planificacion.HistoriaUsuario;
import ec.edu.epn.xp.planificacion.Prioridad;

@Historia("XP-05")
class ReporteAceptacionTest {

    @Prueba
    void resumePorHistoriaConUltimoResultadoDeCadaPrueba() {
        Map<String, ReporteAceptacion.Resumen> r = ReporteAceptacion.resumir(List.of(
                "HU-01;A#a;FALLO",
                "HU-01;A#a;PASO",
                "HU-01;A#b;PASO",
                "HU-02;B#a;FALLO",
                "HU-03;C#a;DESHABILITADA",
                "basura"));
        Afirmar.igual(new ReporteAceptacion.Resumen("HU-01", 2, 0, 0), r.get("HU-01"));
        Afirmar.igual(ReporteAceptacion.Estado.ACEPTADA, r.get("HU-01").estado());
        Afirmar.igual(ReporteAceptacion.Estado.RECHAZADA, r.get("HU-02").estado());
        Afirmar.igual(ReporteAceptacion.Estado.RECHAZADA, r.get("HU-03").estado());
    }

    @Prueba
    void markdownIncluyeHistoriasSinPruebas() {
        HistoriaUsuario hu = new HistoriaUsuario("HU-09", "Idiomas", "a", "b", "c", Prioridad.BAJA, 1, 3,
                EstadoHistoria.PENDIENTE, List.of(), List.of());
        String md = ReporteAceptacion.markdown(ReporteAceptacion.resumir(List.of("HU-01;A#a;PASO")), List.of(hu));
        Afirmar.verdadero(md.contains("| HU-01 |  | 1 | 0 | 0 | ACEPTADA |"));
        Afirmar.verdadero(md.contains("| HU-09 | Idiomas | 0 | 0 | 0 | SIN_PRUEBAS |"));
        Afirmar.verdadero(md.contains("Historias aceptadas: 1/2"));
    }

    @Prueba
    void generarExigeQueExistanResultados() {
        Path dir = Temporal.directorio();
        Afirmar.lanza(IOException.class, () -> ReporteAceptacion.generar(dir.resolve("no.csv"), null));
    }
}
