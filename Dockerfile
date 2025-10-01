# Étape 1: Build de l'application avec Maven
# Utilise une image officielle de Maven avec Java 17 pour compiler le code
FROM maven:3.8.5-openjdk-17 AS build

# Définit le répertoire de travail à l'intérieur du conteneur
WORKDIR /app

# Copie du fichier pom.xml pour profiter du cache de Docker
COPY pom.xml .

# Copie du reste du code source de l'application
COPY src ./src

# Lancement de la commande de build de Maven pour créer le JAR
# -DskipTests pour accélérer le build en ignorant les tests
RUN mvn -f pom.xml clean package -DskipTests


# Étape 2: Exécution de l'application
# On part d'une image Java 17 très légère, juste pour l'exécution
FROM eclipse-temurin:17-jdk AS base

# Définit le répertoire de travail
WORKDIR /app

# On copie uniquement le JAR qui a été créé à l'étape de build
COPY --from=build /app/target/solSolutionQrCodeApp-0.0.1-SNAPSHOT.jar app.jar

# On expose le port sur lequel l'application va tourner
EXPOSE 8080

# C'est la commande qui sera lancée au démarrage du conteneur
# On utilise la variable d'environnement PORT fournie par Render
ENTRYPOINT ["sh", "-c", "echo MAIL_USERNAME=$MAIL_USERNAME && java -jar app.jar"]
