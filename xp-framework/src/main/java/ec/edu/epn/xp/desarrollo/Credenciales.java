package ec.edu.epn.xp.desarrollo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Utilidad de hash para no guardar contraseñas en texto plano en la configuración del equipo. */
public final class Credenciales {

    private Credenciales() {
    }

    public static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    /** Comparación en tiempo constante. */
    public static boolean coincide(String clave, String hashEsperado) {
        if (clave == null || hashEsperado == null) {
            return false;
        }
        return MessageDigest.isEqual(sha256(clave).getBytes(StandardCharsets.UTF_8),
                hashEsperado.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
    }
}
