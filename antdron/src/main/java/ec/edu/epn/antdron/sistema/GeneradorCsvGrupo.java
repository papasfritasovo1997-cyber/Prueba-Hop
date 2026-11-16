package ec.edu.epn.antdron.sistema;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import ec.edu.epn.xp.desarrollo.Equipo;
import ec.edu.epn.xp.desarrollo.Integrante;

/**
 * Genera "Grupo##.csv": una línea por cada dígito de la cédula de cada integrante, con el horario
 * de la estrategia OTAN y una palabra w que pertenece al lenguaje de esa coordenada.
 */
public final class GeneradorCsvGrupo {

    public static final String CABECERA =
            "Geoposición ; Lunes ; Martes ; Miércoles ; Jueves ; Viernes ; Tipo Arsenal";

    public static String nombreArchivo(Equipo equipo) {
        return "Grupo" + equipo.numeroConFormato() + ".csv";
    }

    public String generar(Equipo equipo) {
        StringBuilder sb = new StringBuilder(CABECERA).append('\n');
        for (Integrante integrante : equipo.getIntegrantes()) {
            // Semilla fija por cédula: el archivo es reproducible.
            Random aleatorio = new Random(Long.parseLong(integrante.getCedula()));
            for (char c : integrante.getCedula().toCharArray()) {
                sb.append(linea(EstrategiaOTAN.de(c - '0'), aleatorio)).append('\n');
            }
        }
        return sb.toString();
    }

    public Path escribir(Equipo equipo, Path directorio) throws IOException {
        Files.createDirectories(directorio);
        Path archivo = directorio.resolve(nombreArchivo(equipo));
        Files.writeString(archivo, generar(equipo), StandardCharsets.UTF_8);
        return archivo;
    }

    private static String linea(EstrategiaOTAN.Entrada e, Random aleatorio) {
        StringBuilder sb = new StringBuilder(e.geoposicion()).append(';');
        for (DiaSemana dia : DiaSemana.values()) {
            sb.append(String.format(" %-6s;", dia == e.dia() ? e.horario() : ""));
        }
        return sb.append(' ').append(palabra(e.expresion(), aleatorio)).toString();
    }

    /** Palabra del lenguaje: x* se repite 0..3 veces, x+ 1..4 veces y x sin operador 1 vez. */
    static String palabra(String expresion, Random aleatorio) {
        StringBuilder w = new StringBuilder();
        for (int i = 0; i < expresion.length(); i++) {
            char simbolo = expresion.charAt(i);
            char operador = i + 1 < expresion.length() ? expresion.charAt(i + 1) : ' ';
            int veces = 1;
            if (operador == '*') {
                veces = aleatorio.nextInt(4);
                i++;
            } else if (operador == '+') {
                veces = 1 + aleatorio.nextInt(4);
                i++;
            }
            w.append(String.valueOf(simbolo).repeat(veces));
        }
        return w.toString();
    }
}
