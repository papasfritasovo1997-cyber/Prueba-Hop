package ec.edu.epn.antdron.ui;

import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import ec.edu.epn.antdron.MotorAntdron;

/**
 * Aplicación JavaFX: ingreso (equipo/profesor o usuario KGD) → ventana principal o hormiguero virtual.
 * Cualquier error de configuración se muestra en pantalla con la opción de reintentar.
 */
public final class AntdronFx extends Application {

    static final String TITULO = "ANTDRON · KGD · AntCiberDron";

    private Stage ventana;
    private MotorAntdron motor;

    public static void iniciar(String[] args) {
        launch(AntdronFx.class, args);
    }

    @Override
    public void start(Stage stage) {
        this.ventana = stage;
        stage.setTitle(TITULO);
        stage.setMinWidth(980);
        stage.setMinHeight(680);
        Thread.currentThread().setUncaughtExceptionHandler((h, e) ->
                Ui.error("Ocurrió un error inesperado", e.getMessage() == null ? e.toString() : e.getMessage()));
        mostrarIngreso();
        stage.show();
    }

    /** Lee de nuevo config/ y vuelve a generar el CSV, la bomba y el autómata. */
    boolean recargar() {
        try {
            motor = MotorAntdron.cargar(MotorAntdron.raizPorDefecto());
            return true;
        } catch (Exception e) {
            mostrar(pantallaError(e.getMessage() == null ? e.toString() : e.getMessage()));
            return false;
        }
    }

    void mostrarIngreso() {
        if (recargar()) {
            mostrar(new VistaIngreso(this, motor).getRaiz());
        }
    }

    void mostrarPrincipal(String usuario, String nombre) {
        mostrar(new VistaPrincipal(this, motor, usuario, nombre).getRaiz());
    }

    void mostrarHormigueroKgd() {
        mostrar(new VistaKgd(motor, this::mostrarIngreso).getRaiz());
    }

    MotorAntdron getMotor() {
        return motor;
    }

    void mostrar(Parent raiz) {
        if (ventana.getScene() == null) {
            Scene escena = new Scene(raiz, 1180, 760);
            escena.getStylesheets().add(Ui.estilos());
            ventana.setScene(escena);
        } else {
            ventana.getScene().setRoot(raiz);
        }
    }

    private Parent pantallaError(String mensaje) {
        Label titulo = new Label("No se pudo cargar la configuración del equipo");
        titulo.getStyleClass().add("titulo");
        Label detalle = new Label(mensaje);
        detalle.setWrapText(true);
        detalle.getStyleClass().add("aviso-error");
        Label ayuda = new Label("Revise config/equipo.properties y config/integrantes.csv y pulse «Reintentar».");
        Button reintentar = new Button("Reintentar");
        reintentar.getStyleClass().add("primario");
        reintentar.setOnAction(e -> mostrarIngreso());
        Button salir = new Button("Salir");
        salir.setOnAction(e -> Platform.exit());
        VBox caja = new VBox(16, titulo, detalle, ayuda, Ui.fila(List.of(reintentar, salir)));
        caja.setPadding(new Insets(40));
        caja.setAlignment(Pos.CENTER_LEFT);
        caja.setMaxWidth(760);
        VBox raiz = new VBox(caja);
        raiz.setAlignment(Pos.CENTER);
        raiz.getStyleClass().add("fondo");
        return raiz;
    }
}
