# Architecture / API / GraphQL / GraphQL Performance

## 1. The N+1 Problem

Resolving a list of N orders and then each order's customer fires 1 query for the list plus N for the customers. Because resolvers run per field per item, this explodes silently as result sets grow. It is the single most common GraphQL performance bug.

```graphql
query { orders { customer { name } } }
# 1 query for orders  +  N queries, one per order's customer
```

## 2. DataLoader Batching

A DataLoader collects the individual key lookups made during one tick of the event loop, issues a single batched query, and hands each resolver its slice. It also caches within the request. This turns the N+1 into 2 queries with little change to resolver code.

```java
// instead of customerRepo.findById(id) per order:
customerLoader.load(order.customerId());  // batched + cached / request
```

## 3. Query Cost & Depth Limiting

Because clients compose their own queries, a hostile or careless one can request a deeply nested or huge graph. Defences: cap query depth, give each field a cost and reject over a budget, and paginate every list. Persisted queries -- the client sends a hash of an approved query -- remove arbitrary queries entirely.

```
maxDepth: 7      reject anything deeper
maxCost: 1000    sum of field costs
```
