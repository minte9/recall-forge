# JVM / Java / Basics / Access Modifiers

## 1. public, protected, default, and private

Four access levels, from most to least visible: public (anywhere), protected (same package + subclasses elsewhere), default/package-private (same package only, no keyword needed), private (same class only). Choosing the narrowest level that still works is the standard rule -- it minimizes what other code can depend on.

```java
public class Account {
    public String id;            // visible everywhere
    protected double balance;    // package + subclasses
    String internalNote;         // default: package-private, no modifier
    private String pin;          // this class only
}
```


## 2. Package Structure and Visibility

A class's package is determined by its package declaration, not its folder location alone (though convention keeps them matching). Two classes in the same package can access each other's package-private and protected members directly, which is how a package boundary acts as a natural encapsulation unit.

```java
// File: com/example/orders/Order.java
package com.example.orders;

class Order { // package-private class, only visible within com.example.orders
    void process() { /* ... */ }
}
```


## 3. Why Package-Private Exists

Package-private exists as a middle ground: it lets classes within one logical module collaborate closely (sharing helper classes, internal state) while still hiding those details from unrelated code outside the package. It's the mechanism behind "package by feature" designs where each package exposes only a small public API.

```java
package com.example.orders;

public class OrderService {       // public: the package's API
    private final OrderValidator validator = new OrderValidator();
}

class OrderValidator {            // package-private: an implementation detail
    boolean isValid(Order order) { return true; }
}
```
