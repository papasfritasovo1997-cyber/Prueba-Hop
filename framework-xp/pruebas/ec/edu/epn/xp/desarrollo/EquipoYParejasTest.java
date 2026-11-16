package ec.edu.epn.xp.desarrollo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ec.edu.epn.xp.directrices.RolXP;
import ec.edu.epn.xp.pruebas.Afirmar;
import ec.edu.epn.xp.pruebas.Historia;
import ec.edu.epn.xp.pruebas.Prueba;
import ec.edu.epn.xp.pruebas.Temporal;

@Historia("XP-06")
class EquipoYParejasTest {

    Path dir = Temporal.directorio();

    @Prueba
    void leeEquipoConCedulasVaciasOCompletas() throws IOException {
        Files.writeString(dir.resolve("equipo.properties"), "grupo=3\nnombre=Transformers\nmetafora=Hormiguero\n");
        Files.writeString(dir.resolve("integrantes.csv"), String.join("\n",
                "cedula;nombre;usuario;clave;rol",
                "11036359;Pepito Alimaña;pepito;1;ENTRENADOR",
                ";Juana Galarza;juana;2;cliente"));
        Equipo e = new LectorEquipo().leer(dir);
        Afirmar.igual("03", e.numeroConFormato());
        Afirmar.igual("Transformers", e.getNombre());
        Afirmar.igual(2, e.getIntegrantes().size());
        Afirmar.verdadero(e.getIntegrantes().get(0).tieneCedula());
        Afirmar.falso(e.getIntegrantes().get(1).tieneCedula());
        Afirmar.igual(RolXP.CLIENTE, e.buscarPorUsuario("juana").orElseThrow().getRol());
        Afirmar.contiene(e.getIntegrantes().get(1).toString(), "(cédula pendiente)");
    }

    @Prueba
    void validaCedulaRolYFaltaDeArchivos() {
        LectorEquipo lector = new LectorEquipo();
        Afirmar.contiene(Afirmar.lanza(IllegalArgumentException.class,
                () -> lector.parsearIntegrantes(List.of("12AB;X;x;h;CLIENTE"))).getMessage(), "línea 1");
        Afirmar.lanza(IllegalArgumentException.class, () -> lector.parsearIntegrantes(List.of(";X;x;h;JEFE")));
        Afirmar.lanza(IllegalArgumentException.class, () -> lector.parsearIntegrantes(List.of(";X;x;CLIENTE")));
        Afirmar.lanza(IOException.class, () -> lector.leer(dir));
    }

    @Prueba
    void cadaIntegranteTrabajaUnaVezConCadaCompanero() {
        RotacionParejas r = new RotacionParejas(List.of("A", "B", "C", "D", "E", "F"));
        Set<Set<String>> vistas = new HashSet<>();
        for (int ronda = 1; ronda <= r.totalRondas(); ronda++) {
            List<List<String>> parejas = r.parejas(ronda);
            Afirmar.igual(3, parejas.size());
            Set<String> enRonda = new HashSet<>();
            parejas.forEach(enRonda::addAll);
            Afirmar.igual(6, enRonda.size(), "nadie repite en la misma ronda");
            parejas.forEach(p -> Afirmar.verdadero(vistas.add(Set.copyOf(p)), "pareja repetida " + p));
        }
        Afirmar.igual(15, vistas.size());
        Afirmar.igual(r.parejas(1), r.parejas(6));
    }

    @Prueba
    void rotacionRequiereNumeroPar() {
        Afirmar.lanza(IllegalArgumentException.class, () -> new RotacionParejas(List.of("A", "B", "C")));
    }
}
