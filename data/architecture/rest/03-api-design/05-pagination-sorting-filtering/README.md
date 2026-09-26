# API / REST / API Design / Pagination Sorting Filtering

## 1. Pagination Parameters

Never return an unbounded collection. Offset paging (`page`/`size`) is
simple and supports jumping to a page; cursor paging (`after=<token>`)
stays stable and fast on large or changing data. Return paging metadata
alongside the items.

```
GET /books?page=2&size=20
-> {"items":[...], "page":2, "size":20, "totalElements":431}

GET /events?after=evt_913&limit=50   # cursor-based
```

## 2. Sorting

Expose sorting as a query parameter with field and direction. Whitelist
sortable fields: sorting on unindexed or internal columns is both a
performance and an information-leak risk.

```
GET /books?sort=published,desc&sort=title,asc
# server: allow only {title, published}; reject others with 400
```

## 3. Filtering Collections

Filters are query parameters named after the fields they constrain.
Keep them simple and composable (AND semantics), and document them;
avoid inventing a query language unless the API truly needs one.

```
GET /books?author=Herbert&year=1965&status=available
GET /books?publishedAfter=2000-01-01
```
