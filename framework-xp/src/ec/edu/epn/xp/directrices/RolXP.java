package ec.edu.epn.xp.directrices;

import java.util.Locale;

/** Roles de un equipo XP (Kent Beck). Todos los integrantes además programan. */
public enum RolXP {
    ENTRENADOR("Entrenador (Coach)", "Guía el proceso, hace cumplir las directrices y el estándar de código"),
    CLIENTE("Cliente", "Escribe y prioriza las historias de usuario y define sus criterios de aceptación"),
    ENCARGADO_PRUEBAS("Encargado de pruebas (Tester)", "Ejecuta las pruebas de aceptación y difunde los resultados"),
    ENCARGADO_SEGUIMIENTO("Encargado de seguimiento (Tracker)", "Mide la velocidad y el avance de cada iteración"),
    CONSULTOR("Consultor", "Aporta conocimiento técnico específico: diseño, arquitectura y despliegue"),
    GESTOR("Gestor (Big Boss)", "Coordina al equipo, organiza parejas y reuniones y elimina impedimentos");

    private final String etiqueta;
    private final String responsabilidad;

    RolXP(String etiqueta, String responsabilidad) {
        this.etiqueta = etiqueta;
        this.responsabilidad = responsabilidad;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getResponsabilidad() {
        return responsabilidad;
    }

    public static RolXP desde(String texto) {
        try {
            return valueOf(texto.trim().toUpperCase(Locale.ROOT).replace(' ', '_'));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Rol XP inválido: " + texto, e);
        }
    }
}
