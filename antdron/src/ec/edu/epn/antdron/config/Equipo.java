package ec.edu.epn.antdron.config;

import java.util.List;
import java.util.Optional;

/** Equipo configurado en {@code config/}: número de grupo, nombre, metáfora e integrantes. */
public record Equipo(int grupo, String nombre, String metafora, List<Integrante> integrantes) {

    public Equipo {
        if (grupo < 1 || grupo > 99) {
            throw new IllegalArgumentException("El número de grupo debe estar entre 1 y 99 (config/equipo.properties)");
        }
        nombre = nombre == null || nombre.isBlank() ? "Grupo " + grupo : nombre.trim();
        metafora = metafora == null ? "" : metafora.trim();
        integrantes = List.copyOf(integrantes);
        if (integrantes.isEmpty()) {
            throw new IllegalArgumentException("config/integrantes.csv no tiene integrantes");
        }
        long usuarios = integrantes.stream().map(Integrante::usuario).filter(u -> !u.isEmpty()).count();
        long distintos = integrantes.stream().map(Integrante::usuario).filter(u -> !u.isEmpty()).distinct().count();
        if (usuarios != distintos) {
            throw new IllegalArgumentException("Hay usuarios repetidos en config/integrantes.csv");
        }
    }

    /** "01" para el grupo 1: se usa en el nombre del archivo Grupo01.csv. */
    public String grupoConFormato() {
        return String.format("%02d", grupo);
    }

    public String archivoCoordenadas() {
        return "Grupo" + grupoConFormato() + ".csv";
    }

    /** Cédulas ya registradas, en el orden del archivo. */
    public List<String> cedulas() {
        return integrantes.stream().filter(Integrante::tieneCedula).map(Integrante::cedula).toList();
    }

    public int cedulasPendientes() {
        return (int) integrantes.stream().filter(i -> !i.tieneCedula()).count();
    }

    public Optional<Integrante> buscarUsuario(String usuario) {
        return integrantes.stream().filter(i -> i.usuario().equals(usuario)).findFirst();
    }
}
