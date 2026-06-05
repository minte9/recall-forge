FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN ./gradlew dependencies || true

COPY src src

RUN ./gradlew bootJar

CMD ["sh", "-c", "java -jar build/libs/*.jar"]