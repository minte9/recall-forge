## JVM / Microservices / Docker Compose / Compose Orchestration

### 1. Docker Compose Networking

Compose puts every listed service on a shared network and lets them address each other by service name instead of a container IP -- `product-service` resolves like a hostname. This is what makes the env-var host config from Dockerizing Services actually work end to end.

~~~yaml
product-composite-service:
  environment:
    PRODUCT_SERVICE_HOST: product-service
  depends_on:
    - product-service
~~~

### 2. Build-Deploy-Verify Loop

The recurring workflow after any code change is: rebuild the JAR, rebuild the image, bring Compose back up, then curl the composite endpoint to confirm the whole chain still responds -- skipping the rebuild step is the most common cause of "my fix isn't showing up."

~~~bash
./gradlew clean build
docker compose up -d --build
curl http://localhost:7000/product-composite/1
~~~
