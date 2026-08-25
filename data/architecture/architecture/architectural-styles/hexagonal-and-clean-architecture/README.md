# Architecture / Architecture / Architectural Styles / Hexagonal & Clean Architecture

## 1. Ports and Adapters

Hexagonal architecture puts the domain at the center and defines "ports" -- interfaces -- for everything it needs from the outside world, like a database or an email sender. "Adapters" implement those ports for a specific technology. Swapping Postgres for an in-memory store means writing a new adapter; the domain code never changes.

```java
interface NotificationSender { // the port, owned by the domain
    void send(String to, String message);
}
```

## 2. The Dependency Rule

Clean Architecture's core rule: source code dependencies only point inward, toward the domain -- never outward toward frameworks or infrastructure. The domain layer doesn't import the web framework or the ORM; outer layers depend on the domain's interfaces instead. This is what lets business logic be tested without a database or web server.

```java
// domain/Order.java -- no framework imports
class Order { /* pure business logic */ }

// infrastructure/OrderController.java -- depends inward on domain
class OrderController {
    Order order; // infra depends on domain, never the reverse
}
```

## 3. Java Example: Port and Adapter

A port is a plain interface owned by the domain; an adapter is an infrastructure class that implements it. The domain depends only on `PaymentGateway`, never on `StripePaymentGateway`, so tests can substitute a fake adapter with no framework involved.

```java
// Port: owned by the domain, no infra imports
interface PaymentGateway {
    void charge(String customerId, int amountCents);
}

// Adapter: infra detail, implements the port
class StripePaymentGateway implements PaymentGateway {
    public void charge(String customerId, int amountCents) {
        // calls the Stripe SDK
    }
}
```
