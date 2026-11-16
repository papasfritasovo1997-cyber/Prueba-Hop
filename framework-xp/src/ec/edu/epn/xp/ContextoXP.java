package ec.edu.epn.xp;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import ec.edu.epn.xp.directrices.Practica;

/**
 * Lo que recibe cada comando: la raíz del proyecto que adopta el framework, su configuración
 * ({@code xp.properties}, opcional) y las salidas.
 */
public final class ContextoXP {

    public static final String ARCHIVO_CONFIGURACION = "xp.properties";

    private final Path raiz;
    private final Properties configuracion;
    private final PrintStream salida;
    private final PrintStream errores;

    public ContextoXP(Path raiz, Properties configuracion, PrintStream salida, PrintStream errores) {
        this.raiz = Objects.requireNonNull(raiz, "raiz").toAbsolutePath().normalize();
        this.configuracion = new Properties();
        this.configuracion.putAll(Objects.requireNonNull(configuracion, "configuracion"));
        this.salida = Objects.requireNonNull(salida, "salida");
        this.errores = Objects.requireNonNull(errores, "errores");
    }

    /** Carga {@code xp.properties} de la raíz si existe; si no, usa las rutas por defecto. */
    public static ContextoXP cargar(Path raiz, PrintStream salida, PrintStream errores) {
        Properties p = new Properties();
        Path archivo = raiz.resolve(ARCHIVO_CONFIGURACION);
        if (Files.exists(archivo)) {
            try (Reader r = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
                p.load(r);
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo leer " + archivo, e);
            }
        }
        return new ContextoXP(raiz, p, salida, errores);
    }

    public Path getRaiz() { return raiz; }
    public PrintStream getSalida() { return salida; }
    public PrintStream getErrores() { return errores; }

    public String propiedad(String clave, String porDefecto) {
        String valor = configuracion.getProperty(clave);
        return valor == null || valor.isBlank() ? porDefecto : valor.trim();
    }

    /** Ruta (dentro de la raíz) configurada en {@code clave}, o la ruta por defecto. */
    public Path ruta(String clave, String porDefecto) {
        return raiz.resolve(propiedad(clave, porDefecto)).normalize();
    }

    /** Artefacto que demuestra la práctica, si la práctica tiene uno. */
    public Optional<Path> artefacto(Practica practica) {
        String porDefecto = practica.getArtefactoPorDefecto().orElse(null);
        String valor = propiedad(practica.claveConfiguracion(), porDefecto);
        return Optional.ofNullable(valor).map(v -> raiz.resolve(v).normalize());
    }

    /** Ruta relativa a la raíz, con '/' para que se vea igual en cualquier sistema. */
    public String relativa(Path ruta) {
        return raiz.relativize(ruta.toAbsolutePath().normalize()).toString().replace('\\', '/');
    }
}
