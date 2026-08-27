# Architecture / API / Webhooks / Reliable Delivery

## 1. Retries & Exponential Backoff

Networks and consumers fail, so providers retry non-2xx deliveries on a widening schedule -- seconds, then minutes, then hours -- for a day or more before marking the endpoint unhealthy. As a consumer, return 2xx only once you have safely accepted the event, and expect duplicates from retries.

```
attempt 1: t+0     attempt 2: t+30s   attempt 3: t+2m
attempt 4: t+10m   ...  give up after ~24h
```

## 2. Idempotency & Deduplication

Because at-least-once delivery means the same event can arrive twice, handlers must be idempotent. Store each event id you have processed and drop repeats, or make the side effect naturally idempotent (upsert by a key). Never assume a webhook fires exactly once.

```java
if (!seen.add(event.id())) return ok();  // already handled
process(event);
```

## 3. Ordering & At-Least-Once

Deliveries are not guaranteed in order -- retries and parallel senders reorder them. Don't rely on arrival sequence; use the event's timestamp or a version number and ignore anything older than the state you already hold. If order truly matters, fetch current state from the provider on each event.

```java
if (event.version() <= current.version()) return ok();  // stale
```
