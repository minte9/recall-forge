## JVM / Spring / Cloud / Resilience4j

### 1. The Circuit Breaker Pattern

A circuit breaker wraps a call to a remote service and "trips open" after too many failures, immediately failing subsequent calls (without even attempting them) for a cooldown period, instead of letting every caller keep timing out against a service that's clearly down. This prevents cascading failures across a whole microservices chain.

~~~java
// Closed: calls go through normally
// Open: calls fail immediately, no network call attempted, after too many failures
// Half-Open: a few test calls are let through to check if the service recovered
~~~


### 2. @CircuitBreaker Basics

@CircuitBreaker(name = "orderService") on a method wraps it with Resilience4j's circuit breaker logic, configured via application.properties (failure rate threshold, wait duration in the open state, sliding window size). Once tripped, calls skip straight to the fallback instead of attempting the real, likely-failing call.

~~~java
@CircuitBreaker(name = "orderService", fallbackMethod = "fallback")
List<Order> getOrders() {
    return orderServiceClient.getOrders(); // remote call, can trip the breaker on repeated failure
}

List<Order> fallback(Exception e) {
    return List.of(); // called when the circuit is open
}
~~~


### 3. Fallback Methods

A fallback method must have a matching signature (same return type, plus a Throwable/Exception parameter) and provides a degraded-but-functional response when the circuit is open or the call fails -- an empty list, a cached value, or a default -- so one failing dependency doesn't crash the entire request.

~~~java
@CircuitBreaker(name = "recommendations", fallbackMethod = "emptyRecommendations")
List<Product> getRecommendations(Long userId) {
    return recommendationClient.getFor(userId);
}

List<Product> emptyRecommendations(Long userId, Exception e) {
    return List.of(); // degrade gracefully instead of failing the whole page
}
~~~
