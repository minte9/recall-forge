FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN ./gradlew bootJar

CMD ["java", "-jar", "build/libs/recallforge-0.0.1-SNAPSHOT.jar"]