package ec.edu.epn.antdron.ui;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import ec.edu.epn.antdron.MotorAntdron;
import ec.edu.epn.antdron.auth.Autenticador;

/**
 * Pantalla inicial: autenticación de los integrantes y del profesor (máximo 3 intentos) y acceso
 * aparte para los usuarios de la KGD al hormiguero virtual.
 */
final class VistaIngreso {

    private final VBox raiz;

    VistaIngreso(AntdronFx app, MotorAntdron motor) {
        Autenticador autenticador = new Autenticador(motor.getEquipo());

        TextField usuario = new TextField();
        usuario.setPromptText("usuario");
        usuario.setId("usuario");
        PasswordField clave = new PasswordField();
        clave.setPromptText("contraseña");
        clave.setId("clave");
        Button ingresar = new Button("Ingresar");
        ingresar.getStyleClass().add("primario");
        ingresar.setDefaultButton(true);
        ingresar.setId("ingresar");
        Label intentos = new Label("Intentos disponibles: " + autenticador.intentosRestantes());
        intentos.getStyleClass().add("tenue");
        Label mensaje = Ui.aviso("", true);
        mensaje.setVisible(false);
        mensaje.setManaged(false);

        ingresar.setOnAction(e -> {
            switch (autenticador.intentar(usuario.getText(), clave.getText())) {
                case ACEPTADO -> app.mostrarPrincipal(usuario.getText().trim(),
                        autenticador.nombreDe(usuario.getText().trim()));
                case RECHAZADO -> {
                    Ui.mensaje(mensaje, "Usuario o contraseña incorrectos.", true);
                    intentos.setText("Intentos disponibles: " + autenticador.intentosRestantes());
                    clave.clear();
                    clave.requestFocus();
                }
                case BLOQUEADO -> {
                    Ui.mensaje(mensaje, "Acceso denegado: se superó el máximo de " + Autenticador.MAX_INTENTOS
                            + " intentos. Cierre y vuelva a abrir la aplicación.", true);
                    intentos.setText("Intentos disponibles: 0");
                    usuario.setDisable(true);
                    clave.setDisable(true);
                    ingresar.setDisable(true);
                }
                default -> throw new IllegalStateException();
            }
        });

        VBox equipo = Ui.tarjeta(
                Ui.subtitulo("Integrantes del grupo y profesor"),
                new Label("Usuario"), usuario, new Label("Contraseña"), clave,
                Ui.fila(List.of(ingresar, intentos)), mensaje);
        equipo.setPrefWidth(380);

        Button kgd = new Button("Entrar al hormiguero virtual");
        kgd.setId("kgd");
        kgd.setOnAction(e -> app.mostrarHormigueroKgd());
        VBox usuariosKgd = Ui.tarjeta(
                Ui.subtitulo("Usuarios de la KGD"),
                Ui.texto("Cree su cuenta, su hormiguero virtual y sus hormigas. Toda hormiga nace como HLarva."),
                kgd);
        usuariosKgd.setPrefWidth(380);

        HBox tarjetas = new HBox(24, equipo, usuariosKgd);
        tarjetas.setAlignment(Pos.TOP_CENTER);

        Label titulo = Ui.titulo(AntdronFx.TITULO);
        Label grupo = new Label("TEAM [" + motor.getEquipo().grupo() + "]: " + motor.getEquipo().nombre());
        grupo.getStyleClass().add("subtitulo");

        raiz = new VBox(18, titulo, grupo, tarjetas);
        raiz.setAlignment(Pos.TOP_CENTER);
        raiz.setPadding(new Insets(48, 24, 24, 24));
        raiz.getStyleClass().add("fondo");
        for (String a : motor.getAvisos()) {
            Label l = Ui.aviso(a, false);
            l.setMaxWidth(784);
            raiz.getChildren().add(l);
        }
        VBox.setVgrow(tarjetas, Priority.NEVER);
        usuario.requestFocus();
    }

    Parent getRaiz() {
        return raiz;
    }
}
