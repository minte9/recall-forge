## JVM / Spring / Cloud / Spring Cloud Gateway

### 1. Gateway Basics (Routes/Predicates/Filters)

Spring Cloud Gateway sits in front of your services as a single entry point -- a Route matches incoming requests via Predicates (path, header, method) and forwards them to a target service, optionally transformed by Filters (adding headers, rewriting the path, rate limiting) along the way.

~~~yaml
spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: lb://order-service        # lb:// resolves via service discovery
          predicates:
            - Path=/orders/**
~~~


### 2. Path-Based Routing

Predicate=Path=/orders/** routes any request under /orders to the order service, while a separate route handles /users/**, letting clients call one gateway host without knowing which backend service actually owns each URL prefix -- the classic API Gateway pattern for a microservices frontend.

~~~yaml
routes:
  - id: order-service
    uri: lb://order-service
    predicates: [Path=/orders/**]
  - id: user-service
    uri: lb://user-service
    predicates: [Path=/users/**]
~~~


### 3. Gateway vs Traditional Load Balancer

A traditional load balancer (like an ALB or nginx) typically just distributes traffic across identical backend instances; an API Gateway additionally does request routing to different services by path, cross-cutting concerns like authentication, and request/response transformation -- it's aware of your application's service boundaries, not just infrastructure.

~~~java
// A plain load balancer: "send this request to any healthy instance of X"
// An API Gateway: "route /orders/** to order-service, add an auth header, rate-limit it"
~~~
