# Etapa 1: compila y ejecuta todas las pruebas (si una falla, la imagen no se construye).
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY xp-framework/pom.xml xp-framework/
COPY antdron/pom.xml antdron/
COPY xp-framework/src xp-framework/src
COPY antdron/src antdron/src
COPY xp xp
COPY config config
COPY Dockerfile docker-compose.yml ./
RUN mvn -B -q verify

# Etapa 2: solo el JRE y el jar ejecutable.
FROM eclipse-temurin:17-jre
WORKDIR /opt/antdron
RUN useradd --system --create-home kgd && mkdir -p data db && chown -R kgd:kgd /opt/antdron
COPY --from=build /app/antdron/target/antdron.jar app.jar
COPY --chown=kgd:kgd config config
COPY --chown=kgd:kgd data data
ENV ANTDRON_CONFIG=/opt/antdron/config \
    ANTDRON_DATA=/opt/antdron/data \
    ANTDRON_DB=/opt/antdron/db/antdron.db \
    PORT=8080
USER kgd
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["consola"]
