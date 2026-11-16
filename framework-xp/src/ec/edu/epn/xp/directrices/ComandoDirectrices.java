package ec.edu.epn.xp.directrices;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import ec.edu.epn.xp.ComandoXP;
import ec.edu.epn.xp.ContextoXP;

/** {@code xp directrices [fase]}: muestra qué exige el framework en cada fase. */
public final class ComandoDirectrices implements ComandoXP {

    @Override
    public String nombre() {
        return "directrices";
    }

    @Override
    public String descripcion() {
        return "Reglas XP por fase: práctica, responsable, artefacto y regla (opcional: una fase)";
    }

    @Override
    public int ejecutar(List<String> argumentos, ContextoXP contexto) throws IOException {
        if (argumentos.size() > 1) {
            throw new IllegalArgumentException("Uso: xp directrices [PLANIFICACION|DISENO|DESARROLLO|PRUEBAS]");
        }
        List<Fase> fases = argumentos.isEmpty() ? Arrays.asList(Fase.values()) : List.of(Fase.desde(argumentos.get(0)));
        PrintStream out = contexto.getSalida();
        for (Fase fase : fases) {
            out.println("== " + fase.titulo() + " ==");
            for (Practica p : Practica.deFase(fase)) {
                out.println("  • " + p.getNombre() + "  [" + p.getResponsable().getEtiqueta() + "]");
                out.println("      Regla: " + p.getRegla());
                out.println("      Artefacto: " + contexto.artefacto(p).map(contexto::relativa)
                        .orElse("(directriz de conducta, sin artefacto)"));
            }
            out.println();
        }
        return 0;
    }
}
