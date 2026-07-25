## JVM / Microservices / Spring Data / Persistence per Service

### 1. Database per Service (MongoDB)

Each core service (product, review, recommendation) owns its own database -- the composite service never talks to a database directly, only to the core services' APIs. This keeps services independently deployable: no other service can be broken by a schema change it doesn't own.

### 2. Wiring MongoDB via Compose

MongoDB is added as its own Compose service, and each core service gets `SPRING_DATA_MONGODB_HOST`/`PORT` env vars plus a `depends_on` so it doesn't start racing ahead of the database it needs.

~~~yaml
product-service:
  environment:
    SPRING_DATA_MONGODB_HOST: mongodb
  depends_on:
    - mongodb
~~~

### 3. Repository Pattern per Microservice

In-memory sample data gets replaced by a `MongoRepository` per service -- each service's repository only ever touches that service's own collection, reinforcing the database-per-service boundary at the code level, not just the infra level.

~~~java
public interface ProductRepository
    extends MongoRepository<ProductEntity, String> {}
~~~
