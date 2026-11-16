import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * Herramienta de construcción del proyecto (sin Maven): solo necesita un JDK 17 o superior.
 *
 * <pre>
 *   java herramientas/Construir.java [comando]
 *
 *   ejecutar     (por defecto) compila y abre la aplicación JavaFX
 *   consola      compila y ejecuta la versión de consola (login, equipo, coordenadas, bomba)
 *   probar       compila y ejecuta las pruebas del framework y de la aplicación
 *   xp ...       ejecuta un comando del framework XP (xp ayuda)
 *   compilar     solo compila
 *   limpiar      borra build/
 * </pre>
 *
 * JavaFX se descarga una sola vez en .javafx/ (o se usa JAVAFX_HOME si apunta a un SDK de JavaFX).
 */
public class Construir {

    static final String VERSION_JAVAFX = "21.0.5";
    static final String MODULOS_JAVAFX = "javafx.controls";

    static final Path RAIZ = Paths.get("").toAbsolutePath();
    static final Path BUILD = RAIZ.resolve("build");
    static final Path FW_CLASES = BUILD.resolve("framework/clases");
    static final Path FW_PRUEBAS = BUILD.resolve("framework/pruebas");
    static final Path APP_CLASES = BUILD.resolve("antdron/clases");
    static final Path APP_PRUEBAS = BUILD.resolve("antdron/pruebas");
    static final Path RESULTADOS = BUILD.resolve("pruebas/resultados.csv");

    public static void main(String[] args) throws Exception {
        if (!Files.isDirectory(RAIZ.resolve("framework-xp")) || !Files.isDirectory(RAIZ.resolve("antdron"))) {
            error("Ejecute este comando desde la carpeta raíz del proyecto (donde están framework-xp/ y antdron/).");
        }
        String comando = args.length == 0 ? "ejecutar" : args[0];
        List<String> resto = Arrays.asList(args).subList(Math.min(1, args.length), args.length);
        int codigo;
        switch (comando) {
            case "ejecutar" -> codigo = ejecutarApp(resto);
            case "consola" -> {
                List<String> a = new ArrayList<>(List.of("consola"));
                a.addAll(resto);
                codigo = ejecutarApp(a);
            }
            case "probar" -> codigo = probar();
            case "xp" -> {
                compilarFramework();
                List<String> cmd = new ArrayList<>(List.of(java(), "-cp", FW_CLASES.toString(),
                        "ec.edu.epn.xp.XpCli", "--raiz", RAIZ.toString()));
                cmd.addAll(resto);
                codigo = proceso(cmd);
            }
            case "compilar" -> {
                compilarFramework();
                compilarApp();
                codigo = 0;
            }
            case "limpiar" -> {
                borrar(BUILD);
                System.out.println("Se borró " + RAIZ.relativize(BUILD));
                codigo = 0;
            }
            case "ayuda", "--help", "-h" -> {
                ayuda();
                codigo = 0;
            }
            default -> {
                System.err.println("Comando desconocido: " + comando);
                ayuda();
                codigo = 1;
            }
        }
        System.exit(codigo);
    }

    static void ayuda() {
        System.out.println("""
                Uso: antdron [comando]      (Windows: antdron.cmd · Linux/macOS: ./antdron.sh)

                  ejecutar    (por defecto) abre la aplicación JavaFX
                  consola     versión de consola: login, equipo, coordenadas con loading y bomba
                  probar      pruebas del framework y de la aplicación + reporte de aceptación
                  xp ...      comandos del framework XP (antdron xp ayuda)
                  compilar    solo compila
                  limpiar     borra la carpeta build/
                """);
    }

    // ------------------------------------------------------------------ Comandos

    static int ejecutarApp(List<String> argumentos) throws Exception {
        compilarApp();
        Path javafx = javafx();
        List<String> cmd = new ArrayList<>(List.of(java(), "--module-path", javafx.toString(),
                "--add-modules", MODULOS_JAVAFX, "-cp", APP_CLASES.toString()));
        String prism = System.getenv("ANTDRON_PRISM");
        if (prism != null && !prism.isBlank()) {
            cmd.add("-Dprism.order=" + prism);
        }
        cmd.add("ec.edu.epn.antdron.App");
        cmd.addAll(argumentos);
        return proceso(cmd);
    }

    static int probar() throws Exception {
        compilarFramework();
        compilarApp();
        Path javafx = javafx();
        compilar("pruebas del framework", RAIZ.resolve("framework-xp/pruebas"), FW_PRUEBAS,
                List.of("-cp", FW_CLASES.toString()));
        compilar("pruebas de la aplicación", RAIZ.resolve("antdron/pruebas"), APP_PRUEBAS,
                List.of("-cp", FW_CLASES + File.SEP + APP_CLASES, "--module-path", javafx.toString(),
                        "--add-modules", MODULOS_JAVAFX));
        Files.deleteIfExists(RESULTADOS);

        System.out.println("\n=== Pruebas del framework XP ===");
        int fw = proceso(List.of(java(), "-cp", FW_CLASES + File.SEP + FW_PRUEBAS,
                "ec.edu.epn.xp.pruebas.EjecutorPruebas", FW_PRUEBAS.toString(), RESULTADOS.toString()));
        System.out.println("\n=== Pruebas de la aplicación AntCiberDron ===");
        int app = proceso(List.of(java(), "--module-path", javafx.toString(), "--add-modules", MODULOS_JAVAFX,
                "-Djava.awt.headless=true", "-cp", FW_CLASES + File.SEP + APP_CLASES + File.SEP + APP_PRUEBAS,
                "ec.edu.epn.xp.pruebas.EjecutorPruebas", APP_PRUEBAS.toString(), RESULTADOS.toString()));

        System.out.println("\n=== Reporte de aceptación (framework XP) ===");
        proceso(List.of(java(), "-cp", FW_CLASES.toString(), "ec.edu.epn.xp.XpCli", "--raiz", RAIZ.toString(),
                "aceptacion", "--escribir"));
        boolean ok = fw == 0 && app == 0;
        System.out.println(ok ? "\nTODAS LAS PRUEBAS PASARON" : "\nHAY PRUEBAS FALLIDAS (vea el detalle arriba)");
        return ok ? 0 : 1;
    }

    // ---------------------------------------------------------------- Compilación

    static void compilarFramework() throws IOException {
        compilar("framework XP", RAIZ.resolve("framework-xp/src"), FW_CLASES, List.of());
        copiarRecursos(RAIZ.resolve("framework-xp/recursos"), FW_CLASES);
    }

    /** La aplicación se compila SIN el framework en el classpath: así se garantiza que no depende de él. */
    static void compilarApp() throws IOException {
        Path javafx = javafx();
        compilar("aplicación AntCiberDron", RAIZ.resolve("antdron/src"), APP_CLASES,
                List.of("--module-path", javafx.toString(), "--add-modules", MODULOS_JAVAFX));
        copiarRecursos(RAIZ.resolve("antdron/recursos"), APP_CLASES);
    }

    static void compilar(String nombre, Path fuentes, Path destino, List<String> opciones) throws IOException {
        List<String> archivos;
        try (Stream<Path> s = Files.walk(fuentes)) {
            archivos = s.filter(p -> p.toString().endsWith(".java")).map(Path::toString).sorted().toList();
        }
        Path sello = destino.resolve(".compilado");
        if (archivos.isEmpty()) {
            Files.createDirectories(destino);
            return;
        }
        if (Files.exists(sello) && !masNuevo(fuentes, Files.getLastModifiedTime(sello))) {
            return;
        }
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        if (javac == null) {
            error("Se necesita un JDK (no solo un JRE) de Java 17 o superior.");
        }
        System.out.println("Compilando " + nombre + " (" + archivos.size() + " archivos)...");
        borrar(destino);
        Files.createDirectories(destino);
        List<String> a = new ArrayList<>(List.of("-d", destino.toString(), "-encoding", "UTF-8",
                "--release", "17", "-Xlint:-options", "-nowarn"));
        a.addAll(opciones);
        a.addAll(archivos);
        if (javac.run(null, null, null, a.toArray(new String[0])) != 0) {
            error("Falló la compilación de " + nombre + ".");
        }
        Files.writeString(sello, "");
    }

    static boolean masNuevo(Path dir, FileTime referencia) throws IOException {
        try (Stream<Path> s = Files.walk(dir)) {
            return s.filter(Files::isRegularFile).anyMatch(p -> {
                try {
                    return Files.getLastModifiedTime(p).compareTo(referencia) > 0;
                } catch (IOException e) {
                    return true;
                }
            });
        }
    }

    static void copiarRecursos(Path origen, Path destino) throws IOException {
        if (!Files.isDirectory(origen)) {
            return;
        }
        try (Stream<Path> s = Files.walk(origen)) {
            for (Path p : s.filter(Files::isRegularFile).toList()) {
                Path d = destino.resolve(origen.relativize(p).toString());
                Files.createDirectories(d.getParent());
                Files.copy(p, d, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    // --------------------------------------------------------------------- JavaFX

    /** Carpeta lib/ del SDK de JavaFX: JAVAFX_HOME, la copia local en .javafx/ o una descarga nueva. */
    static Path javafx() throws IOException {
        String home = System.getenv("JAVAFX_HOME");
        if (home != null && !home.isBlank()) {
            Path lib = Paths.get(home).resolve("lib");
            if (Files.exists(lib.resolve("javafx.controls.jar"))) {
                return lib;
            }
            System.err.println("Aviso: JAVAFX_HOME no contiene lib/javafx.controls.jar; se usa la copia local.");
        }
        Path base = RAIZ.resolve(".javafx");
        Path lib = base.resolve("javafx-sdk-" + VERSION_JAVAFX).resolve("lib");
        if (Files.exists(lib.resolve("javafx.controls.jar"))) {
            return lib;
        }
        String plataforma = plataforma();
        String url = "https://download2.gluonhq.com/openjfx/" + VERSION_JAVAFX + "/openjfx-" + VERSION_JAVAFX
                + "_" + plataforma + "_bin-sdk.zip";
        System.out.println("Descargando JavaFX " + VERSION_JAVAFX + " (" + plataforma + "), solo la primera vez...");
        System.out.println("  " + url);
        Files.createDirectories(base);
        Path zip = base.resolve("javafx.zip.part");
        try {
            HttpClient cliente = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(30)).build();
            HttpResponse<InputStream> r = cliente.send(HttpRequest.newBuilder(URI.create(url)).GET().build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (r.statusCode() != 200) {
                throw new IOException("respuesta HTTP " + r.statusCode());
            }
            long total = r.headers().firstValueAsLong("Content-Length").orElse(-1);
            try (InputStream in = r.body(); OutputStream out = Files.newOutputStream(zip)) {
                byte[] buf = new byte[1 << 16];
                long leidos = 0;
                int n;
                int ultimo = -1;
                while ((n = in.read(buf)) > 0) {
                    out.write(buf, 0, n);
                    leidos += n;
                    int pct = total > 0 ? (int) (leidos * 100 / total) : -1;
                    if (pct / 10 != ultimo / 10) {
                        System.out.print("\r  " + (pct >= 0 ? pct + "%" : (leidos >> 20) + " MB") + "   ");
                        ultimo = pct;
                    }
                }
            }
            System.out.println("\r  100%  descargado");
            descomprimir(zip, base);
        } catch (IOException | InterruptedException e) {
            error("No se pudo descargar JavaFX: " + e.getMessage() + "\n"
                    + "  Descargue el SDK de JavaFX " + VERSION_JAVAFX + " de https://gluonhq.com/products/javafx/\n"
                    + "  y defina la variable JAVAFX_HOME con la carpeta del SDK (la que contiene lib/).");
        } finally {
            Files.deleteIfExists(zip);
        }
        if (!Files.exists(lib.resolve("javafx.controls.jar"))) {
            error("El SDK de JavaFX descargado no tiene la estructura esperada: " + lib);
        }
        return lib;
    }

    static String plataforma() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        String a = arch.contains("aarch64") || arch.contains("arm64") ? "aarch64" : "x64";
        if (os.contains("win")) {
            return "windows-x64";
        }
        if (os.contains("mac") || os.contains("darwin")) {
            return "osx-" + a;
        }
        return "linux-" + a;
    }

    static void descomprimir(Path zip, Path destino) throws IOException {
        try (ZipInputStream z = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry e;
            while ((e = z.getNextEntry()) != null) {
                Path p = destino.resolve(e.getName()).normalize();
                if (!p.startsWith(destino)) {
                    throw new IOException("Entrada inválida en el ZIP: " + e.getName());
                }
                if (e.isDirectory()) {
                    Files.createDirectories(p);
                } else {
                    Files.createDirectories(p.getParent());
                    Files.copy(z, p, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    // ------------------------------------------------------------------ Utilidades

    static String java() {
        boolean windows = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
        return Paths.get(System.getProperty("java.home"), "bin", windows ? "java.exe" : "java").toString();
    }

    static int proceso(List<String> comando) throws IOException, InterruptedException {
        return new ProcessBuilder(comando).directory(RAIZ.toFile()).inheritIO().start().waitFor();
    }

    static void borrar(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path f, BasicFileAttributes a) throws IOException {
                Files.delete(f);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException {
                Files.delete(d);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    static void error(String mensaje) {
        System.err.println("ERROR: " + mensaje);
        System.exit(2);
    }

    /** Separador del classpath según el sistema operativo. */
    static final class File {
        static final String SEP = java.io.File.pathSeparator;
    }
}
