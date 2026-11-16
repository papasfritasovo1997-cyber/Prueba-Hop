package ec.edu.epn.xp.desarrollo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rotación de parejas (programación en parejas) con el método del círculo:
 * con n integrantes, en n-1 rondas cada integrante trabaja exactamente una vez con cada compañero.
 */
public final class RotacionParejas {

    private final List<String> integrantes;

    public RotacionParejas(List<String> integrantes) {
        if (integrantes == null || integrantes.size() < 2 || integrantes.size() % 2 != 0) {
            throw new IllegalArgumentException("Se necesita un número par (>= 2) de integrantes para formar parejas");
        }
        if (integrantes.stream().distinct().count() != integrantes.size()) {
            throw new IllegalArgumentException("Hay integrantes repetidos");
        }
        this.integrantes = List.copyOf(integrantes);
    }

    public int totalRondas() {
        return integrantes.size() - 1;
    }

    /** Parejas de la ronda indicada (1..n-1); rondas mayores vuelven a empezar el ciclo. */
    public List<List<String>> parejas(int ronda) {
        if (ronda < 1) {
            throw new IllegalArgumentException("La ronda debe ser >= 1");
        }
        int n = integrantes.size();
        List<String> giratorios = new ArrayList<>(integrantes.subList(1, n));
        Collections.rotate(giratorios, (ronda - 1) % (n - 1));
        List<String> orden = new ArrayList<>();
        orden.add(integrantes.get(0));
        orden.addAll(giratorios);

        List<List<String>> parejas = new ArrayList<>();
        for (int i = 0; i < n / 2; i++) {
            parejas.add(List.of(orden.get(i), orden.get(n - 1 - i)));
        }
        return parejas;
    }
}
