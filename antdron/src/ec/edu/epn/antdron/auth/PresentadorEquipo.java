package ec.edu.epn.antdron.auth;

import java.util.ArrayList;
import java.util.List;

import ec.edu.epn.antdron.config.Equipo;
import ec.edu.epn.antdron.config.Integrante;

/** Texto "[+] TEAM [1]: Transformers" con cada alumno (cédula | nombre), igual que el enunciado. */
public final class PresentadorEquipo {

    private PresentadorEquipo() {
    }

    public static List<String> lineas(Equipo equipo) {
        List<String> lineas = new ArrayList<>();
        lineas.add("[+] TEAM [" + equipo.grupo() + "]: " + equipo.nombre());
        List<Integrante> integrantes = equipo.integrantes();
        for (int i = 0; i < integrantes.size(); i++) {
            Integrante a = integrantes.get(i);
            lineas.add("      • Alumno " + (i + 1) + ": " + a.cedulaVisible() + " | " + a.nombre());
        }
        return lineas;
    }
}
