# JVM / Spring / Data / Transactions

## 1. @Transactional Basics

@Transactional wraps a method in a database transaction -- all its writes commit together on success, or roll back together if an unchecked exception escapes the method. It's typically placed on service-layer methods that perform multiple related writes, so a failure partway through doesn't leave the database in an inconsistent state.

```java
@Service
class OrderService {
    @Transactional
    void placeOrder(Order order) {
        orderRepository.save(order);
        inventoryService.reserveStock(order); // if this throws, the save() above rolls back too
    }
}
```


## 2. Propagation

Propagation controls how a @Transactional method behaves when called from inside another transaction -- REQUIRED (the default) joins the existing transaction if one exists, or starts a new one if not; REQUIRES_NEW always suspends any existing transaction and starts a fresh one, useful for logging that must persist even if the outer transaction rolls back.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
void logAuditEvent(String event) {
    // Commits independently, even if the calling method's transaction later rolls back
    auditRepository.save(new AuditLog(event));
}
```


## 3. Rollback Rules

By default, @Transactional only rolls back on unchecked exceptions (RuntimeException/Error) -- a checked exception commits the transaction as-is unless rollbackFor explicitly says otherwise. This surprises developers coming from the assumption that any exception triggers a rollback; checked exceptions need @Transactional(rollbackFor = SomeCheckedException.class) to roll back.

```java
@Transactional(rollbackFor = IOException.class) // without this, a checked IOException would NOT roll back
void processAndSave(byte[] data) throws IOException {
    repository.save(parse(data)); // parse() might throw a checked IOException
}
```
