package ec.edu.epn.xp;

import java.io.IOException;
import java.util.List;

/**
 * Punto de extensión del framework. Cada módulo (planificación, diseño, desarrollo, pruebas)
 * agrega sus comandos implementando esta interfaz y registrándola en
 * {@code src/main/resources/META-INF/services/ec.edu.epn.xp.ComandoXP} (una clase por línea).
 * {@link XpCli} los descubre con {@link java.util.ServiceLoader}; el núcleo no los conoce.
 */
public interface ComandoXP {

    /** Nombre con el que se invoca, en minúsculas y sin espacios: "backlog", "velocidad". */
    String nombre();

    /** Una línea que aparece en {@code xp ayuda}. */
    String descripcion();

    /**
     * @param argumentos argumentos que siguen al nombre del comando
     * @return 0 si se cumplió lo pedido, 1 si el uso es incorrecto o la verificación falló
     * @throws IllegalArgumentException con un mensaje para el usuario si los datos son inválidos
     */
    int ejecutar(List<String> argumentos, ContextoXP contexto) throws IOException;
}
