package ec.edu.epn.xp.desarrollo;

import java.util.Objects;

/** Integrante del equipo XP (alumno). */
public final class Integrante {

    private final String cedula;
    private final String nombre;
    private final String usuario;
    private final String claveHash;
    private final RolXP rol;

    public Integrante(String cedula, String nombre, String usuario, String claveHash, RolXP rol) {
        if (cedula == null || !cedula.trim().matches("\\d{6,13}")) {
            throw new IllegalArgumentException("Cédula inválida '" + cedula + "': solo dígitos (6 a 13)");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del integrante es obligatorio");
        }
        if (usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException("El usuario del integrante es obligatorio");
        }
        this.cedula = cedula.trim();
        this.nombre = nombre.trim();
        this.usuario = usuario.trim();
        this.claveHash = Objects.requireNonNull(claveHash, "claveHash").trim();
        this.rol = Objects.requireNonNull(rol, "rol");
    }

    /** Último dígito de la cédula: define la coordenada objetivo del autómata. */
    public int ultimoDigito() {
        return cedula.charAt(cedula.length() - 1) - '0';
    }

    public String getCedula() { return cedula; }
    public String getNombre() { return nombre; }
    public String getUsuario() { return usuario; }
    public String getClaveHash() { return claveHash; }
    public RolXP getRol() { return rol; }

    @Override
    public String toString() {
        return cedula + " | " + nombre + " (" + rol.getEtiqueta() + ")";
    }
}
