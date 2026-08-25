# JVM / Spring / Hibernate / Caching

## 1. First-Level Cache

The first-level cache is the persistence context itself -- always on, scoped to a single transaction/session, and not configurable. It's why repeated find()/findById() calls for the same entity within one transaction hit memory, not the database, as shown in the Persistence Context topic.

```java
@Transactional
void example() {
    authorRepository.findById(1L); // hits the database
    authorRepository.findById(1L); // hits the first-level cache instead, no SQL executed
}
```


## 2. Second-Level Cache

The second-level cache is optional, spans across sessions/transactions (and even across the whole application), and requires an explicit cache provider (like Ehcache or Caffeine) plus @Cacheable on the entity. It trades a small risk of staleness for avoiding repeated database round-trips for data that changes rarely.

```java
@Entity
@Cacheable // opt-in, requires a configured second-level cache provider
class Country {
    @Id Long id;
    String name; // rarely changes -- a good candidate for second-level caching
}
```


## 3. When Caching Causes Stale Data

Second-level caching is risky for data that changes often or is modified outside the application (a direct SQL update, another service writing to the same table) -- the cache doesn't know about changes it didn't cause, and serves stale data until it expires or is explicitly evicted.

```java
// A bad candidate: order status changes constantly and from multiple places
@Entity
@Cacheable // risky here -- cached data can go stale quickly
class Order { /* ... */ }
```
