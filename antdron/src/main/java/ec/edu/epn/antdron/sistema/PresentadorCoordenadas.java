package ec.edu.epn.antdron.sistema;

import java.io.PrintStream;
import java.util.List;

/** Presenta "[+] COORDENADAS UCRANIANAS" con un loading por cada línea leída. */
public final class PresentadorCoordenadas {

    private static final String FORMATO = "%-12s| %-6s| %-7s| %-10s| %-7s| %-8s| %s%n";

    private final PrintStream salida;
    private final Loading loading;

    public PresentadorCoordenadas(PrintStream salida, Loading loading) {
        this.salida = salida;
        this.loading = loading;
    }

    public void presentar(List<CoordenadaUK> coordenadas) {
        salida.println();
        salida.println("[+] COORDENADAS UCRANIANAS:");
        salida.printf("%-8s" + FORMATO, "Loading", "Geoposición", "Lunes", "Martes", "Miércoles", "Jueves",
                "Viernes", "Tipo Arsenal");
        for (CoordenadaUK c : coordenadas) {
            loading.cargar();
            salida.printf(FORMATO, c.getGeoposicion(), c.getHorario(DiaSemana.LUNES), c.getHorario(DiaSemana.MARTES),
                    c.getHorario(DiaSemana.MIERCOLES), c.getHorario(DiaSemana.JUEVES),
                    c.getHorario(DiaSemana.VIERNES), c.getTipoArsenal());
        }
        salida.println("Total: " + coordenadas.size() + " coordenadas");
    }
}
