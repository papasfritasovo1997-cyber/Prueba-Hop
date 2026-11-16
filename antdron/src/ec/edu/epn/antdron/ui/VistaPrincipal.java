package ec.edu.epn.antdron.ui;

import java.util.List;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import ec.edu.epn.antdron.MotorAntdron;

/** Ventana principal tras el ingreso: una pestaña por cada requerimiento del enunciado. */
final class VistaPrincipal {

    private final BorderPane raiz = new BorderPane();

    VistaPrincipal(AntdronFx app, MotorAntdron motor, String usuario, String nombre) {
        Label marca = new Label(AntdronFx.TITULO);
        marca.getStyleClass().add("marca");
        Label quien = new Label("Sesión: " + nombre);
        Button recargar = new Button("Recargar configuración");
        recargar.setId("recargar");
        recargar.setOnAction(e -> {
            if (app.recargar()) {
                app.mostrarPrincipal(usuario, nombre);
            }
        });
        Button cerrar = new Button("Cerrar sesión");
        cerrar.setOnAction(e -> app.mostrarIngreso());
        Button salir = new Button("Salir");
        salir.setOnAction(e -> Platform.exit());
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox barra = new HBox(12, marca, espacio, quien, recargar, cerrar, salir);
        barra.getStyleClass().add("barra");
        barra.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        PanelCoordenadas coordenadas = new PanelCoordenadas(motor);
        Tab tabCoordenadas = pestana("Coordenadas ucranianas", coordenadas.getRaiz());
        tabCoordenadas.setOnSelectionChanged(e -> {
            if (tabCoordenadas.isSelected()) {
                coordenadas.alMostrar();
            }
        });
        TabPane pestanas = new TabPane(
                pestana("Equipo", new PanelEquipo(motor).getRaiz()),
                tabCoordenadas,
                pestana("Bomba y autómata", new PanelAutomata(motor).getRaiz()),
                pestana("AntCiberDron", new PanelDron(motor).getRaiz()),
                pestana("Hormiguero virtual", new VistaKgd(motor, null).getRaiz()));
        pestanas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        pestanas.setId("pestanas");

        raiz.setTop(barra);
        raiz.setCenter(pestanas);
        raiz.getStyleClass().add("fondo");
        if (!motor.getAvisos().isEmpty()) {
            HBox avisos = new HBox(8);
            avisos.setPadding(new Insets(8, 12, 8, 12));
            List.copyOf(motor.getAvisos()).forEach(a -> {
                Label l = Ui.aviso(a, false);
                HBox.setHgrow(l, Priority.ALWAYS);
                avisos.getChildren().add(l);
            });
            raiz.setBottom(avisos);
        }
    }

    private static Tab pestana(String titulo, Parent contenido) {
        Tab t = new Tab(titulo, contenido);
        t.setId("tab-" + titulo.toLowerCase().replace(' ', '-'));
        return t;
    }

    Parent getRaiz() {
        return raiz;
    }
}
