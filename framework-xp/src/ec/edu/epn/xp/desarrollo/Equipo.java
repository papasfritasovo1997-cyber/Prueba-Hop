package ec.edu.epn.xp.desarrollo;

import java.util.List;
import java.util.Optional;

/** Equipo XP: número de grupo, nombre, metáfora del sistema e integrantes. */
public final class Equipo {

    private final int numero;
    private final String nombre;
    private final String metafora;
    private final List<Integrante> integrantes;

    public Equipo(int numero, String nombre, String metafora, List<Integrante> integrantes) {
        if (numero <= 0 || numero > 99) {
            throw new IllegalArgumentException("El número de grupo debe estar entre 1 y 99");
        }
        if (integrantes == null || integrantes.isEmpty()) {
            throw new IllegalArgumentException("El equipo debe tener al menos un integrante");
        }
        long usuariosUnicos = integrantes.stream().map(Integrante::getUsuario).distinct().count();
        if (usuariosUnicos != integrantes.size()) {
            throw new IllegalArgumentException("Hay usuarios repetidos en el equipo");
        }
        this.numero = numero;
        this.nombre = nombre == null || nombre.isBlank() ? "Grupo " + numero : nombre.trim();
        this.metafora = metafora == null ? "" : metafora.trim();
        this.integrantes = List.copyOf(integrantes);
    }

    /** Número con dos dígitos, p. ej. 1 → "01" (para "Grupo01.csv"). */
    public String numeroConFormato() {
        return String.format("%02d", numero);
    }

    public Optional<Integrante> buscarPorUsuario(String usuario) {
        return integrantes.stream().filter(i -> i.getUsuario().equals(usuario)).findFirst();
    }

    public int getNumero() { return numero; }
    public String getNombre() { return nombre; }
    public String getMetafora() { return metafora; }
    public List<Integrante> getIntegrantes() { return integrantes; }
}
