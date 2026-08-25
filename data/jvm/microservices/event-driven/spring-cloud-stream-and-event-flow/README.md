# JVM / Microservices / Event-Driven / Spring Cloud Stream & Event Flow

## 1. Spring Cloud Stream Abstraction

Spring Cloud Stream sits between service code and the message broker, so services publish/consume through a binder abstraction instead of hand-rolling RabbitMQ client code. Swapping RabbitMQ for Kafka later is a config/dependency change, not a rewrite of business logic.

```gradle
implementation 'org.springframework.cloud:spring-cloud-starter-stream-rabbit'
```

## 2. Publish/Subscribe Event Flow

Instead of the composite service calling review/recommendation services directly to create data, `product-service` publishes a `ProductCreatedEvent` and each interested service reacts independently -- the publisher never knows or cares who's listening.

```java
public record ProductCreatedEvent(int productId, String name) {}

// product-service: saves, then publishes
// review-service:  listens -> creates default review
```
