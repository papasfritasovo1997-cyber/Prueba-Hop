package ec.edu.epn.antdron.alimento;

import java.util.List;

import ec.edu.epn.antdron.pkAntCiberDronContract.Utility;

/** Crea alimentos a partir de su nombre (usado por las pantallas). */
public final class FabricaAlimentos {

    private FabricaAlimentos() {
    }

    public static List<Alimento> todos() {
        return List.of(new ANectar(), new ACarnivoro(), new AOmnivoro(), new AHerbivoro());
    }

    /** Acepta el nombre con o sin tildes y en cualquier capitalización: "nectar", "Carnívoro"... */
    public static Alimento desde(String nombre) {
        if (nombre == null) {
            throw new IllegalArgumentException("Alimento no indicado");
        }
        String clave = normalizar(nombre);
        return todos().stream()
                .filter(a -> normalizar(a.getNombre()).equals(clave))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Alimento desconocido: " + nombre));
    }

    public static String normalizar(String texto) {
        return Utility.normalizar(texto);
    }
}
