package ec.edu.epn.xp.directrices;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ec.edu.epn.xp.pruebas.Historia;
import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Prueba;
import ec.edu.epn.xp.pruebas.Temporal;

import ec.edu.epn.xp.ContextoXP;

@Historia("XP-01")
class DirectricesTest {


    Path raiz = Temporal.directorio();

    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();

    private ContextoXP contexto() {
        PrintStream out = new PrintStream(salida, true, StandardCharsets.UTF_8);
        return ContextoXP.cargar(raiz, out, out);
    }

    private String out() {
        return salida.toString(StandardCharsets.UTF_8);
    }

    @Prueba
    void cubreLasCuatroFasesYTodasLasPracticasDelProcesoXP() {
        Afirmar.igual(17, Practica.values().length);
        Afirmar.igual(List.of(6, 5, 4, 2),
                Arrays.stream(Fase.values()).map(f -> Practica.deFase(f).size()).toList());
        for (Practica p : Practica.values()) {
            Afirmar.falso(p.getRegla().isBlank(), p + " debe tener una regla");
        }
        Set<Practica> sinArtefacto = Arrays.stream(Practica.values())
                .filter(p -> p.getArtefactoPorDefecto().isEmpty()).collect(Collectors.toSet());
        Afirmar.igual(EnumSet.of(Practica.FUNCIONALIDAD_MINIMA, Practica.RECICLAJE), sinArtefacto);
        Set<RolXP> responsables = Arrays.stream(Practica.values()).map(Practica::getResponsable)
                .collect(Collectors.toSet());
        Afirmar.igual(EnumSet.allOf(RolXP.class), responsables, "cada rol es responsable de alguna práctica");
    }

    @Prueba
    void faseYRolSeLeenConOSinTilde() {
        Afirmar.igual(Fase.DISENO, Fase.desde("Diseño"));
        Afirmar.igual(Fase.DISENO, Fase.desde(" diseno "));
        Afirmar.igual(Fase.PRUEBAS, Fase.desde("PRUEBAS"));
        Afirmar.lanza(IllegalArgumentException.class, () -> Fase.desde(null));
        Afirmar.igual(RolXP.ENCARGADO_PRUEBAS, RolXP.desde("encargado pruebas"));
        Afirmar.lanza(IllegalArgumentException.class, () -> RolXP.desde("jefe"));
    }

    @Prueba
    void elProyectoPuedeCambiarLaRutaDeCadaArtefacto() throws IOException {
        Files.writeString(raiz.resolve("xp.properties"), String.join("\n",
                "artefacto.UNIDAD_PRUEBAS=antdron/pruebas",
                "artefacto.IMPLANTACION=antdron/Dockerfile"));
        ContextoXP c = contexto();
        Afirmar.igual("antdron/pruebas", c.relativa(c.artefacto(Practica.UNIDAD_PRUEBAS).orElseThrow()));
        Afirmar.igual("xp/historias.csv", c.relativa(c.artefacto(Practica.HISTORIAS_USUARIO).orElseThrow()));
        Afirmar.verdadero(c.artefacto(Practica.RECICLAJE).isEmpty());
    }

    @Prueba
    void verificarDetectaArtefactosFaltantesVaciosYPresentes() throws IOException {
        Files.createDirectories(raiz.resolve("xp"));
        Files.writeString(raiz.resolve("xp/historias.csv"), "id;titulo\n");
        Files.writeString(raiz.resolve("xp/plan-entregas.md"), "");
        Files.createDirectories(raiz.resolve("pruebas"));

        List<ComandoVerificar.Resultado> r = ComandoVerificar.verificar(contexto(), List.of(Fase.values()));
        Afirmar.verdadero(resultado(r, Practica.HISTORIAS_USUARIO).cumple());
        Afirmar.falso(resultado(r, Practica.PLAN_ENTREGAS).cumple(), "un archivo vacío no cuenta");
        Afirmar.falso(resultado(r, Practica.UNIDAD_PRUEBAS).cumple(), "un directorio vacío no cuenta");
        Afirmar.verdadero(resultado(r, Practica.RECICLAJE).cumple());
        Afirmar.falso(resultado(r, Practica.RECICLAJE).verificable());

        Files.writeString(raiz.resolve("pruebas/AlgoTest.java"), "class AlgoTest {}");
        r = ComandoVerificar.verificar(contexto(), List.of(Fase.DESARROLLO));
        Afirmar.verdadero(resultado(r, Practica.UNIDAD_PRUEBAS).cumple());
        Afirmar.igual(4, r.size());
    }

    private static ComandoVerificar.Resultado resultado(List<ComandoVerificar.Resultado> r, Practica p) {
        return r.stream().filter(x -> x.practica() == p).findFirst().orElseThrow();
    }

    @Prueba
    void comandoVerificarMuestraElCumplimientoYFallaSiFaltaAlgo() throws IOException {
        Afirmar.igual(1, new ComandoVerificar().ejecutar(List.of(), contexto()));
        Afirmar.verdadero(out().contains("[FALTA] Historias de usuario → xp/historias.csv  (responsable: Cliente)"));
        Afirmar.verdadero(out().contains("[•] Reciclaje (refactorización) (directriz sin artefacto)"));
        Afirmar.verdadero(out().contains("Cumplimiento de directrices: 0/15"));

        Files.writeString(raiz.resolve("xp.properties"), "artefacto.IMPLANTACION=Dockerfile\n");
        Files.writeString(raiz.resolve("Dockerfile"), "FROM scratch\n");
        salida.reset();
        Afirmar.igual(1, new ComandoVerificar().ejecutar(List.of("pruebas"), contexto()),
                "aún falta el reporte de aceptación");
        Afirmar.verdadero(out().contains("[OK] Implantación → Dockerfile"));
        Afirmar.verdadero(out().contains("Cumplimiento de directrices: 1/2"));
    }

    @Prueba
    void verificarTerminaEn0CuandoTodoExiste() throws IOException {
        for (Practica p : Practica.values()) {
            if (p.getArtefactoPorDefecto().isPresent()) {
                Path ruta = raiz.resolve(p.getArtefactoPorDefecto().get());
                if (p == Practica.UNIDAD_PRUEBAS) {
                    ruta = ruta.resolve("UnaPruebaTest.java");
                }
                Files.createDirectories(ruta.getParent());
                Files.writeString(ruta, "contenido");
            }
        }
        Afirmar.igual(0, new ComandoVerificar().ejecutar(List.of(), contexto()));
        Afirmar.verdadero(out().contains("Cumplimiento de directrices: 15/15"));
    }

    @Prueba
    void comandoDirectricesMuestraReglasPorFase() throws IOException {
        Afirmar.igual(0, new ComandoDirectrices().ejecutar(List.of("planificacion"), contexto()));
        String s = out();
        Afirmar.verdadero(s.contains("== I. Planificación =="));
        Afirmar.verdadero(s.contains("• Velocidad del proyecto  [Encargado de seguimiento (Tracker)]"));
        Afirmar.verdadero(s.contains("Artefacto: xp/historias.csv"));
        Afirmar.falso(s.contains("Diseño"));
        salida.reset();
        new ComandoDirectrices().ejecutar(List.of(), contexto());
        Afirmar.verdadero(out().contains("== IV. Pruebas =="));
        Afirmar.verdadero(out().contains("(directriz de conducta, sin artefacto)"));
        Afirmar.lanza(IllegalArgumentException.class,
                () -> new ComandoDirectrices().ejecutar(List.of("a", "b"), contexto()));
    }
}
