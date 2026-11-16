package ec.edu.epn.antdron.ui;

import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import ec.edu.epn.antdron.MotorAntdron;
import ec.edu.epn.antdron.sistema.CoordenadaUK;
import ec.edu.epn.antdron.sistema.DiaSemana;

/**
 * "[+] COORDENADAS UCRANIANAS": lee Grupo##.csv y, por cada línea, muestra un loading \|/- hasta 100%
 * antes de presentar la fila.
 */
final class PanelCoordenadas {

    private static final char[] CUADROS = {'\\', '|', '/', '-'};
    private static final Duration PASO = Duration.millis(45);

    private final VBox raiz;
    private final ObservableList<Fila> filas = FXCollections.observableArrayList();
    private Timeline animacion;

    /** Fila con el texto de loading que cambia durante la animación. */
    static final class Fila {
        final CoordenadaUK coordenada;
        final StringProperty loading = new SimpleStringProperty("\\ 0%");

        Fila(CoordenadaUK coordenada) {
            this.coordenada = coordenada;
        }
    }

    PanelCoordenadas(MotorAntdron motor) {
        List<CoordenadaUK> coordenadas = motor.getCoordenadas();
        TableView<Fila> tabla = Ui.tabla("Sin coordenadas: registre las cédulas en config/integrantes.csv");
        TableColumn<Fila, String> loading = new TableColumn<>("Loading");
        loading.setCellValueFactory(d -> d.getValue().loading);
        loading.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean vacio) {
                super.updateItem(item, vacio);
                setText(vacio ? null : item);
                if (!getStyleClass().contains("loading")) {
                    getStyleClass().add("loading");
                }
            }
        });
        loading.setPrefWidth(90);
        loading.setSortable(false);
        tabla.getColumns().add(loading);
        tabla.getColumns().add(Ui.columna("Geoposición", f -> f.coordenada.getGeoposicion(), 110));
        for (DiaSemana dia : DiaSemana.values()) {
            tabla.getColumns().add(Ui.columna(dia.getEtiqueta(), f -> f.coordenada.getHorario(dia), 90));
        }
        tabla.getColumns().add(Ui.columna("Tipo Arsenal", f -> f.coordenada.getTipoArsenal(), 160));
        tabla.setItems(filas);
        tabla.setId("tabla-coordenadas");

        Label estado = new Label();
        estado.getStyleClass().add("tenue");
        Button cargar = new Button("Cargar de nuevo");
        cargar.setId("cargar-coordenadas");
        cargar.setDisable(coordenadas.isEmpty());
        cargar.setOnAction(e -> animar(coordenadas, estado));

        Label archivo = Ui.texto(motor.getArchivoCoordenadas()
                .map(p -> "Archivo: " + motor.getRaiz().relativize(p) + " (se genera a partir de las cédulas)")
                .orElse("Aún no hay archivo de coordenadas: faltan cédulas en config/integrantes.csv."));
        archivo.getStyleClass().add("tenue");

        raiz = new VBox(12, Ui.titulo("[+] COORDENADAS UCRANIANAS"), archivo, Ui.fila(List.of(cargar, estado)), tabla);
        raiz.setPadding(new Insets(18));
        estadoInicial = estado;
        listaInicial = coordenadas;
    }

    private final Label estadoInicial;
    private final List<CoordenadaUK> listaInicial;
    private boolean mostrado;

    /** La lectura con loading empieza la primera vez que se abre la pestaña. */
    void alMostrar() {
        if (!mostrado) {
            mostrado = true;
            animar(listaInicial, estadoInicial);
        }
    }

    private void animar(List<CoordenadaUK> coordenadas, Label estado) {
        if (animacion != null) {
            animacion.stop();
        }
        filas.clear();
        if (coordenadas.isEmpty()) {
            estado.setText("");
            return;
        }
        int[] fila = {0};
        int[] porcentaje = {0};
        animacion = new Timeline(new KeyFrame(PASO, e -> {
            if (fila[0] >= coordenadas.size()) {
                animacion.stop();
                estado.setText("Total: " + coordenadas.size() + " coordenadas");
                return;
            }
            if (porcentaje[0] == 0) {
                filas.add(new Fila(coordenadas.get(fila[0])));
            }
            Fila actual = filas.get(filas.size() - 1);
            if (porcentaje[0] < 100) {
                actual.loading.set(CUADROS[(porcentaje[0] / 10) % CUADROS.length] + " " + porcentaje[0] + "%");
                porcentaje[0] += 10;
            } else {
                actual.loading.set("100%");
                porcentaje[0] = 0;
                fila[0]++;
            }
            estado.setText("Leyendo línea " + Math.min(fila[0] + 1, coordenadas.size()) + " de " + coordenadas.size());
        }));
        animacion.setCycleCount(Timeline.INDEFINITE);
        animacion.play();
    }

    Parent getRaiz() {
        return raiz;
    }
}
