# Stage 1: Build stage (Java 21 और Maven 3.9 से JAR बनाना)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run stage (हल्की और सुरक्षित Java 21 JRE इमेज)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# कंपनी स्टैंडर्ड सिक्योरिटी: नॉर्मल यूजर बनाना (Alpine के हिसाब से सुरक्षित तरीका)
RUN adduser -D appuser && chown -R appuser /app
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]