#!/bin/sh
# Punto de entrada del contenedor.
#   gui      (por defecto) pantalla virtual + noVNC: abra http://localhost:6080/vnc.html
#   consola  versión de consola (requiere docker compose run con -it)
#   otro     comando del framework XP (probar, verificar, backlog, ...)
cd /opt/antdron || exit 2
CP="bin:/opt/javafx/*:lib/*"
modo=${1:-gui}

case "$modo" in
  gui)
    Xvfb :99 -screen 0 1280x800x24 -nolisten tcp >/dev/null 2>&1 &
    sleep 1
    x11vnc -display :99 -forever -shared -nopw -quiet -rfbport 5900 >/dev/null 2>&1 &
    websockify --web /usr/share/novnc 6080 localhost:5900 >/dev/null 2>&1 &
    echo "AntCiberDron listo: abra http://localhost:6080/vnc.html?autoconnect=true&resize=scale"
    # Si la ventana se cierra, se vuelve a abrir (el contenedor sigue disponible en el navegador).
    while true; do
      java -Dprism.order=sw -cp "$CP" ec.edu.epn.antdron.App
      echo "La aplicación se cerró; se abre de nuevo en 2 s..."
      sleep 2
    done
    ;;
  consola)
    exec java -cp "$CP" ec.edu.epn.antdron.App consola
    ;;
  *)
    exec java -Djava.awt.headless=true -cp "$CP" ec.edu.epn.xp.XpCli "$@"
    ;;
esac
