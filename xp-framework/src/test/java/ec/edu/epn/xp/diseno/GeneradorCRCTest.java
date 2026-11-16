package ec.edu.epn.xp.diseno;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ec.edu.epn.xp.pruebas.Historia;

@Historia("XP-04")
class GeneradorCRCTest {

    @TarjetaCRC(responsabilidades = {"Volar", "Disparar"}, colaboradores = {"FuentePoder"})
    static class Dron {
    }

    static class SinTarjeta {
    }

    @Test
    void generaTarjetaEnMarkdown() {
        String md = GeneradorCRC.tarjeta(Dron.class);
        assertTrue(md.contains("### Clase: Dron"));
        assertTrue(md.contains("| Volar | FuentePoder |"));
        assertTrue(md.contains("| Disparar |  |"));
    }

    @Test
    void omiteClasesSinTarjeta() {
        String md = GeneradorCRC.markdown(Dron.class, SinTarjeta.class);
        assertTrue(md.contains("Dron"));
        assertFalse(md.contains("SinTarjeta"));
        assertThrows(IllegalArgumentException.class, () -> GeneradorCRC.tarjeta(SinTarjeta.class));
    }
}
