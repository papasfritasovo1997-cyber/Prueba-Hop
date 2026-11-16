package ec.edu.epn.xp.pruebas;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import ec.edu.epn.xp.ComandoXP;
import ec.edu.epn.xp.ContextoXP;
import ec.edu.epn.xp.directrices.Practica;
import ec.edu.epn.xp.planificacion.HistoriaUsuario;
import ec.edu.epn.xp.planificacion.LectorBacklog;

/**
 * {@code xp aceptacion [--escribir]}: estado de aceptación de cada historia según las pruebas.
 * Con {@code --escribir} guarda el reporte en el artefacto de pruebas de aceptación.
 */
public final class ComandoAceptacion implements ComandoXP {

    @Override
    public String nombre() {
        return "aceptacion";
    }

    @Override
    public String descripcion() {
        return "Reporte de pruebas de aceptación por historia (--escribir: lo guarda en xp/)";
    }

    @Override
    public int ejecutar(List<String> argumentos, ContextoXP contexto) throws IOException {
        boolean escribir = argumentos.contains("--escribir");
        if (argumentos.size() > (escribir ? 1 : 0)) {
            throw new IllegalArgumentException("Uso: xp aceptacion [--escribir]");
        }
        Path backlog = contexto.ruta("backlog", "xp/historias.csv");
        List<HistoriaUsuario> historias = Files.exists(backlog) ? new LectorBacklog().leer(backlog) : null;
        String reporte = ReporteAceptacion.generar(contexto.ruta("resultados", "build/pruebas/resultados.csv"),
                historias);
        contexto.getSalida().print(reporte);
        if (escribir) {
            Path destino = contexto.artefacto(Practica.PRUEBAS_ACEPTACION).orElseThrow();
            Files.createDirectories(destino.toAbsolutePath().getParent());
            Files.writeString(destino, reporte, StandardCharsets.UTF_8);
            contexto.getSalida().println("Guardado en " + contexto.relativa(destino));
        }
        return 0;
    }
}
