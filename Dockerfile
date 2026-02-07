# STAGE 1: Build a- Le "Builder"
# On utilise une image qui contient le JDK et Maven pour compiler le projet.
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copier le pom.xml d'abord pour utiliser le cache Docker pour les dépendances.
# Si le pom.xml ne change pas, Docker n'aura pas à retélécharger les dépendances à chaque build.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copier le reste du code source
COPY src ./src

# Compiler l'application et créer le fichier .jar. On saute les tests dans le build Docker.
RUN mvn package -DskipTests

# STAGE 2: Run - Le "Runner"
# On utilise une image JRE beaucoup plus légère, car on n'a plus besoin de Maven ou du JDK.
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copier uniquement le fichier .jar qui a été construit dans l'étage précédent.
COPY --from=builder /app/target/*.jar app.jar

# Exposer le port sur lequel l'application Spring Boot tourne.
EXPOSE 8080

# La commande pour démarrer l'application.
ENTRYPOINT ["java", "-jar", "app.jar"]