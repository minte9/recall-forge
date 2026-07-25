## JVM / Microservices / Event-Driven / Event-Driven Fundamentals

### 1. REST Composition vs Event-Driven Trade-offs

A composite service calling three services synchronously over REST is simple but tightly coupled -- every downstream service must be up at request time, and one slow service slows the whole chain. Event-driven messaging trades that synchronous dependency for loose coupling and better resilience, at the cost of eventual (not immediate) consistency.

### 2. RabbitMQ Basics for Microservices

RabbitMQ is added as a Compose service with a management UI for inspecting queues -- it's a common first choice over Kafka precisely because it's lighter weight and simpler to run locally while still being enough to learn the event-driven pattern.

~~~yaml
rabbitmq:
  image: rabbitmq:3.12-management
  ports:
    - "5672:5672"
    - "15672:15672"
~~~
