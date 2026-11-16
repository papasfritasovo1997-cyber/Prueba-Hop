package ec.edu.epn.xp.diseno;

/** Genera las tarjetas CRC en Markdown a partir de las clases anotadas con {@link TarjetaCRC}. */
public final class GeneradorCRC {

    private GeneradorCRC() {
    }

    public static boolean tieneTarjeta(Class<?> clase) {
        return clase.isAnnotationPresent(TarjetaCRC.class);
    }

    public static String tarjeta(Class<?> clase) {
        TarjetaCRC crc = clase.getAnnotation(TarjetaCRC.class);
        if (crc == null) {
            throw new IllegalArgumentException(clase.getSimpleName() + " no tiene @TarjetaCRC");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("### Clase: ").append(clase.getSimpleName()).append("\n\n");
        sb.append("| Responsabilidades | Colaboradores |\n|---|---|\n");
        String[] r = crc.responsabilidades();
        String[] c = crc.colaboradores();
        for (int i = 0; i < Math.max(r.length, c.length); i++) {
            sb.append("| ").append(i < r.length ? celda(r[i]) : "")
              .append(" | ").append(i < c.length ? celda(c[i]) : "").append(" |\n");
        }
        return sb.toString();
    }

    /** Tarjetas de todas las clases anotadas; las que no tienen tarjeta se omiten. */
    public static String markdown(Class<?>... clases) {
        StringBuilder sb = new StringBuilder("# Tarjetas CRC\n\n");
        for (Class<?> clase : clases) {
            if (tieneTarjeta(clase)) {
                sb.append(tarjeta(clase)).append('\n');
            }
        }
        return sb.toString();
    }

    private static String celda(String texto) {
        return texto.replace("|", "\\|");
    }
}
