package ec.edu.epn.antdron.sistema;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Una línea del archivo Grupo##.csv: geoposición, horarios por día y tipo de arsenal (palabra w). */
public final class CoordenadaUK {

    private final String geoposicion;
    private final int digito;
    private final Map<DiaSemana, String> horarios;
    private final String tipoArsenal;

    public CoordenadaUK(String geoposicion, Map<DiaSemana, String> horarios, String tipoArsenal) {
        this.geoposicion = Objects.requireNonNull(geoposicion).trim();
        if (!this.geoposicion.matches("Coord-\\d{2}")) {
            throw new IllegalArgumentException("Geoposición inválida: '" + geoposicion + "' (formato Coord-0N)");
        }
        this.digito = Integer.parseInt(this.geoposicion.substring(6)) % 10;
        EnumMap<DiaSemana, String> copia = new EnumMap<>(DiaSemana.class);
        copia.putAll(horarios);
        this.horarios = Collections.unmodifiableMap(copia);
        this.tipoArsenal = Objects.requireNonNull(tipoArsenal).trim();
    }

    public String getGeoposicion() { return geoposicion; }
    /** Dígito de cédula al que corresponde la coordenada (Coord-05 → 5). */
    public int getDigito() { return digito; }
    public String getTipoArsenal() { return tipoArsenal; }

    public String getHorario(DiaSemana dia) {
        return horarios.getOrDefault(dia, "");
    }

    @Override
    public String toString() {
        return geoposicion + " " + horarios + " " + tipoArsenal;
    }
}
