package ec.edu.epn.xp.planificacion;

import java.util.List;
import java.util.Objects;

/**
 * Historia de usuario XP: "Como [rol], quiero [funcionalidad] para [beneficio]".
 * Inmutable; los cambios de estado generan una copia.
 */
public final class HistoriaUsuario {

    private final String id;
    private final String titulo;
    private final String rol;
    private final String funcionalidad;
    private final String beneficio;
    private final Prioridad prioridad;
    private final int puntos;
    private final int iteracion;
    private final EstadoHistoria estado;
    private final List<String> responsables;
    private final List<String> criteriosAceptacion;

    public HistoriaUsuario(String id, String titulo, String rol, String funcionalidad, String beneficio,
                           Prioridad prioridad, int puntos, int iteracion, EstadoHistoria estado,
                           List<String> responsables, List<String> criteriosAceptacion) {
        this.id = requerido(id, "id");
        this.titulo = requerido(titulo, "titulo");
        this.rol = requerido(rol, "como");
        this.funcionalidad = requerido(funcionalidad, "quiero");
        this.beneficio = requerido(beneficio, "para");
        this.prioridad = Objects.requireNonNull(prioridad, "prioridad");
        if (puntos <= 0) {
            throw new IllegalArgumentException("Historia " + id + ": los puntos de estimación deben ser > 0");
        }
        if (iteracion < 0) {
            throw new IllegalArgumentException("Historia " + id + ": la iteración no puede ser negativa");
        }
        this.puntos = puntos;
        this.iteracion = iteracion;
        this.estado = Objects.requireNonNull(estado, "estado");
        this.responsables = List.copyOf(responsables);
        this.criteriosAceptacion = List.copyOf(criteriosAceptacion);
    }

    private static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' de la historia es obligatorio");
        }
        return valor.trim();
    }

    public String enunciado() {
        return "Como " + rol + ", quiero " + funcionalidad + " para " + beneficio + ".";
    }

    public HistoriaUsuario conEstado(EstadoHistoria nuevoEstado) {
        return new HistoriaUsuario(id, titulo, rol, funcionalidad, beneficio, prioridad, puntos, iteracion,
                nuevoEstado, responsables, criteriosAceptacion);
    }

    public boolean estaTerminada() {
        return estado == EstadoHistoria.TERMINADA;
    }

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getRol() { return rol; }
    public String getFuncionalidad() { return funcionalidad; }
    public String getBeneficio() { return beneficio; }
    public Prioridad getPrioridad() { return prioridad; }
    public int getPuntos() { return puntos; }
    /** 0 = aún sin asignar a una iteración. */
    public int getIteracion() { return iteracion; }
    public EstadoHistoria getEstado() { return estado; }
    public List<String> getResponsables() { return responsables; }
    public List<String> getCriteriosAceptacion() { return criteriosAceptacion; }

    @Override
    public String toString() {
        return id + " [" + prioridad + ", " + puntos + " pts, " + estado + "] " + titulo;
    }
}
