# API / REST / Production REST APIs / API Performance

## 1. Database and Query Performance

Most slow endpoints are slow queries. Watch for N+1 queries when mapping
relations, missing indexes on filter and sort columns, and loading whole
entities just to return a few fields. Fetch exactly what the response
needs.

```java
@Query("select new dev.x.BookSummary(b.id, b.title, a.name) " +
       "from Book b join b.author a")      // one query, no N+1
List<BookSummary> summaries();
```

## 2. Payload Size

Large responses cost serialization time, bandwidth and client parsing.
Return summaries in lists and details on item endpoints, avoid deep
embedding, and enable gzip compression for text payloads.

```yaml
server.compression.enabled: true
server.compression.mime-types: application/json
server.compression.min-response-size: 1KB
```

## 3. Pagination and Caching

Bounded pages keep response time constant as data grows. HTTP caching
(`Cache-Control`, ETag → 304) avoids repeating work for unchanged data,
and an application cache (`@Cacheable`) helps with expensive, rarely
changing lookups.

```java
@Cacheable("authors")
public Author getAuthor(Long id) { ... }    // hit DB once per id

// GET /books?page=0&size=20  -> constant cost regardless of total
```
