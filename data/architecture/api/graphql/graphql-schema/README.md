# Architecture / API / GraphQL / GraphQL Schema

## 1. Types, Queries & Schema

A GraphQL schema is a strongly typed graph: object types with fields, plus two special roots -- `Query` for reads and `Mutation` for writes. The client asks for exactly the fields it needs and gets JSON shaped identically. The schema is introspectable, so docs and tooling are generated from it.

```graphql
type Order { id: ID!, status: String!, lines: [Line!]! }
type Query { order(id: ID!): Order }
```

## 2. Resolvers

Each field is backed by a resolver function that knows how to fetch that piece -- from a database, another service, or a parent object already in hand. The engine walks the query tree calling resolvers, so a nested query naturally becomes many small fetches unless you batch them.

```java
// resolver for Order.lines, given the parent Order
public List<Line> lines(Order order) {
    return lineRepo.findByOrderId(order.id());
}
```

## 3. Mutations & Subscriptions

By convention one mutation makes one logical change and returns the affected object so the client can update its cache. Subscriptions add a third root for server-pushed events over a persistent transport (usually WebSocket), delivering a payload each time something the client watches changes.

```graphql
type Mutation { cancelOrder(id: ID!): Order }
type Subscription { orderStatusChanged(id: ID!): Order }
```
