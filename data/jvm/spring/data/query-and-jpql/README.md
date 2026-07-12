## JVM / Spring / Data / @Query and JPQL

### 1. @Query with JPQL

@Query lets you write JPQL (Java Persistence Query Language) directly on a repository method -- JPQL looks like SQL but operates on entity names and fields instead of table/column names, so it stays portable across databases. Use it whenever a derived query name would be too complex or can't express what's needed.

~~~java
interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("select o from Order o where o.status = :status and o.total > :minTotal")
    List<Order> findExpensiveOrders(@Param("status") String status, @Param("minTotal") double minTotal);
}
~~~


### 2. Native Queries

@Query(value = "...", nativeQuery = true) drops down to raw SQL when JPQL can't express what's needed -- database-specific functions, complex joins, or performance-tuned queries. This sacrifices database portability in exchange for full SQL power, so it's reached for only when JPQL genuinely can't do the job.

~~~java
@Query(value = "SELECT * FROM orders WHERE status = :status LIMIT :limit", nativeQuery = true)
List<Order> findRecentOrders(@Param("status") String status, @Param("limit") int limit);
~~~


### 3. Named Parameters vs Positional

Named parameters (:status, bound with @Param("status")) are self-documenting and don't break if the argument order changes; positional parameters (?1, ?2) are terser but fragile -- reordering method arguments silently breaks the query. Named parameters are the safer default for anything beyond a single-parameter query.

~~~java
@Query("select o from Order o where o.status = ?1") // positional -- fragile if reordered
List<Order> findByStatusPositional(String status);

@Query("select o from Order o where o.status = :status") // named -- safer
List<Order> findByStatusNamed(@Param("status") String status);
~~~
