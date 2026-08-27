# Architecture / API / GraphQL / GraphQL vs REST

## 1. Over- & Under-fetching

REST endpoints return a fixed shape, so a mobile screen often downloads fields it ignores (over-fetching) or must call three endpoints to assemble one view (under-fetching). GraphQL lets the client name exactly the fields and relations it needs in one round trip -- its main draw for varied front-ends.

```graphql
# one request replaces GET /order/42 + /customer/7 + /lines
query { order(id: 42) { status customer { name } lines { sku } } }
```

## 2. Single Endpoint & Versioning

GraphQL exposes one endpoint and evolves the schema instead of cutting `/v2`: add fields freely, mark old ones `@deprecated`, and watch usage to know when removal is safe. REST usually versions the whole surface. The GraphQL approach needs field-level analytics to be done safely.

```graphql
type Order {
  total: Int @deprecated(reason: "use totalCents")
  totalCents: Int
}
```

## 3. Caching Challenges

REST rides on HTTP caching: GET + URL + `ETag` works in browsers, CDNs, and proxies for free. GraphQL usually POSTs to one URL, so that layer does not apply; caching moves to the client (normalised store keyed by type and id) and server (per resolver). GET-based persisted queries can restore some HTTP caching.

```
REST:    Cache-Control + ETag on GET /orders/42
GraphQL: client normalised cache; server DataLoader / response cache
```
