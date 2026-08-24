## Architecture / Architecture / Domain-Driven Design / DDD Building Blocks

### 1. Entities vs Value Objects

An entity has a persistent identity that survives changes to its attributes -- two `Customer` objects with the same name are still different customers if their IDs differ. A value object has no identity of its own; it's defined entirely by its attributes, so two `Money` objects with the same amount and currency are interchangeable.

~~~java
class Customer { Long id; String name; } // identity via id

record Money(int amountCents, String currency) {} // equal by value
~~~

### 2. Aggregates and Aggregate Roots

An aggregate is a cluster of entities and value objects treated as one consistency boundary -- an `Order` and its `OrderLine`s, for example. The aggregate root (`Order`) is the only object outside code may reference directly; everything inside the boundary is reached through it, which is what lets the root enforce its own invariants.

~~~java
class Order { // aggregate root
    List<OrderLine> lines; // only reachable through Order
    void addLine(OrderLine line) {
        if (lines.size() >= 50) throw new IllegalStateException();
        lines.add(line);
    }
}
~~~

### 3. Repositories

A repository gives the domain a collection-like interface for loading and saving aggregates, like `orders.findById(id)`, while hiding whether that means a SQL query, a document lookup, or an in-memory map. The domain talks to the interface; only the implementation knows about the database.

~~~java
interface OrderRepository {
    Order findById(Long id);
    void save(Order order);
}
~~~
