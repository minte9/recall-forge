## JVM / Microservices / Spring Boot / Workspace & Bootstrapping

### 1. Multi-Module Workspace Structure

A microservices workspace is a folder of independent Spring Boot projects (e.g. `product-service`, `review-service`, `product-composite-service`), each with its own build file and deployable JAR -- not one project with multiple packages. This lets each service be built, versioned, and deployed on its own schedule.

### 2. Core Service Bootstrapping (spring init CLI)

Each core service starts as its own `spring init` invocation with a distinct `--package-name`/`--name` and a minimal dependency set (`web,actuator` to start). Keeping the initial dependency list small avoids pulling in persistence or messaging concerns before the service boundary itself is proven out.

~~~bash
spring init --type=gradle-project --java-version=17 \
  --package-name=com.example.core.product \
  --dependencies=web,actuator --name=product-service
~~~
