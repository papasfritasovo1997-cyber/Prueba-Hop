package ec.edu.epn.antdron.ui;

import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import ec.edu.epn.antdron.MotorAntdron;
import ec.edu.epn.antdron.automata.Automata;
import ec.edu.epn.antdron.bomba.Bomba;

/**
 * Bomba basada en un autómata: coordenadas objetivo (último dígito de cada cédula), lenguaje L,
 * grafo, definición formal, gramática y una prueba de palabras.
 */
final class PanelAutomata {

    private final VBox raiz;

    PanelAutomata(MotorAntdron motor) {
        raiz = new VBox(12);
        raiz.setPadding(new Insets(18));
        raiz.getChildren().add(Ui.titulo("Bomba basada en un autómata"));
        if (motor.getBomba().isEmpty()) {
            raiz.getChildren().add(Ui.aviso("La bomba se construye con el último dígito de la cédula de cada "
                    + "integrante. Escriba las cédulas en config/integrantes.csv y pulse «Recargar configuración».",
                    false));
            return;
        }
        Bomba bomba = motor.getBomba().get();
        Automata a = bomba.getAutomata();

        Label objetivos = Ui.subtitulo("Coordenadas objetivo: " + String.join(", ", bomba.coordenadasObjetivo())
                + "     " + bomba.lenguaje());
        objetivos.setId("lenguaje");
        Label simbolos = Ui.texto("Símbolos: a = Avión · b = Barco · c = Convoy · d = Dron · t = Tanque. "
                + "La bomba explota solo si la coordenada es objetivo y el autómata acepta el tipo de arsenal.");
        simbolos.getStyleClass().add("tenue");

        ScrollPane grafo = new ScrollPane(new LienzoAutomata(a));
        grafo.getStyleClass().add("lienzo");
        grafo.setPrefViewportHeight(260);

        TextField palabra = new TextField();
        palabra.setPromptText("palabra w, p. ej. abcdttt");
        palabra.setId("palabra");
        Label resultado = new Label();
        resultado.setId("resultado-palabra");
        Button probar = new Button("¿w ∈ L?");
        probar.setOnAction(e -> {
            String w = palabra.getText().trim();
            boolean acepta = bomba.evaluar(w);
            Ui.mensaje(resultado, "w = \"" + w + "\" " + (acepta
                    ? "∈ L: la bomba explota si la coordenada es objetivo"
                    : "∉ L: la bomba NO explota"), !acepta);
        });
        resultado.setVisible(false);
        resultado.setManaged(false);
        palabra.setOnAction(e -> probar.fire());

        VBox formal = Ui.tarjeta(Ui.subtitulo("Definición formal"), Ui.monoespaciado(a.descripcionFormal()));
        VBox gramatica = Ui.tarjeta(Ui.subtitulo("Gramática regular"), Ui.monoespaciado(a.gramatica().toString()));
        HBox.setHgrow(formal, Priority.ALWAYS);
        HBox.setHgrow(gramatica, Priority.ALWAYS);
        HBox textos = new HBox(12, formal, gramatica);
        VBox.setVgrow(textos, Priority.ALWAYS);

        Label exportado = Ui.texto(motor.getArchivoAutomata()
                .map(p -> "Exportado (con Mermaid y Graphviz) en " + motor.getRaiz().relativize(p)).orElse(""));
        exportado.getStyleClass().add("tenue");

        raiz.getChildren().addAll(objetivos, simbolos, grafo,
                Ui.fila(List.of(new Label("Probar palabra:"), palabra, probar, resultado)), textos, exportado);
    }

    Parent getRaiz() {
        return raiz;
    }
}
