package ec.edu.epn.antdron.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/**
 * Comparación de contraseñas. En {@code config/integrantes.csv} la clave puede escribirse tal cual
 * o como {@code sha256:<hex>} para no dejarla legible en el repositorio.
 */
public final class Claves {

    public static final String PREFIJO_SHA256 = "sha256:";

    private Claves() {
    }

    public static String sha256(String texto) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    /** True si {@code clave} corresponde a {@code almacenada} (texto plano o sha256:...). */
    public static boolean coincide(String clave, String almacenada) {
        if (clave == null || almacenada == null || almacenada.isEmpty()) {
            return false;
        }
        byte[] a;
        byte[] b;
        if (almacenada.toLowerCase(Locale.ROOT).startsWith(PREFIJO_SHA256)) {
            a = sha256(clave).getBytes(StandardCharsets.UTF_8);
            b = almacenada.substring(PREFIJO_SHA256.length()).trim().toLowerCase(Locale.ROOT)
                    .getBytes(StandardCharsets.UTF_8);
        } else {
            a = clave.getBytes(StandardCharsets.UTF_8);
            b = almacenada.getBytes(StandardCharsets.UTF_8);
        }
        return MessageDigest.isEqual(a, b);
    }
}
