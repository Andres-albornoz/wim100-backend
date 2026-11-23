# ---------- Etapa 1: compilar el .jar con Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primero solo el pom: Docker guarda las dependencias en caché
# y no las vuelve a descargar si solo cambia el código.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Las pruebas necesitan MySQL, así que se ejecutan aparte (no al construir la imagen)
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: imagen liviana solo con el JRE ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuario sin privilegios de administrador (buena práctica de seguridad)
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
