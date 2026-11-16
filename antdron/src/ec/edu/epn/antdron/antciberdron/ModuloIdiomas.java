package ec.edu.epn.antdron.antciberdron;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Conocimiento de inglés y español: léxico, gramática y fonética, entrenados solo por expertos parlantes. */
public final class ModuloIdiomas {

    private final Map<Idioma, Map<AspectoLinguistico, List<String>>> conocimiento = new EnumMap<>(Idioma.class);

    public ModuloIdiomas() {
        for (Idioma idioma : Idioma.values()) {
            Map<AspectoLinguistico, List<String>> aspectos = new EnumMap<>(AspectoLinguistico.class);
            for (AspectoLinguistico a : AspectoLinguistico.values()) {
                aspectos.put(a, new ArrayList<>());
            }
            conocimiento.put(idioma, aspectos);
        }
    }

    public void entrenar(ExpertoParlante experto, Idioma idioma, AspectoLinguistico aspecto, String contenido) {
        if (experto == null) {
            throw new EntrenamientoNoAutorizadoException("Solo un experto parlante puede entrenar idiomas");
        }
        if (experto.getIdiomaNativo() != idioma) {
            throw new EntrenamientoNoAutorizadoException(experto.getNombre() + " es experto en "
                    + experto.getIdiomaNativo().getEtiqueta() + " y no puede entrenar " + idioma.getEtiqueta());
        }
        if (aspecto == null || contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("Indique el aspecto y el contenido a entrenar");
        }
        conocimiento.get(idioma).get(aspecto).add(contenido.trim());
    }

    public List<String> conocimiento(Idioma idioma, AspectoLinguistico aspecto) {
        return Collections.unmodifiableList(conocimiento.get(idioma).get(aspecto));
    }

    /** Porcentaje de aspectos (léxico, gramática, fonética) ya entrenados en el idioma. */
    public int dominio(Idioma idioma) {
        long entrenados = conocimiento.get(idioma).values().stream().filter(l -> !l.isEmpty()).count();
        return (int) (entrenados * 100 / AspectoLinguistico.values().length);
    }

    public boolean puedeHablar(Idioma idioma) {
        return dominio(idioma) == 100;
    }
}
