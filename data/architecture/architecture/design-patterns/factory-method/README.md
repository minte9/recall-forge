# Architecture / Architecture / Design Patterns / Factory Method

## 1. Coupling to Concrete Classes

Calling new SomeConcreteClass() directly inside business logic hardcodes a dependency on that exact implementation, making it impossible to swap in a different one without editing that code. This becomes a real problem when the concrete type to create depends on runtime conditions, or new variants get added over time.

```java
// Business logic tightly coupled to a specific implementation
Notifier notifier = new EmailNotifier(); // hardcoded -- can't easily swap or extend
```


## 2. Factory Method

The Factory Method pattern defines a method whose job is to create and return an object, letting subclasses (or a parameter-driven implementation) decide which concrete type gets instantiated. Callers depend only on the abstract return type, never the concrete classes, so new variants can be added without touching existing call sites.

```java
interface Notifier { void send(String msg); }
class EmailNotifier implements Notifier { public void send(String msg) {} }
class SmsNotifier implements Notifier { public void send(String msg) {} }

class NotifierFactory {
    static Notifier create(String type) { // the factory method
        return switch (type) {
            case "email" -> new EmailNotifier();
            case "sms" -> new SmsNotifier();
            default -> throw new IllegalArgumentException(type);
        };
    }
}
```


## 3. Simple Static Factory

A simple static factory method (like List.of() or Optional.of()) is a lighter-weight cousin of the full Factory Method pattern -- it centralizes creation logic in one place, but without the subclass-driven polymorphism of the GoF pattern. Reach for it when you just need to hide a constructor call, not swap implementations.

```java
class Point {
    private Point(int x, int y) { /* ... */ }

    static Point of(int x, int y) { // simple static factory, not the full GoF pattern
        return new Point(x, y);
    }
}
```
