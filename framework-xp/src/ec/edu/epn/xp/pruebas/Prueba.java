package ec.edu.epn.xp.pruebas;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marca un método público sin parámetros como prueba unitaria. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Prueba {

    /** Descripción opcional que se muestra en el reporte. */
    String value() default "";
}
