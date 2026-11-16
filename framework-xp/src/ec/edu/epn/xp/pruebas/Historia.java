package ec.edu.epn.xp.pruebas;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Vincula una clase de prueba (o un método) con una o varias historias de usuario,
 * p. ej. {@code @Historia("HU-06")}. El método tiene prioridad sobre la clase.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Historia {

    String[] value();
}
