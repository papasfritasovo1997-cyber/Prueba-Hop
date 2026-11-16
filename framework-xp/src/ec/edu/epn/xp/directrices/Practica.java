package ec.edu.epn.xp.directrices;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Directrices del framework: cada práctica XP con su fase, el rol responsable, la regla que debe
 * cumplirse y el artefacto que demuestra que se cumple. La ruta del artefacto es la de un proyecto
 * típico y cada proyecto puede cambiarla en {@code xp.properties} con la clave {@code artefacto.<PRACTICA>}.
 */
public enum Practica {

    // I. Planificación
    HISTORIAS_USUARIO(Fase.PLANIFICACION, "Historias de usuario", RolXP.CLIENTE, "xp/historias.csv",
            "Todo requerimiento es una historia \"Como <rol>, quiero <funcionalidad> para <beneficio>\" con "
            + "prioridad, estimación de 1 a 5 puntos y criterios de aceptación. Una historia de más de 5 puntos "
            + "se divide."),
    PLAN_ENTREGAS(Fase.PLANIFICACION, "Plan de entregas", RolXP.ENCARGADO_SEGUIMIENTO, "xp/plan-entregas.md",
            "El cliente elige qué historias entran en cada entrega; cada entrega produce software funcionando."),
    VELOCIDAD(Fase.PLANIFICACION, "Velocidad del proyecto", RolXP.ENCARGADO_SEGUIMIENTO, "xp/historias.csv",
            "En una iteración no se comprometen más puntos que los terminados en la iteración anterior."),
    ITERACIONES(Fase.PLANIFICACION, "Iteraciones", RolXP.ENCARGADO_SEGUIMIENTO, "xp/historias.csv",
            "Iteraciones cortas de duración fija, definida al iniciar el proyecto (en ANTDRON, 1 día). Cada "
            + "historia tiene una iteración asignada y un estado "
            + "(PENDIENTE, EN_PROGRESO, TERMINADA)."),
    ROTACIONES(Fase.PLANIFICACION, "Rotaciones", RolXP.GESTOR, "config/integrantes.csv",
            "Las parejas rotan en cada bloque de trabajo (al menos una vez por iteración) para que todos "
            + "conozcan todo el código."),
    REUNIONES(Fase.PLANIFICACION, "Reuniones", RolXP.GESTOR, "xp/reuniones.md",
            "Reunión diaria de pie de 15 minutos: qué hice, qué haré, qué me bloquea. Se registran los acuerdos."),

    // II. Diseño
    METAFORA(Fase.DISENO, "Metáfora del sistema", RolXP.CONSULTOR, "xp/metafora.md",
            "Una historia corta compartida por todos explica cómo funciona el sistema y da nombre a sus partes."),
    TARJETAS_CRC(Fase.DISENO, "Tarjetas CRC", RolXP.CONSULTOR, "xp/tarjetas-crc.md",
            "Cada clase principal tiene su tarjeta Clase-Responsabilidad-Colaborador y se actualiza con el código."),
    SOLUCIONES_PUNTUALES(Fase.DISENO, "Soluciones puntuales (spikes)", RolXP.CONSULTOR, "xp/spikes.md",
            "Ante una duda técnica se hace un prototipo pequeño y desechable y se anota la conclusión."),
    FUNCIONALIDAD_MINIMA(Fase.DISENO, "Funcionalidad mínima", RolXP.ENTRENADOR, null,
            "Se programa solo lo que piden las historias de la iteración actual; nada \"por si acaso\"."),
    RECICLAJE(Fase.DISENO, "Reciclaje (refactorización)", RolXP.ENTRENADOR, null,
            "Se mejora el diseño sin cambiar el comportamiento, siempre con todas las pruebas en verde."),

    // III. Desarrollo
    DISPONIBILIDAD_CLIENTE(Fase.DESARROLLO, "Disponibilidad del cliente", RolXP.CLIENTE, "config/integrantes.csv",
            "El equipo tiene un integrante con rol CLIENTE que responde dudas y acepta las historias."),
    UNIDAD_PRUEBAS(Fase.DESARROLLO, "Unidad de pruebas", RolXP.ENCARGADO_PRUEBAS, "pruebas",
            "La prueba se escribe antes que el código. Cada clase de prueba lleva @Historia(\"<id de historia>\") "
            + "y se ejecuta con el EjecutorPruebas del framework."),
    PROGRAMACION_PAREJAS(Fase.DESARROLLO, "Programación por parejas", RolXP.GESTOR, "config/integrantes.csv",
            "Todo el código de producción se escribe en parejas; conductor y navegante cambian cada 30 minutos."),
    INTEGRACION(Fase.DESARROLLO, "Integración continua", RolXP.GESTOR, ".github/workflows/ci.yml",
            "Cada push compila y ejecuta todas las pruebas; no se integra nada con la integración en rojo."),

    // IV. Pruebas
    IMPLANTACION(Fase.PRUEBAS, "Implantación", RolXP.CONSULTOR, "Dockerfile",
            "Cada entrega se puede desplegar con un solo comando (contenedor Docker)."),
    PRUEBAS_ACEPTACION(Fase.PRUEBAS, "Pruebas de aceptación", RolXP.ENCARGADO_PRUEBAS, "xp/pruebas-aceptacion.md",
            "Una historia está terminada solo cuando todas sus pruebas de aceptación pasan.");

    private final Fase fase;
    private final String nombre;
    private final RolXP responsable;
    private final String artefactoPorDefecto;
    private final String regla;

    Practica(Fase fase, String nombre, RolXP responsable, String artefactoPorDefecto, String regla) {
        this.fase = fase;
        this.nombre = nombre;
        this.responsable = responsable;
        this.artefactoPorDefecto = artefactoPorDefecto;
        this.regla = regla;
    }

    public Fase getFase() { return fase; }
    public String getNombre() { return nombre; }
    public RolXP getResponsable() { return responsable; }
    public String getRegla() { return regla; }

    /** Vacío si la práctica es una directriz de conducta sin artefacto verificable. */
    public Optional<String> getArtefactoPorDefecto() {
        return Optional.ofNullable(artefactoPorDefecto);
    }

    /** Clave en {@code xp.properties} para cambiar la ruta del artefacto. */
    public String claveConfiguracion() {
        return "artefacto." + name();
    }

    public static List<Practica> deFase(Fase fase) {
        return Arrays.stream(values()).filter(p -> p.fase == fase).toList();
    }
}
