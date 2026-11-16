package ec.edu.epn.antdron.web;

import java.util.List;

import ec.edu.epn.antdron.hormiga.TipoHormiga;
import ec.edu.epn.antdron.persistencia.HormigaRegistro;

/** Plantillas HTML de las pantallas. Todo texto variable pasa por {@link #e(String)}. */
final class Html {

    private static final String ESTILO = """
            body{font-family:system-ui,sans-serif;background:#111827;color:#e5e7eb;margin:0}
            header{background:#7f1d1d;padding:14px 24px;display:flex;justify-content:space-between;align-items:center}
            header h1{font-size:20px;margin:0}
            main{max-width:900px;margin:24px auto;padding:0 16px}
            .tarjeta{background:#1f2937;border-radius:10px;padding:18px 22px;margin-bottom:18px}
            input,select,button{font:inherit;padding:7px 10px;border-radius:6px;border:1px solid #4b5563;
              background:#111827;color:#e5e7eb}
            button{background:#b91c1c;border-color:#b91c1c;cursor:pointer}
            button.sec{background:#374151;border-color:#374151}
            table{width:100%;border-collapse:collapse}
            th,td{text-align:left;padding:8px;border-bottom:1px solid #374151}
            .aviso{background:#064e3b;padding:10px 14px;border-radius:8px;margin-bottom:16px}
            .error{background:#7f1d1d}
            .VIVA{color:#34d399}.MUERTA{color:#f87171}
            form.linea{display:flex;gap:8px;align-items:center;flex-wrap:wrap;margin:0}
            label{display:block;margin:10px 0 4px}
            a{color:#fca5a5}
            """;

    private Html() {
    }

    static String e(String texto) {
        if (texto == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(texto.length());
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '&' -> sb.append("&amp;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    static String pagina(String titulo, String cuerpo) {
        return "<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>" + e(titulo) + " · ANTDRON</title><style>" + ESTILO + "</style></head><body>"
                + cuerpo + "</body></html>";
    }

    private static String cabecera(String equipo, String derecha) {
        return "<header><h1>🐜 ANTDRON · Hormiguero virtual KGD</h1><div>" + derecha + "</div></header>"
                + "<main><p>Equipo: " + e(equipo) + "</p>";
    }

    private static String aviso(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        String clase = texto.startsWith("Error") ? "aviso error" : "aviso";
        return "<div class=\"" + clase + "\" role=\"status\">" + e(texto) + "</div>";
    }

    static String login(String equipo, String error) {
        return pagina("Ingreso", cabecera(equipo, "")
                + "<section class=\"tarjeta\"><h2>Ingreso de usuarios KGD</h2>"
                + aviso(error == null ? null : "Error: " + error)
                + "<form method=\"post\" action=\"/login\">"
                + "<label for=\"usuario\">Usuario</label><input id=\"usuario\" name=\"usuario\" required autofocus>"
                + "<label for=\"clave\">Contraseña</label><input id=\"clave\" name=\"clave\" type=\"password\" required>"
                + "<p><button type=\"submit\">Ingresar</button></p></form>"
                + "<p>¿No tienes hormiguero? <a href=\"/registro\">Regístrate</a></p></section></main>");
    }

    static String registro(String equipo, String error) {
        return pagina("Registro", cabecera(equipo, "")
                + "<section class=\"tarjeta\"><h2>Crear mi hormiguero virtual</h2>"
                + aviso(error == null ? null : "Error: " + error)
                + "<form method=\"post\" action=\"/registro\">"
                + "<label for=\"usuario\">Usuario (3-20 letras, números o _)</label>"
                + "<input id=\"usuario\" name=\"usuario\" required pattern=\"[A-Za-z0-9_]{3,20}\" autofocus>"
                + "<label for=\"clave\">Contraseña (mínimo 4 caracteres)</label>"
                + "<input id=\"clave\" name=\"clave\" type=\"password\" required minlength=\"4\">"
                + "<p><button type=\"submit\">Registrarme</button></p></form>"
                + "<p><a href=\"/login\">Ya tengo cuenta</a></p></section></main>");
    }

    static String hormiguero(String equipo, String usuario, TipoHormiga tipo, List<HormigaRegistro> hormigas,
                             List<String> alimentos, String avisoTexto, String estadoMuerta) {
        StringBuilder opciones = new StringBuilder();
        alimentos.forEach(a -> opciones.append("<option value=\"").append(e(a)).append("\">").append(e(a))
                .append("</option>"));

        StringBuilder filas = new StringBuilder();
        for (HormigaRegistro h : hormigas) {
            filas.append("<tr><td>#").append(h.id()).append("</td><td>").append(e(h.tipo())).append("</td><td>")
                 .append(e(h.sexo())).append("</td><td class=\"").append(e(h.estado())).append("\">")
                 .append(e(h.estado())).append("</td><td>");
            if (!estadoMuerta.equals(h.estado())) {
                filas.append("<form class=\"linea\" method=\"post\" action=\"/hormigas/alimentar\">")
                     .append("<input type=\"hidden\" name=\"id\" value=\"").append(h.id()).append("\">")
                     .append("<select name=\"alimento\" aria-label=\"Alimento para la hormiga ").append(h.id())
                     .append("\">").append(opciones).append("</select>")
                     .append("<button type=\"submit\" class=\"sec\">Alimentar</button></form>");
            } else {
                filas.append("—");
            }
            filas.append("</td></tr>");
        }
        String tabla = hormigas.isEmpty()
                ? "<p>Tu hormiguero está vacío. ¡Crea tu primera hormiga!</p>"
                : "<table><thead><tr><th>Id</th><th>Tipo</th><th>Sexo</th><th>Estado</th><th>Acción</th></tr>"
                  + "</thead><tbody>" + filas + "</tbody></table>";

        String salir = "<form class=\"linea\" method=\"post\" action=\"/logout\"><span>" + e(usuario)
                + "</span><button type=\"submit\" class=\"sec\">Salir</button></form>";
        return pagina("Mi hormiguero", cabecera(equipo, salir)
                + aviso(avisoTexto)
                + "<section class=\"tarjeta\"><h2>Crear hormiga</h2>"
                + "<p>Toda hormiga nace como <b>HLarva</b> y vive con <b>Néctar</b>. Si come <b>"
                + e(tipo.alimento().getNombre()) + "</b> se transforma en <b>" + e(tipo.getNombre())
                + "</b>, que vive solo si come " + e(tipo.alimento().getNombre()) + ".</p>"
                + "<form class=\"linea\" method=\"post\" action=\"/hormigas\">"
                + "<label for=\"sexo\" style=\"margin:0\">Sexo</label><select id=\"sexo\" name=\"sexo\">"
                + "<option value=\"HEMBRA\">Hembra</option><option value=\"MACHO\">Macho</option></select>"
                + "<button type=\"submit\">Crear HLarva</button></form></section>"
                + "<section class=\"tarjeta\"><h2>Mis hormigas (" + hormigas.size() + ")</h2>" + tabla
                + "</section></main>");
    }
}
