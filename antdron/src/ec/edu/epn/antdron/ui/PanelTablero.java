package ec.edu.epn.antdron.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import ec.edu.epn.antdron.MotorAntdron;
import ec.edu.epn.antdron.config.Integrante;
import ec.edu.epn.antdron.tablero.EstadoTarea;
import ec.edu.epn.antdron.tablero.TableroXp;
import ec.edu.epn.antdron.tablero.Tarea;

/**
 * Tablero de trabajo XP: las tareas de la división por fases en columnas Por hacer / En curso / Hecha,
 * con filtros por integrante y sesión, y el avance por integrante, historia y práctica XP.
 */
final class PanelTablero {

    private static final String TODOS = "Todos los integrantes";
    private static final String TODAS = "Todas las sesiones";

    private final MotorAntdron motor;
    private final VBox raiz = new VBox(12);
    private final ComboBox<String> integrante = new ComboBox<>();
    private final ComboBox<String> sesion = new ComboBox<>();
    private final HBox resumen = new HBox(12);
    private final HBox columnas = new HBox(12);
    private final TabPane detalle = new TabPane();
    private TableroXp tablero;

    PanelTablero(MotorAntdron motor) {
        this.motor = motor;
        raiz.setPadding(new Insets(16));
        raiz.setId("panel-tablero");

        integrante.getItems().add(TODOS);
        motor.getEquipo().integrantes().forEach(i -> integrante.getItems().add(i.nombre()));
        integrante.setValue(TODOS);
        sesion.getItems().add(TODAS);
        Tarea.SESIONES.forEach(s -> sesion.getItems().add(Tarea.nombreSesion(s)));
        sesion.setValue(TODAS);
        integrante.setOnAction(e -> pintarColumnas());
        sesion.setOnAction(e -> pintarColumnas());
        Button actualizar = new Button("Actualizar");
        actualizar.setId("actualizar-tablero");
        actualizar.setOnAction(e -> cargar());
        Label ayuda = Ui.texto("Las tareas están en xp/tareas.csv. Una tarea pasa sola a «Hecha» cuando su captura "
                + "está en docs/evidencias/.");
        ayuda.getStyleClass().add("tenue");
        HBox.setHgrow(ayuda, Priority.ALWAYS);
        HBox filtros = new HBox(10, new Label("Ver:"), integrante, sesion, actualizar, ayuda);
        filtros.setAlignment(Pos.CENTER_LEFT);

        columnas.setMinHeight(260);
        VBox.setVgrow(columnas, Priority.ALWAYS);
        detalle.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        detalle.setPrefHeight(230);
        detalle.setMinHeight(200);

        raiz.getChildren().addAll(Ui.titulo("Tablero de trabajo XP · Grupo " + motor.getEquipo().grupo() + " "
                + motor.getEquipo().nombre()), resumen, filtros, columnas, detalle);
        cargar();
    }

    private void cargar() {
        try {
            tablero = TableroXp.cargar(motor.getRaiz(), motor.getEquipo());
        } catch (IOException | IllegalArgumentException e) {
            raiz.getChildren().setAll(Ui.titulo("Tablero de trabajo XP"), Ui.aviso(e.getMessage(), true));
            return;
        }
        pintarResumen();
        pintarColumnas();
        pintarDetalle();
    }

    private void pintarResumen() {
        int total = tablero.getTareas().size();
        long hechas = tablero.contar(EstadoTarea.HECHA);
        List<TableroXp.AvancePractica> practicas = tablero.porPractica();
        long defendidas = practicas.stream().filter(TableroXp.AvancePractica::defendida).count();
        String velocidad = tablero.porIteracion().stream()
                .map(it -> "Iteración " + it.numero() + ": " + it.terminados() + "/" + it.planificados() + " pts")
                .collect(Collectors.joining("\n"));
        resumen.getChildren().setAll(
                cifra("Tareas hechas", hechas + " / " + total,
                        tablero.contar(EstadoTarea.EN_CURSO) + " en curso · " + tablero.contar(EstadoTarea.POR_HACER)
                                + " por hacer", total == 0 ? 0 : (double) hechas / total),
                cifra("Prácticas XP defendidas", defendidas + " / " + practicas.size(),
                        "Con todas sus evidencias entregadas", practicas.isEmpty() ? 0 : (double) defendidas / practicas.size()),
                cifra("Historias aceptadas", tablero.historiasAceptadas() + " / " + tablero.getHistorias().size(),
                        tablero.pruebasFallidas() == 0 ? "Sin pruebas fallidas" : tablero.pruebasFallidas() + " pruebas fallidas",
                        tablero.getHistorias().isEmpty() ? 0 : (double) tablero.historiasAceptadas() / tablero.getHistorias().size()),
                cifra("Velocidad", velocidad.isEmpty() ? "—" : "", velocidad, -1));
    }

    private static VBox cifra(String titulo, String valor, String detalle, double avance) {
        Label t = new Label(titulo);
        t.getStyleClass().add("tenue");
        Label v = new Label(valor);
        v.getStyleClass().add("cifra");
        Label d = Ui.texto(detalle);
        VBox caja = Ui.tarjeta(t);
        if (!valor.isEmpty()) {
            caja.getChildren().add(v);
        }
        caja.getChildren().add(d);
        if (avance >= 0) {
            ProgressBar barra = new ProgressBar(avance);
            barra.setMaxWidth(Double.MAX_VALUE);
            caja.getChildren().add(barra);
        }
        caja.setSpacing(4);
        caja.setPadding(new Insets(10, 14, 10, 14));
        HBox.setHgrow(caja, Priority.ALWAYS);
        caja.setMaxWidth(Double.MAX_VALUE);
        caja.setPrefWidth(250);
        return caja;
    }

    private void pintarColumnas() {
        if (tablero == null) {
            return;
        }
        List<Tarea> visibles = new ArrayList<>();
        for (Tarea t : tablero.getTareas()) {
            boolean delIntegrante = TODOS.equals(integrante.getValue()) || tablero.responsables(t).stream()
                    .anyMatch(i -> i.nombre().equals(integrante.getValue()));
            boolean deLaSesion = TODAS.equals(sesion.getValue()) || Tarea.nombreSesion(t.sesion()).equals(sesion.getValue());
            if (delIntegrante && deLaSesion) {
                visibles.add(t);
            }
        }
        columnas.getChildren().clear();
        for (EstadoTarea estado : EstadoTarea.values()) {
            List<Tarea> deEstado = visibles.stream().filter(t -> tablero.estado(t) == estado).toList();
            VBox tarjetas = new VBox(8);
            tarjetas.setPadding(new Insets(4));
            deEstado.forEach(t -> tarjetas.getChildren().add(tarjeta(t, estado)));
            if (deEstado.isEmpty()) {
                Label vacio = new Label("Sin tareas");
                vacio.getStyleClass().add("tenue");
                tarjetas.getChildren().add(vacio);
            }
            ScrollPane scroll = new ScrollPane(tarjetas);
            scroll.setFitToWidth(true);
            scroll.getStyleClass().add("columna-scroll");
            VBox.setVgrow(scroll, Priority.ALWAYS);
            Label cabecera = Ui.subtitulo(estado.getEtiqueta() + " (" + deEstado.size() + ")");
            VBox columna = new VBox(8, cabecera, scroll);
            columna.getStyleClass().addAll("columna-tablero", "columna-" + estado.name().toLowerCase());
            columna.setPadding(new Insets(10));
            columna.setId("columna-" + estado.name().toLowerCase().replace('_', '-'));
            HBox.setHgrow(columna, Priority.ALWAYS);
            columna.setPrefWidth(330);
            columna.setMaxWidth(Double.MAX_VALUE);
            columnas.getChildren().add(columna);
        }
    }

    private Node tarjeta(Tarea t, EstadoTarea estado) {
        Label cabecera = new Label(t.id() + " · " + Tarea.nombreSesion(t.sesion())
                + (t.practica().isEmpty() ? "" : " · " + t.practica()));
        cabecera.getStyleClass().add("tenue");
        cabecera.setWrapText(true);
        Label tarea = Ui.texto(t.descripcion());
        tarea.getStyleClass().add("tarea-texto");
        String quienes = t.responsables().contains(TableroXp.TODOS) ? "Todos"
                : tablero.responsables(t).stream().map(Integrante::nombre).collect(Collectors.joining(", "));
        Label responsable = new Label(quienes);
        responsable.getStyleClass().add("tarea-responsable");
        VBox caja = new VBox(4, cabecera, tarea, responsable);
        if (!t.artefacto().isEmpty()) {
            caja.getChildren().add(detalle("Artefacto: " + t.artefacto(), tablero.existe(t.artefacto())));
        }
        if (!t.evidencia().isEmpty()) {
            String texto = "Evidencia: " + (t.evidenciaEsArchivo()
                    ? t.evidencia().substring(t.evidencia().lastIndexOf('/') + 1) : t.evidencia());
            caja.getChildren().add(detalle(texto, estado == EstadoTarea.HECHA));
        }
        caja.getStyleClass().addAll("tarea", "tarea-" + estado.name().toLowerCase());
        caja.setPadding(new Insets(8, 10, 8, 10));
        caja.setMinHeight(Region.USE_PREF_SIZE);
        return caja;
    }

    private static Label detalle(String texto, boolean listo) {
        Label l = new Label((listo ? "✔ " : "○ ") + texto);
        l.setWrapText(true);
        l.getStyleClass().add(listo ? "listo" : "pendiente");
        return l;
    }

    private void pintarDetalle() {
        TableView<TableroXp.AvanceIntegrante> personas = Ui.tabla("Sin integrantes");
        personas.getColumns().add(Ui.columna("Integrante", a -> a.integrante().nombre(), 170));
        personas.getColumns().add(Ui.columna("Rol XP", a -> a.integrante().rol(), 170));
        personas.getColumns().add(Ui.columna("Tareas hechas", a -> a.hechas() + " / " + a.total() + "  (" + a.porcentaje() + " %)", 120));
        personas.getColumns().add(Ui.columna("En curso", a -> String.valueOf(a.enCurso()), 80));
        personas.getColumns().add(Ui.columna("Historias del backlog", a -> String.join(", ", a.historias()), 250));
        personas.setItems(FXCollections.observableArrayList(tablero.porIntegrante()));
        personas.setId("tabla-integrantes");

        TableView<TableroXp.Historia> historias = Ui.tabla("No se encontró xp/historias.csv");
        historias.getColumns().add(Ui.columna("Historia", TableroXp.Historia::id, 70));
        historias.getColumns().add(Ui.columna("Título", TableroXp.Historia::titulo, 230));
        historias.getColumns().add(Ui.columna("Responsable", h -> String.join(", ", h.responsables()), 150));
        historias.getColumns().add(Ui.columna("Iteración", h -> String.valueOf(h.iteracion()), 70));
        historias.getColumns().add(Ui.columna("Puntos", h -> String.valueOf(h.puntos()), 60));
        historias.getColumns().add(Ui.columna("Estado", TableroXp.Historia::estado, 100));
        historias.getColumns().add(Ui.columna("Pruebas", h -> h.pasadas() + " ✔  " + h.fallidas() + " ✘", 90));
        historias.getColumns().add(Ui.columna("Aceptación", TableroXp.Historia::aceptacion, 110));
        historias.setItems(FXCollections.observableArrayList(tablero.getHistorias()));
        historias.setId("tabla-historias");

        TableView<TableroXp.AvancePractica> practicas = Ui.tabla("Sin prácticas");
        practicas.getColumns().add(Ui.columna("Práctica XP", TableroXp.AvancePractica::practica, 260));
        practicas.getColumns().add(Ui.columna("Tareas hechas", p -> p.hechas() + " / " + p.total(), 120));
        practicas.getColumns().add(Ui.columna("Estado", p -> p.defendida() ? "Defendida" : "Falta evidencia", 160));
        practicas.setItems(FXCollections.observableArrayList(tablero.porPractica()));
        practicas.setId("tabla-practicas");

        detalle.getTabs().setAll(new Tab("Por integrante", personas),
                new Tab("Historias (HU) y pruebas", historias),
                new Tab("17 prácticas XP", practicas));
    }

    Parent getRaiz() {
        return raiz;
    }
}
