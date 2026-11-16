package ec.edu.epn.xp.diseno;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

import ec.edu.epn.xp.ComandoXP;
import ec.edu.epn.xp.ContextoXP;
import ec.edu.epn.xp.directrices.Practica;

/**
 * {@code xp crc [dir-fuentes]}: indica qué clases no tienen tarjeta CRC en xp/tarjetas-crc.md.
 * El directorio por defecto se configura con {@code crc.fuentes} en xp.properties.
 */
public final class ComandoCrc implements ComandoXP {

    @Override
    public String nombre() {
        return "crc";
    }

    @Override
    public String descripcion() {
        return "Clases sin tarjeta CRC (opcional: directorio de fuentes)";
    }

    @Override
    public int ejecutar(List<String> argumentos, ContextoXP contexto) throws IOException {
        if (argumentos.size() > 1) {
            throw new IllegalArgumentException("Uso: xp crc [directorio-de-fuentes]");
        }
        Path fuentes = argumentos.isEmpty() ? contexto.ruta("crc.fuentes", "src")
                : contexto.getRaiz().resolve(argumentos.get(0));
        Path tarjetas = contexto.artefacto(Practica.TARJETAS_CRC).orElseThrow();
        RevisorCRC.Revision r = RevisorCRC.revisar(fuentes, tarjetas);
        PrintStream out = contexto.getSalida();
        out.println("Tarjetas CRC (" + contexto.relativa(tarjetas) + ") para " + contexto.relativa(fuentes));
        out.println("  Con tarjeta: " + r.conTarjeta().size() + "/" + r.clases().size() + " (" + r.cobertura() + "%)");
        if (!r.sinTarjeta().isEmpty()) {
            out.println("  Sin tarjeta: " + String.join(", ", r.sinTarjeta()));
        }
        if (!r.sobrantes().isEmpty()) {
            out.println("  Tarjetas de clases que ya no existen: " + String.join(", ", r.sobrantes()));
        }
        return 0;
    }
}
