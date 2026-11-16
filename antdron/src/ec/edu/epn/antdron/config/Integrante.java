package ec.edu.epn.antdron.config;

/**
 * Integrante del equipo leído de {@code config/integrantes.csv}. La cédula puede estar vacía
 * hasta que el integrante la complete.
 */
public record Integrante(String cedula, String nombre, String usuario, String clave, String rol) {

    public Integrante {
        cedula = cedula == null ? "" : cedula.trim();
        nombre = nombre == null ? "" : nombre.trim();
        usuario = usuario == null ? "" : usuario.trim();
        clave = clave == null ? "" : clave.trim();
        rol = rol == null ? "" : rol.trim();
        if (!cedula.isEmpty() && !cedula.matches("\\d{6,13}")) {
            throw new IllegalArgumentException("Cédula inválida '" + cedula + "' de " + nombre
                    + ": debe tener solo dígitos (6 a 13) o quedar vacía");
        }
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("Cada integrante debe tener nombre");
        }
    }

    public boolean tieneCedula() {
        return !cedula.isEmpty();
    }

    /** Puede iniciar sesión si tiene usuario y clave. */
    public boolean puedeIngresar() {
        return !usuario.isEmpty() && !clave.isEmpty();
    }

    /** Último dígito de la cédula: la coordenada objetivo de la bomba. */
    public int ultimoDigito() {
        if (!tieneCedula()) {
            throw new IllegalStateException(nombre + " aún no tiene cédula");
        }
        return cedula.charAt(cedula.length() - 1) - '0';
    }

    public String cedulaVisible() {
        return tieneCedula() ? cedula : "(cédula pendiente)";
    }
}
