package ec.edu.epn.antdron.sistema;

import java.io.PrintStream;

/** Indicador de carga "\|/- 100%" que se muestra antes de presentar cada coordenada. */
public final class Loading {

    public static final String PROPIEDAD_MS = "antdron.loading.ms";
    private static final char[] CUADROS = {'\\', '|', '/', '-'};

    private final PrintStream salida;
    private final long msPorCuadro;

    public Loading(PrintStream salida, long msPorCuadro) {
        this.salida = salida;
        this.msPorCuadro = Math.max(0, msPorCuadro);
    }

    /** Usa la propiedad de sistema {@code antdron.loading.ms} (por defecto 40 ms por cuadro). */
    public static Loading desdeSistema(PrintStream salida) {
        return new Loading(salida, Long.getLong(PROPIEDAD_MS, 40L));
    }

    /** Anima de 0% a 100% en la línea actual y deja escrito "100%" listo para la fila. */
    public void cargar() {
        for (int porcentaje = 0; porcentaje < 100; porcentaje += 10) {
            salida.print("\r" + CUADROS[(porcentaje / 10) % CUADROS.length] + " " + porcentaje + "%  ");
            salida.flush();
            esperar();
        }
        salida.print("\r100%    ");
        salida.flush();
    }

    private void esperar() {
        if (msPorCuadro == 0) {
            return;
        }
        try {
            Thread.sleep(msPorCuadro);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
