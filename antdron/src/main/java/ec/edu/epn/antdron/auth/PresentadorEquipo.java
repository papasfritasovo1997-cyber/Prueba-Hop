package ec.edu.epn.antdron.auth;

import java.io.PrintStream;
import java.util.List;

import ec.edu.epn.xp.desarrollo.Equipo;
import ec.edu.epn.xp.desarrollo.Integrante;

/** Presenta el equipo: "[+] TEAM [1]: Transformers" y cada alumno con cédula y nombre. */
public final class PresentadorEquipo {

    private PresentadorEquipo() {
    }

    public static void presentar(Equipo equipo, PrintStream salida) {
        salida.println();
        salida.println("[+] TEAM [" + equipo.getNumero() + "]: " + equipo.getNombre());
        List<Integrante> integrantes = equipo.getIntegrantes();
        for (int i = 0; i < integrantes.size(); i++) {
            Integrante a = integrantes.get(i);
            salida.println("      • Alumno " + (i + 1) + ": " + a.getCedula() + " | " + a.getNombre());
        }
    }
}
