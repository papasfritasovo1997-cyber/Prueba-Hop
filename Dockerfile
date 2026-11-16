# Imagen de la aplicación AntCiberDron (JavaFX) sin Maven.
# La interfaz gráfica se ve en el navegador: http://localhost:6080/vnc.html
FROM eclipse-temurin:21-jdk

RUN apt-get update \
 && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
      xvfb x11vnc novnc websockify \
      libgtk-3-0t64 libxtst6 libgl1 libxxf86vm1 fonts-dejavu-core \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/antdron
COPY herramientas herramientas
COPY framework-xp framework-xp
COPY antdron antdron
COPY config config
COPY xp xp
COPY xp.properties Dockerfile docker-compose.yml ./
COPY docker/iniciar.sh docker/iniciar.sh

# Descarga JavaFX, compila y ejecuta todas las pruebas: si alguna falla, la imagen no se construye.
RUN java herramientas/Construir.java probar

ENV ANTDRON_RAIZ=/opt/antdron \
    ANTDRON_PRISM=sw \
    DISPLAY=:99
EXPOSE 6080
ENTRYPOINT ["sh", "/opt/antdron/docker/iniciar.sh"]
CMD ["gui"]
