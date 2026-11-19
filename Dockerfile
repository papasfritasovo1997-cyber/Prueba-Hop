# Imagen de la aplicación AntCiberDron (JavaFX), sin Maven.
# La interfaz gráfica se ve en el navegador: http://localhost:6080/vnc.html
FROM eclipse-temurin:21-jdk

RUN apt-get update \
 && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
      xvfb x11vnc novnc websockify \
      libgtk-3-0t64 libxtst6 libgl1 libxxf86vm1 fonts-dejavu-core \
 && rm -rf /var/lib/apt/lists/*

# JavaFX para Linux (los .jar de lib/ son para Windows y no se copian).
ARG JAVAFX=21.0.5
ADD https://repo1.maven.org/maven2/org/openjfx/javafx-base/${JAVAFX}/javafx-base-${JAVAFX}-linux.jar \
    https://repo1.maven.org/maven2/org/openjfx/javafx-graphics/${JAVAFX}/javafx-graphics-${JAVAFX}-linux.jar \
    https://repo1.maven.org/maven2/org/openjfx/javafx-controls/${JAVAFX}/javafx-controls-${JAVAFX}-linux.jar \
    /opt/javafx/

WORKDIR /opt/antdron
# Controlador SQLite del hormiguero virtual (el mismo .jar que usa VS Code).
COPY lib/sqlite-jdbc-3.54.1.0.jar lib/
COPY framework-xp framework-xp
COPY antdron antdron
COPY config config
COPY xp xp
COPY xp.properties Dockerfile docker-compose.yml ./
COPY .vscode .vscode
COPY docker/iniciar.sh docker/iniciar.sh

# Compila las mismas cuatro carpetas que VS Code, copia los recursos y ejecuta todas las pruebas:
# si alguna falla, la imagen no se construye.
RUN find framework-xp/src framework-xp/pruebas antdron/src antdron/pruebas -name '*.java' > /tmp/fuentes.txt \
 && javac -encoding UTF-8 -nowarn -d bin -cp '/opt/javafx/*:lib/*' @/tmp/fuentes.txt \
 && for d in framework-xp/src antdron/src; do (cd "$d" && find . -type f ! -name '*.java' -exec cp --parents {} /opt/antdron/bin/ \;); done \
 && java -Djava.awt.headless=true -cp 'bin:/opt/javafx/*:lib/*' ec.edu.epn.xp.XpCli probar

ENV ANTDRON_RAIZ=/opt/antdron \
    DISPLAY=:99
EXPOSE 6080
ENTRYPOINT ["sh", "/opt/antdron/docker/iniciar.sh"]
CMD ["gui"]
