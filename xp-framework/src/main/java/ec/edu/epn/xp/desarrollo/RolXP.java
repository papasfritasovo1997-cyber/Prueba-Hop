package ec.edu.epn.xp.desarrollo;

import java.util.Locale;

/** Roles definidos por Kent Beck para un equipo XP. Todos los integrantes además programan. */
public enum RolXP {
    ENTRENADOR("Entrenador (Coach)", "Guía el proceso XP, vela por las prácticas y el estándar de código"),
    CLIENTE("Cliente", "Escribe y prioriza historias de usuario y define las pruebas de aceptación"),
    ENCARGADO_PRUEBAS("Encargado de pruebas (Tester)", "Ejecuta pruebas de aceptación y difunde resultados"),
    ENCARGADO_SEGUIMIENTO("Encargado de seguimiento (Tracker)", "Mide la velocidad y el avance de cada iteración"),
    CONSULTOR("Consultor", "Aporta conocimiento técnico específico (arquitectura, contenedores)"),
    GESTOR("Gestor (Big Boss)", "Coordina al equipo con el cliente y elimina impedimentos"),
    PROGRAMADOR("Programador", "Escribe pruebas y código en parejas");

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
