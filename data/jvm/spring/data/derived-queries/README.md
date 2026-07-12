## JVM / Spring / Data / Derived Queries

### 1. findBy Naming Conventions

Spring Data parses a method name like findByLastNameAndAge(String lastName, int age) into a query, matching each "And"/"Or"-separated segment to an entity field. findBy, existsBy, countBy, and deleteBy are the common prefixes; the field names after them must match the entity's property names exactly (case-sensitive after the first letter).

~~~java
interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByLastName(String lastName);
    boolean existsByEmail(String email);
    long countByActiveTrue();
    void deleteByEmail(String email);
}
~~~


### 2. Combining Conditions (And/Or)

Multiple conditions chain with And/Or directly in the method name, along with comparison keywords like GreaterThan, Like, In, and OrderBy for sorting. This keeps simple queries fully declarative -- no query string to write or maintain -- but readability degrades once a method name accumulates more than 2-3 conditions.

~~~java
interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatusAndTotalGreaterThan(String status, double total);
    List<Order> findByCustomerNameOrderByCreatedAtDesc(String customerName);
}
~~~


### 3. Limitations of Derived Queries

Derived query names get unwieldy fast for anything beyond a few conditions, and they can't express joins across unrelated entities, aggregations, or complex filtering logic. Once a method name would need 4+ conditions or custom logic, switch to @Query (see the dedicated topic) instead of fighting the naming convention.

~~~java
// Getting unwieldy -- time to switch to @Query instead
List<Order> findByStatusAndTotalGreaterThanAndCustomerCountryAndCreatedAtAfter(
    String status, double total, String country, LocalDateTime after
);
~~~
