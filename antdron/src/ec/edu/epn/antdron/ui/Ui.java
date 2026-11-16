package ec.edu.epn.antdron.ui;

import java.util.List;
import java.util.function.Function;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** Utilidades de las pantallas: tarjetas, títulos, columnas de tablas y mensajes. */
final class Ui {

    private Ui() {
    }

    static String estilos() {
        return Ui.class.getResource("/ec/edu/epn/antdron/ui/antdron.css").toExternalForm();
    }

    static Label titulo(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("titulo");
        return l;
    }

    static Label subtitulo(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("subtitulo");
        return l;
    }

    static Label texto(String texto) {
        Label l = new Label(texto);
        l.setWrapText(true);
        l.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        return l;
    }

    static VBox tarjeta(Node... hijos) {
        VBox v = new VBox(10, hijos);
        v.getStyleClass().add("tarjeta");
        v.setPadding(new Insets(16));
        return v;
    }

    static HBox fila(List<? extends Node> hijos) {
        HBox h = new HBox(10);
        h.getChildren().addAll(hijos);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    /** Etiqueta de aviso (amarilla) o error (roja); vacía la oculta. */
    static Label aviso(String texto, boolean error) {
        Label l = new Label(texto);
        l.setWrapText(true);
        l.setMaxWidth(Double.MAX_VALUE);
        l.getStyleClass().add(error ? "aviso-error" : "aviso");
        return l;
    }

    static void mensaje(Label etiqueta, String texto, boolean error) {
        etiqueta.setText(texto);
        etiqueta.getStyleClass().removeAll("aviso", "aviso-error", "aviso-ok");
        etiqueta.getStyleClass().add(error ? "aviso-error" : "aviso-ok");
        etiqueta.setVisible(texto != null && !texto.isEmpty());
        etiqueta.setManaged(etiqueta.isVisible());
    }

    static TextArea monoespaciado(String contenido) {
        TextArea t = new TextArea(contenido);
        t.setEditable(false);
        t.getStyleClass().add("mono");
        t.setWrapText(false);
        VBox.setVgrow(t, Priority.ALWAYS);
        return t;
    }

    static <T> TableColumn<T, String> columna(String titulo, Function<T, String> valor, double ancho) {
        TableColumn<T, String> c = new TableColumn<>(titulo);
        c.setCellValueFactory(d -> new ReadOnlyStringWrapper(valor.apply(d.getValue())));
        c.setPrefWidth(ancho);
        c.setSortable(false);
        return c;
    }

    static <T> TableView<T> tabla(String sinDatos) {
        TableView<T> t = new TableView<>();
        t.setPlaceholder(new Label(sinDatos));
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(t, Priority.ALWAYS);
        return t;
    }

    /** Muestra los elementos del combo con un texto legible en lugar de su nombre interno. */
    static <T> void mostrarCon(ComboBox<T> combo, Function<T, String> texto) {
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(T valor) {
                return valor == null ? "" : texto.apply(valor);
            }

            @Override
            public T fromString(String s) {
                return null;
            }
        });
    }

    static void error(String titulo, String detalle) {
        Alert a = new Alert(Alert.AlertType.ERROR, detalle);
        a.setHeaderText(titulo);
        a.setTitle(AntdronFx.TITULO);
        a.show();
    }
}
