# Architecture / Architecture / Scalability & Performance / Stateless Services & Session Handling

## 1. Stateless vs Stateful Services

A stateless service keeps no per-user state between requests -- any instance can handle any request, which is what makes horizontal scaling trivial: just add instances behind a load balancer. A stateful service keeps state in memory, like an in-process session, which means a user's requests must keep reaching the same instance.

```java
// Stateless: any instance can handle this request
Order order = orderService.findById(request.getOrderId());

// Stateful: only the instance holding this in-memory
// session can handle it
session.get(sessionId).addToCart(item);
```

## 2. Session Affinity Trade-offs

Session affinity ("sticky sessions") routes a user's requests back to the same instance, letting a stateful service work behind a load balancer. It's a workaround, not a fix -- it unbalances load toward instances with long-lived sessions, and losing that instance loses the session. Moving session state to a shared store like Redis removes the need for affinity entirely.

```java
// Sticky session: load balancer pins this client to one instance
// header: X-Sticky-Session: instance-3

// Shared store removes the need for affinity entirely
redis.set("session:" + sessionId, sessionData);
```
