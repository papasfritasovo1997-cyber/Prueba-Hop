package ec.edu.epn.xp.pruebas;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Vincula una prueba (clase o método) con una o varias historias de usuario.
 * Registra automáticamente {@link TrazabilidadExtension}, que guarda el resultado de cada prueba
 * para generar el reporte de pruebas de aceptación por historia.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtendWith(TrazabilidadExtension.class)
public @interface Historia {

    /** Identificadores de historia, p. ej. "HU-05". */
    String[] value();
}
