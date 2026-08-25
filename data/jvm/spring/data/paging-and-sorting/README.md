# JVM / Spring / Data / Paging and Sorting

## 1. Pageable and Page<T>

Adding a Pageable parameter to any repository method (including derived queries) automatically paginates the results -- Spring Data slices the query with LIMIT/OFFSET under the hood. The return type Page<T> includes the page content plus metadata like total elements and total pages, everything a paginated UI needs in one response.

```java
interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByStatus(String status, Pageable pageable);
}

Page<Order> page = orderRepository.findByStatus("SHIPPED", PageRequest.of(0, 20)); // page 0, size 20
System.out.println(page.getTotalElements()); // metadata included automatically
```


## 2. Sort

Sort can be passed on its own (for unpaginated results) or combined into a Pageable via PageRequest.of(page, size, sort), specifying one or more fields and direction. This avoids needing a separate OrderBy-suffixed derived query method for every possible sort order a caller might want.

```java
List<Order> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

Pageable pageable = PageRequest.of(0, 20, Sort.by("total").descending());
```


## 3. Combining Paging with Derived Queries

Pageable and Sort parameters work seamlessly alongside derived query conditions and @Query methods alike -- Spring Data just appends the paging/sorting logic to whatever query it already built. One method signature handles filtering, sorting, and pagination together, without three separate methods.

```java
interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomerNameContaining(String name, Pageable pageable); // filter + page + sort, all in one
}
```
