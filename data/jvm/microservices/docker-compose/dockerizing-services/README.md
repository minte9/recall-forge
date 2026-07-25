## JVM / Microservices / Docker Compose / Dockerizing Services

### 1. Dockerfile per Service

Each service gets its own minimal `Dockerfile` built from a JRE-only base image (not a full JDK), copying just the built JAR in. Smaller runtime images start faster and shrink the attack surface compared to shipping build tools into production.

~~~dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 7001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
~~~

### 2. Externalizing Host/Port via Env Vars

A composite service that hardcodes `http://localhost:7001` for a downstream call works alone but breaks once each service runs in its own container -- "localhost" no longer means the same thing. Host/port must become environment-variable-driven config before Compose is introduced.

~~~properties
app.product-service.host=${PRODUCT_SERVICE_HOST:localhost}
app.product-service.port=${PRODUCT_SERVICE_PORT:7001}
~~~
