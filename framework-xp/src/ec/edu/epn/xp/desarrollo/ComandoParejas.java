package ec.edu.epn.xp.desarrollo;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import ec.edu.epn.xp.ComandoXP;
import ec.edu.epn.xp.ContextoXP;

/** {@code xp parejas [ronda]}: rotación de parejas (todas las rondas si se omite). */
public final class ComandoParejas implements ComandoXP {

    @Override
    public String nombre() {
        return "parejas";
    }

    @Override
    public String descripcion() {
        return "Rotación de parejas para programar en pareja (opcional: número de ronda)";
    }

    @Override
    public int ejecutar(List<String> argumentos, ContextoXP contexto) throws IOException {
        Equipo e = new LectorEquipo().leer(contexto.ruta("equipo", "config"));
        RotacionParejas rotacion = new RotacionParejas(e.getIntegrantes().stream().map(Integrante::getNombre).toList());
        int desde = 1;
        int hasta = rotacion.totalRondas();
        if (argumentos.size() > 1) {
            throw new IllegalArgumentException("Uso: xp parejas [ronda]");
        }
        if (argumentos.size() == 1) {
            try {
                desde = Integer.parseInt(argumentos.get(0));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("La ronda debe ser un número: " + argumentos.get(0));
            }
            hasta = desde;
        }
        PrintStream out = contexto.getSalida();
        for (int ronda = desde; ronda <= hasta; ronda++) {
            out.println("Ronda " + ronda + ":");
            rotacion.parejas(ronda).forEach(p -> out.println("   " + p.get(0) + "  <->  " + p.get(1)));
        }
        return 0;
    }
}
