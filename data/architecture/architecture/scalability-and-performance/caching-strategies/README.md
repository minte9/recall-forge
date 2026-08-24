## Architecture / Architecture / Scalability & Performance / Caching Strategies

### 1. Cache-Aside Pattern

In cache-aside, the application checks the cache first; on a miss it reads from the database and writes the result into the cache before returning it. The cache stays a pure derived copy of the database -- if it's ever cleared entirely, the app keeps working, just slower until it's warm again.

~~~java
Order order = cache.get(id);
if (order == null) {
    order = db.findById(id);
    cache.put(id, order);
}
~~~

### 2. Write-Through vs Write-Behind

Write-through updates the cache and the database in the same request, so they never drift apart, but every write pays the cache's latency too. Write-behind updates the cache immediately and writes to the database asynchronously later, which is faster but risks losing the write if the process crashes before that async write completes.

~~~java
// Write-through: cache and DB updated in the same request
cache.put(id, order);
db.save(order);

// Write-behind: cache updated now, DB write queued for later
cache.put(id, order);
writeQueue.enqueueAsync(() -> db.save(order));
~~~

### 3. Cache Invalidation

Stale cache entries are the classic failure mode -- a value gets written to the database but the old cached copy is never cleared. A TTL bounds how stale data can get even if invalidation is missed; explicitly deleting the cache key on write closes the gap immediately instead of waiting for the TTL to expire.

~~~java
db.save(order);
cache.evict(order.getId()); // close the staleness window now
~~~
