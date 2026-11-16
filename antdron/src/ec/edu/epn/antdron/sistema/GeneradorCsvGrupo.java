package ec.edu.epn.antdron.sistema;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import ec.edu.epn.antdron.config.Equipo;

/**
 * Genera "Grupo##.csv": una línea por cada dígito de la cédula de cada integrante, con el horario de la
 * estrategia OTAN y, en la última columna, una palabra w que pertenece al lenguaje de esa coordenada.
 * Es determinista (la semilla es la cédula): las mismas cédulas producen siempre el mismo archivo.
 */
public final class GeneradorCsvGrupo {

    public static final String CABECERA =
            "Geoposición ; Lunes ; Martes ; Miércoles ; Jueves ; Viernes ; Tipo Arsenal";

    public String generar(List<String> cedulas) {
        StringBuilder sb = new StringBuilder(CABECERA).append('\n');
        for (String cedula : cedulas) {
            if (!cedula.matches("\\d{6,13}")) {
                throw new IllegalArgumentException("Cédula inválida: " + cedula);
            }
            Random aleatorio = new Random(Long.parseLong(cedula));
            for (char c : cedula.toCharArray()) {
                sb.append(linea(EstrategiaOTAN.de(c - '0'), aleatorio)).append('\n');
            }
        }
        return sb.toString();
    }

    /** Escribe datos/Grupo##.csv con las cédulas registradas. Devuelve la ruta del archivo. */
    public Path escribir(Equipo equipo, Path directorio) throws IOException {
        if (equipo.cedulas().isEmpty()) {
            throw new IllegalStateException("No hay cédulas en config/integrantes.csv para generar "
                    + equipo.archivoCoordenadas());
        }
        Files.createDirectories(directorio);
        Path archivo = directorio.resolve(equipo.archivoCoordenadas());
        Files.writeString(archivo, generar(equipo.cedulas()), StandardCharsets.UTF_8);
        return archivo;
    }

    private static String linea(EstrategiaOTAN.Entrada e, Random aleatorio) {
        StringBuilder sb = new StringBuilder(e.geoposicion()).append(';');
        for (DiaSemana dia : DiaSemana.values()) {
            sb.append(String.format(" %-6s;", dia == e.dia() ? e.horario() : ""));
        }
        return sb.append(' ').append(palabra(e.expresion(), aleatorio)).toString();
    }

    /** Palabra del lenguaje: x* se repite 0..3 veces, x+ 1..4 veces y x sin operador una vez. */
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
