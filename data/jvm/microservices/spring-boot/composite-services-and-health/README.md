## JVM / Microservices / Spring Boot / Composite Services & Health

### 1. Composite Service Pattern

A composite service (e.g. `product-composite-service`) has no data of its own -- it calls several core services (product, review, recommendation) and merges their responses into one payload for the client. This keeps clients from having to know about or call every downstream service individually.

~~~java
// Composite aggregates 3 downstream calls into 1 response
ProductAggregate composite = new ProductAggregate(
    product, reviews, recommendations
);
~~~

### 2. Actuator Health Checks

Before wiring services together, each one exposes `/actuator/health` so its liveness can be checked in isolation -- this is the first thing to verify after starting a service, and later becomes what orchestrators poll to decide whether to route traffic to an instance.

~~~bash
curl http://localhost:7001/actuator/health
# {"status":"UP"}
~~~
