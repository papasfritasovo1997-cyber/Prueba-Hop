package ec.edu.epn.xp.directrices;

import java.util.Locale;

/** Las cuatro fases de XP que el framework exige seguir. */
public enum Fase {
    PLANIFICACION("I", "Planificación"),
    DISENO("II", "Diseño"),
    DESARROLLO("III", "Desarrollo"),
    PRUEBAS("IV", "Pruebas");

    private final String numero;
    private final String etiqueta;

    Fase(String numero, String etiqueta) {
        this.numero = numero;
        this.etiqueta = etiqueta;
    }

    public String getNumero() {
        return numero;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String titulo() {
        return numero + ". " + etiqueta;
    }

    /** Acepta el nombre del enum o la etiqueta, con o sin tilde: "diseno", "Diseño". */
    public static Fase desde(String texto) {
        if (texto != null) {
            String clave = normalizar(texto);
            for (Fase f : values()) {
                if (normalizar(f.name()).equals(clave) || normalizar(f.etiqueta).equals(clave)) {
                    return f;
                }
            }
        }
        throw new IllegalArgumentException("Fase desconocida: " + texto
                + " (use PLANIFICACION, DISENO, DESARROLLO o PRUEBAS)");
    }

    private static String normalizar(String texto) {
        return java.text.Normalizer.normalize(texto.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
