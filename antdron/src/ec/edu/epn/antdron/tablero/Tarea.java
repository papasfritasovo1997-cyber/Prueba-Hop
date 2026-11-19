package ec.edu.epn.antdron.tablero;

import java.util.List;

/**
 * Fila de {@code xp/tareas.csv}: una tarea de la división de trabajo por fases.
 *
 * @param sesion      PREP, S1, S2, S3 o S4
 * @param practica    práctica XP que defiende (vacía si no defiende una)
 * @param responsables usuarios de {@code config/integrantes.csv} o «todos»
 * @param evidencia   captura en {@code docs/evidencias/} o el entregable (video, PR...)
 */
public record Tarea(String id, String sesion, String practica, List<String> responsables, String descripcion,
                    String artefacto, String evidencia, EstadoTarea estadoDeclarado) {

    public static final List<String> SESIONES = List.of("PREP", "S1", "S2", "S3", "S4");

    public Tarea {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("La tarea no tiene id");
        }
        if (!SESIONES.contains(sesion)) {
            throw new IllegalArgumentException("Tarea " + id + ": sesión '" + sesion + "' (use " + SESIONES + ")");
        }
        if (responsables.isEmpty()) {
            throw new IllegalArgumentException("Tarea " + id + ": falta el responsable");
        }
        responsables = List.copyOf(responsables);
    }

    /** La evidencia es un archivo del repositorio (una captura) y no un video, PR o comentario. */
    public boolean evidenciaEsArchivo() {
        return evidencia.startsWith("docs/");
    }

    public static String nombreSesion(String sesion) {
        return switch (sesion) {
            case "PREP" -> "Preparación";
            case "S1" -> "S1 · Planificación";
            case "S2" -> "S2 · Diseño";
            case "S3" -> "S3 · Desarrollo";
            case "S4" -> "S4 · Pruebas y retrospectiva";
            default -> sesion;
        };
    }
}
