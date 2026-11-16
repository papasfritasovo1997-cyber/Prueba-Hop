package ec.edu.epn.xp.diseno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tarjeta CRC (Clase-Responsabilidad-Colaborador) declarada junto al código,
 * para que el diseño y la implementación no se desincronicen.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TarjetaCRC {

    String[] responsabilidades();

    String[] colaboradores() default {};
}
