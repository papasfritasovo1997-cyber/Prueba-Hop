package ec.edu.epn.antdron.ui;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import ec.edu.epn.antdron.automata.Automata;

/**
 * Dibuja el grafo del autómata: los estados se ubican por niveles (distancia desde q0), los finales con
 * doble círculo, la flecha de inicio a la izquierda y los lazos sobre el estado.
 */
final class LienzoAutomata extends Canvas {

    private static final double RADIO = 22;
    private static final double SEP_X = 130;
    private static final double SEP_Y = 95;
    private static final double MARGEN = 70;

    private static final Color FONDO = Color.web("#0b1220");
    private static final Color TRAZO = Color.web("#e5e7eb");
    private static final Color ESTADO = Color.web("#1f2937");
    private static final Color FINAL = Color.web("#b91c1c");
    private static final Color ETIQUETA = Color.web("#fbbf24");

    LienzoAutomata(Automata a) {
        Map<Integer, double[]> pos = posiciones(a);
        double ancho = pos.values().stream().mapToDouble(p -> p[0]).max().orElse(0) + MARGEN + RADIO;
        double alto = pos.values().stream().mapToDouble(p -> p[1]).max().orElse(0) + MARGEN;
        setWidth(Math.max(ancho, 400));
        setHeight(Math.max(alto, 180));
        dibujar(a, pos);
    }

    /** Niveles por recorrido en anchura; dentro de cada nivel, en orden de número de estado. */
    static Map<Integer, double[]> posiciones(Automata a) {
        Map<Integer, Integer> nivel = new HashMap<>();
        Deque<Integer> cola = new ArrayDeque<>();
        nivel.put(a.getInicial(), 0);
        cola.add(a.getInicial());
        while (!cola.isEmpty()) {
            int q = cola.poll();
            for (int destino : a.transiciones().get(q).values()) {
                if (!nivel.containsKey(destino)) {
                    nivel.put(destino, nivel.get(q) + 1);
                    cola.add(destino);
                }
            }
        }
        for (int q : a.estados()) {
            nivel.putIfAbsent(q, nivel.size());
        }
        Map<Integer, List<Integer>> porNivel = new TreeMap<>();
        nivel.forEach((q, n) -> porNivel.computeIfAbsent(n, k -> new ArrayList<>()).add(q));
        int maxFilas = porNivel.values().stream().mapToInt(List::size).max().orElse(1);
        double centro = MARGEN + (maxFilas - 1) * SEP_Y / 2 + 20;
        Map<Integer, double[]> pos = new HashMap<>();
        porNivel.forEach((n, estados) -> {
            estados.sort(Integer::compare);
            for (int i = 0; i < estados.size(); i++) {
                double y = centro + (i - (estados.size() - 1) / 2.0) * SEP_Y;
                pos.put(estados.get(i), new double[] {MARGEN + 30 + n * SEP_X, y});
            }
        });
        return pos;
    }

    private void dibujar(Automata a, Map<Integer, double[]> pos) {
        GraphicsContext g = getGraphicsContext2D();
        g.setFill(FONDO);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setLineWidth(1.6);
        g.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);

        double[] inicio = pos.get(a.getInicial());
        flecha(g, inicio[0] - RADIO - 45, inicio[1], inicio[0] - RADIO, inicio[1]);

        a.transiciones().forEach((origen, t) -> {
            Map<Integer, StringBuilder> etiquetas = new TreeMap<>();
            t.forEach((s, d) -> {
                StringBuilder sb = etiquetas.computeIfAbsent(d, k -> new StringBuilder());
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(s);
            });
            etiquetas.forEach((destino, simbolos) -> arista(g, pos.get(origen), pos.get(destino),
                    origen.equals(destino), destino < origen, simbolos.toString()));
        });

        for (int q : a.estados()) {
            double[] p = pos.get(q);
            g.setFill(a.getFinales().contains(q) ? FINAL : ESTADO);
            g.fillOval(p[0] - RADIO, p[1] - RADIO, 2 * RADIO, 2 * RADIO);
            g.setStroke(TRAZO);
            g.strokeOval(p[0] - RADIO, p[1] - RADIO, 2 * RADIO, 2 * RADIO);
            if (a.getFinales().contains(q)) {
                g.strokeOval(p[0] - RADIO + 4, p[1] - RADIO + 4, 2 * RADIO - 8, 2 * RADIO - 8);
            }
            g.setFill(TRAZO);
            g.fillText(Automata.nombre(q), p[0], p[1]);
        }
    }

    private static void arista(GraphicsContext g, double[] o, double[] d, boolean lazo, boolean haciaAtras,
                               String etiqueta) {
        g.setStroke(TRAZO);
        g.setFill(ETIQUETA);
        if (lazo) {
            double cx = o[0];
            double cy = o[1] - RADIO - 14;
            g.strokeOval(cx - 14, cy - 14, 28, 28);
            punta(g, cx + 10, cy + 9, cx + 7, o[1] - RADIO + 1);
            g.fillText(etiqueta, cx, cy - 24);
            return;
        }
        double ang = Math.atan2(d[1] - o[1], d[0] - o[0]);
        double x1 = o[0] + RADIO * Math.cos(ang);
        double y1 = o[1] + RADIO * Math.sin(ang);
        double x2 = d[0] - RADIO * Math.cos(ang);
        double y2 = d[1] - RADIO * Math.sin(ang);
        if (haciaAtras) {
            // Arco por debajo para no superponerse con la arista de ida.
            double mx = (x1 + x2) / 2;
            double my = Math.max(y1, y2) + 60;
            g.beginPath();
            g.moveTo(x1, y1);
            g.quadraticCurveTo(mx, my, x2, y2);
            g.stroke();
            punta(g, mx, my, x2, y2);
            g.fillText(etiqueta, mx, my - 10);
        } else {
            g.strokeLine(x1, y1, x2, y2);
            punta(g, x1, y1, x2, y2);
            double mx = (x1 + x2) / 2;
            double my = (y1 + y2) / 2;
            g.fillText(etiqueta, mx + 8 * Math.sin(ang), my - 12 * Math.cos(ang));
        }
    }

    private static void flecha(GraphicsContext g, double x1, double y1, double x2, double y2) {
        g.setStroke(TRAZO);
        g.strokeLine(x1, y1, x2, y2);
        punta(g, x1, y1, x2, y2);
    }

    /** Punta de flecha en (x2, y2) apuntando desde (x1, y1). */
    private static void punta(GraphicsContext g, double x1, double y1, double x2, double y2) {
        double ang = Math.atan2(y2 - y1, x2 - x1);
        double l = 10;
        g.setFill(TRAZO);
        g.fillPolygon(new double[] {x2, x2 - l * Math.cos(ang - 0.4), x2 - l * Math.cos(ang + 0.4)},
                new double[] {y2, y2 - l * Math.sin(ang - 0.4), y2 - l * Math.sin(ang + 0.4)}, 3);
        g.setFill(ETIQUETA);
    }
}
