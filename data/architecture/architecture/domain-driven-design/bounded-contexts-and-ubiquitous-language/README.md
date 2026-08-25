# Architecture / Architecture / Domain-Driven Design / Bounded Contexts & Ubiquitous Language

## 1. Bounded Context

A bounded context is a boundary within which a model and its terms have one consistent meaning. "Product" means something different in the Catalog context (description, images) than in the Shipping context (weight, dimensions) -- DDD doesn't force one shared `Product` class across the whole system, it lets each context define its own.

```java
// Catalog context
class Product { String description; List<String> images; }

// Shipping context -- same name, different model
class Product { double weightKg; Dimensions size; }
```

## 2. Ubiquitous Language

The ubiquitous language is the vocabulary shared by domain experts and the code within one bounded context -- if the business calls it a "chargeback," the code should use `Chargeback`, not `PaymentReversalEventTypeB`. When code and conversation use the same words, mapping a bug report to the right class is immediate.

```java
// Business says "chargeback" -> code says Chargeback
class Chargeback {
    Money amount;
    String reasonCode;
}
```

## 3. Anti-Corruption Layer

When integrating with a legacy system or another bounded context whose model doesn't match your own, an anti-corruption layer translates between the two at the boundary, so foreign concepts and naming never leak into your domain model. It costs a translation layer, but it's cheaper than letting an external system's design decisions creep into yours.

```java
// Anti-corruption layer: translates legacy shape to our domain
class LegacyOrderTranslator {
    Order toDomain(LegacyOrderDto dto) {
        return new Order(dto.getOrderRef(), dto.getCustNo());
    }
}
```
