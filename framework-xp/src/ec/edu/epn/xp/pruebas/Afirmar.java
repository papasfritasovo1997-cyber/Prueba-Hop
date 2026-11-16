package ec.edu.epn.xp.pruebas;

import java.util.Objects;

/** Verificaciones de las pruebas. Cada una lanza {@link AssertionError} con un mensaje en español. */
public final class Afirmar {

    /** Código que se espera que lance una excepción. */
    @FunctionalInterface
    public interface Ejecutable {
        void ejecutar() throws Throwable;
    }

    private Afirmar() {
    }

    public static void igual(Object esperado, Object real) {
        igual(esperado, real, null);
    }

    public static void igual(Object esperado, Object real, String mensaje) {
        if (!Objects.equals(esperado, real)) {
            String tipos = esperado != null && real != null && String.valueOf(esperado).equals(String.valueOf(real))
                    ? " (tipos distintos: " + esperado.getClass().getSimpleName() + " y "
                      + real.getClass().getSimpleName() + ")" : "";
            fallar(mensaje, "se esperaba <" + esperado + "> pero fue <" + real + ">" + tipos);
        }
    }

    public static void igual(double esperado, double real, double tolerancia) {
        if (Math.abs(esperado - real) > tolerancia) {
            fallar(null, "se esperaba <" + esperado + "> ± " + tolerancia + " pero fue <" + real + ">");
        }
    }

    public static void verdadero(boolean condicion) {
        verdadero(condicion, null);
    }

    public static void verdadero(boolean condicion, String mensaje) {
        if (!condicion) {
            fallar(mensaje, "se esperaba verdadero");
        }
    }

    public static void falso(boolean condicion) {
        falso(condicion, null);
    }

    public static void falso(boolean condicion, String mensaje) {
        if (condicion) {
            fallar(mensaje, "se esperaba falso");
        }
    }

    public static void noNulo(Object valor) {
        if (valor == null) {
            fallar(null, "se esperaba un valor no nulo");
        }
    }

    public static void contiene(String texto, String fragmento) {
        if (texto == null || !texto.contains(fragmento)) {
            fallar(null, "el texto no contiene <" + fragmento + ">:\n" + texto);
        }
    }

    public static void noContiene(String texto, String fragmento) {
        if (texto != null && texto.contains(fragmento)) {
            fallar(null, "el texto no debería contener <" + fragmento + ">");
        }
    }

    public static <T> T esInstancia(Class<T> tipo, Object valor) {
        if (!tipo.isInstance(valor)) {
            fallar(null, "se esperaba una instancia de " + tipo.getSimpleName() + " pero fue "
                    + (valor == null ? "null" : valor.getClass().getSimpleName()));
        }
        return tipo.cast(valor);
    }

    /** Verifica que el código lance una excepción del tipo indicado y la devuelve para revisar su mensaje. */
    public static <T extends Throwable> T lanza(Class<T> tipo, Ejecutable codigo) {
        try {
            codigo.ejecutar();
        } catch (Throwable t) {
            if (tipo.isInstance(t)) {
                return tipo.cast(t);
            }
            throw new AssertionError("se esperaba " + tipo.getSimpleName() + " pero se lanzó "
                    + t.getClass().getSimpleName() + ": " + t.getMessage(), t);
        }
        throw new AssertionError("se esperaba " + tipo.getSimpleName() + " pero no se lanzó nada");
    }

    public static void fallar(String mensaje) {
        throw new AssertionError(mensaje);
    }

    private static void fallar(String mensaje, String detalle) {
        throw new AssertionError(mensaje == null ? detalle : mensaje + ": " + detalle);
    }
}
