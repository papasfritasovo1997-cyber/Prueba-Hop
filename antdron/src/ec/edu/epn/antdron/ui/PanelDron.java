package ec.edu.epn.antdron.ui;

import java.util.List;
import java.util.function.Supplier;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import ec.edu.epn.antdron.MotorAntdron;
import ec.edu.epn.antdron.antciberdron.AntCiberDron;
import ec.edu.epn.antdron.antciberdron.AspectoLinguistico;
import ec.edu.epn.antdron.antciberdron.ExpertoParlante;
import ec.edu.epn.antdron.antciberdron.FuentePoder;
import ec.edu.epn.antdron.antciberdron.Idioma;
import ec.edu.epn.antdron.sistema.CoordenadaUK;

/**
 * AntCiberDron: piloto (la HLarva que se transforma), IA que busca el tipo de arsenal, armas en las patas
 * delanteras, vuelo con el turbo reactor, fuente de poder asistida por la IIA e idiomas.
 */
final class PanelDron {

    private final VBox raiz = new VBox(12);
    private final ObservableList<String> bitacora = FXCollections.observableArrayList();
    private AntCiberDron dron;
    private ProgressBar energia;
    private Label energiaTexto;
    private Label dominio;

    /** Opción del combo de expertos. */
    private record Opcion(String etiqueta, ExpertoParlante experto) {
        @Override
        public String toString() {
            return etiqueta;
        }
    }

    PanelDron(MotorAntdron motor) {
        raiz.setPadding(new Insets(18));
        raiz.getChildren().add(Ui.titulo("AntCiberDron"));
        if (motor.getTipoHormiga().isEmpty()) {
            motor.getAvisos().forEach(a -> raiz.getChildren().add(Ui.aviso(a, false)));
            return;
        }
        MotorAntdron.Piloto piloto = motor.prepararPiloto();
        dron = motor.crearAntCiberDron(piloto.hormiga());

        VBox pilotoCaja = Ui.tarjeta(Ui.subtitulo("Hormiga piloto: " + motor.getTipoHormiga().get().getNombre()),
                Ui.texto(String.join("\n", piloto.pasos())),
                Ui.texto("Extremidades del exoesqueleto: " + dron.getExtremidades().size()
                        + " (las mismas de la hormiga). Turbo reactor en la " + dron.getUbicacionTurboReactor() + "."));

        VBox columnaIzq = new VBox(12, pilotoCaja, panelBusqueda(motor));
        VBox columnaDer = new VBox(12, panelArmas(), panelIdiomas());
        HBox.setHgrow(columnaIzq, Priority.ALWAYS);
        HBox.setHgrow(columnaDer, Priority.ALWAYS);
        columnaIzq.setPrefWidth(520);
        columnaDer.setPrefWidth(520);

        ListView<String> log = new ListView<>(bitacora);
        log.setPrefHeight(130);
        log.setId("bitacora");
        VBox contenido = new VBox(12, new HBox(12, columnaIzq, columnaDer), Ui.subtitulo("Bitácora"), log);
        ScrollPane desplazable = new ScrollPane(contenido);
        desplazable.setFitToWidth(true);
        VBox.setVgrow(desplazable, Priority.ALWAYS);
        raiz.getChildren().add(desplazable);
        actualizarEnergia();
    }

    private VBox panelBusqueda(MotorAntdron motor) {
        TableView<CoordenadaUK> tabla = Ui.tabla("Pulse «Buscar» para que la IA busque el arsenal");
        tabla.getColumns().add(Ui.columna("Geoposición", CoordenadaUK::getGeoposicion, 120));
        tabla.getColumns().add(Ui.columna("Tipo Arsenal", CoordenadaUK::getTipoArsenal, 150));
        tabla.getColumns().add(Ui.columna("Acción", c -> "true", 80));
        tabla.setPrefHeight(230);
        tabla.setId("tabla-destruir");
        ObservableList<CoordenadaUK> filas = FXCollections.observableArrayList();
        tabla.setItems(filas);
        dron.setPresentador(filas::add);

        TextField tipo = new TextField();
        tipo.setPromptText("tipo de arsenal, p. ej. abcdttt");
        tipo.setId("tipo-arsenal");
        Label resultado = new Label();
        resultado.setVisible(false);
        resultado.setManaged(false);
        Button buscar = new Button("Buscar");
        buscar.setOnAction(e -> {
            filas.clear();
            boolean explota = dron.buscar(tipo.getText());
            Ui.mensaje(resultado, "buscar(\"" + tipo.getText().trim() + "\") = " + explota, !explota);
            bitacora.add(0, "[IA] buscar(\"" + tipo.getText().trim() + "\") → " + explota);
        });
        Button todos = new Button("Buscar en todas las coordenadas");
        todos.getStyleClass().add("primario");
        todos.setId("buscar-todos");
        todos.setOnAction(e -> {
            filas.clear();
            int n = dron.buscarObjetivos().size();
            Ui.mensaje(resultado, "Objetivos destruidos: " + n, n == 0);
            bitacora.add(0, "[IA] Coordenadas ucranianas a destruir: " + n);
        });
        boolean sinBomba = motor.getBomba().isEmpty();
        buscar.setDisable(sinBomba);
        todos.setDisable(sinBomba);
        VBox caja = Ui.tarjeta(Ui.subtitulo("[+] COORDENADAS UCRANIANAS A DESTRUIR"),
                Ui.fila(List.of(tipo, buscar)), todos, resultado, tabla);
        if (sinBomba) {
            caja.getChildren().add(1, Ui.aviso("Sin cédulas no hay bomba: la IA nunca la hará explotar.", false));
        }
        return caja;
    }

    private VBox panelArmas() {
        energia = new ProgressBar(1);
        energia.setPrefWidth(200);
        energiaTexto = new Label();
        Spinner<Integer> minutos = new Spinner<>(1, 60, 10);
        minutos.setPrefWidth(80);
        minutos.setEditable(true);
        Spinner<Integer> capacidad = new Spinner<>(10, 1000, 150, 10);
        capacidad.setPrefWidth(100);
        capacidad.setEditable(true);

        Button metralleta = accion("Disparar metralleta (pata delantera derecha)", () -> dron.dispararMetralleta());
        Button laser = accion("Disparar láser (pata delantera izquierda)", () -> String.format(
                "Potencia según la energía: %.1f W · %s", dron.potenciaLaser(), dron.dispararLaser()));
        Button volar = accion("Volar", () -> dron.volar(minutos.getValue()));
        Button recargar = accion("Recargar fuente (IIA)", () -> dron.recargarFuente());
        Button reemplazar = accion("Reemplazar (IIA)",
                () -> dron.reemplazarFuente(new FuentePoder(capacidad.getValue())));

        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(8);
        g.addRow(0, new Label("Energía"), energia, energiaTexto);
        g.addRow(1, new Label("Minutos"), minutos, volar);
        g.addRow(2, new Label("Capacidad"), capacidad, reemplazar);
        return Ui.tarjeta(Ui.subtitulo("Armas, vuelo y energía"), metralleta, laser, g, recargar);
    }

    private VBox panelIdiomas() {
        ComboBox<Opcion> experto = new ComboBox<>(FXCollections.observableArrayList(
                new Opcion("John Smith · experto parlante de inglés", new ExpertoParlante("John Smith", Idioma.INGLES)),
                new Opcion("María Pérez · experta parlante de español",
                        new ExpertoParlante("María Pérez", Idioma.ESPANOL)),
                new Opcion("Usuario sin certificación (no autorizado)", null)));
        experto.getSelectionModel().selectFirst();
        ComboBox<Idioma> idioma = new ComboBox<>(FXCollections.observableArrayList(Idioma.values()));
        idioma.getSelectionModel().selectFirst();
        ComboBox<AspectoLinguistico> aspecto = new ComboBox<>(
                FXCollections.observableArrayList(AspectoLinguistico.values()));
        aspecto.getSelectionModel().selectFirst();
        Ui.mostrarCon(idioma, Idioma::getEtiqueta);
        Ui.mostrarCon(aspecto, AspectoLinguistico::getEtiqueta);
        TextField contenido = new TextField();
        contenido.setPromptText("contenido a enseñar");
        dominio = new Label();
        Button entrenar = accion("Entrenar", () -> {
            dron.aprender(experto.getValue().experto(), idioma.getValue(), aspecto.getValue(), contenido.getText());
            return "Entrenado " + aspecto.getValue().getEtiqueta() + " de " + idioma.getValue().getEtiqueta()
                    + " por " + experto.getValue().experto().getNombre();
        });
        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(8);
        g.addRow(0, new Label("Quién entrena"), experto);
        g.addRow(1, new Label("Idioma"), idioma);
        g.addRow(2, new Label("Aspecto"), aspecto);
        g.addRow(3, new Label("Contenido"), contenido);
        return Ui.tarjeta(Ui.subtitulo("Idiomas (solo expertos parlantes)"), g, entrenar, dominio);
    }

    /** Botón que ejecuta una acción del dron y registra el resultado o el error en la bitácora. */
    private Button accion(String texto, Supplier<String> accion) {
        Button b = new Button(texto);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> {
            try {
                bitacora.add(0, accion.get());
            } catch (RuntimeException ex) {
                bitacora.add(0, "✘ " + ex.getMessage());
            }
            actualizarEnergia();
        });
        return b;
    }

    private void actualizarEnergia() {
        FuentePoder f = dron.getFuente();
        energia.setProgress(f.porcentaje() / 100);
        energiaTexto.setText(String.format("%.0f / %.0f (%.0f%%)", f.getEnergia(), f.getCapacidad(), f.porcentaje()));
        dominio.setText("Dominio: Inglés " + dron.getIdiomas().dominio(Idioma.INGLES) + "% · Español "
                + dron.getIdiomas().dominio(Idioma.ESPANOL) + "%");
    }

    Parent getRaiz() {
        return raiz;
    }
}
