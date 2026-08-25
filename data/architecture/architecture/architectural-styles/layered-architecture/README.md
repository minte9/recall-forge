# Architecture / Architecture / Architectural Styles / Layered Architecture

## 1. Layered / N-Tier Architecture

A layered architecture stacks the app into horizontal layers -- typically presentation, business logic, and data access -- where each layer only calls the layer directly below it. This keeps concerns separated and each layer independently testable, at the cost of some boilerplate as a request passes through several layers to reach the database.

```java
class OrderController {
    OrderService service; // presentation -> business
}
class OrderService {
    OrderRepository repository; // business -> data
}
```

## 2. Layering Violations

A violation happens when a layer skips past the one directly below it -- a controller querying the database directly, bypassing the service layer. It looks like a shortcut, but it duplicates business rules across call sites and makes the service layer's guarantees unreliable, since some paths never go through it.

```java
// Violation: controller bypasses the service layer
class OrderController {
    OrderRepository repository; // should go through OrderService
    Order get(Long id) { return repository.findById(id); }
}
```
