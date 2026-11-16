package ec.edu.epn.xp.desarrollo;

import java.util.Objects;

import ec.edu.epn.xp.directrices.RolXP;

/** Integrante del equipo XP. La cédula puede estar vacía mientras el equipo la completa. */
public final class Integrante {

    private final String cedula;
    private final String nombre;
    private final String usuario;
    private final RolXP rol;

    public Integrante(String cedula, String nombre, String usuario, RolXP rol) {
        String c = cedula == null ? "" : cedula.trim();
        if (!c.isEmpty() && !c.matches("\\d{6,13}")) {
            throw new IllegalArgumentException("Cédula inválida '" + cedula + "': solo dígitos (6 a 13) o vacía");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del integrante es obligatorio");
        }
        this.cedula = c;
        this.nombre = nombre.trim();
        this.usuario = usuario == null ? "" : usuario.trim();
        this.rol = Objects.requireNonNull(rol, "rol");
    }

    public boolean tieneCedula() {
        return !cedula.isEmpty();
    }

    public String getCedula() { return cedula; }
    public String getNombre() { return nombre; }
    public String getUsuario() { return usuario; }
    public RolXP getRol() { return rol; }

    @Override
    public String toString() {
        return (tieneCedula() ? cedula : "(cédula pendiente)") + " | " + nombre + " (" + rol.getEtiqueta() + ")";
    }
}
