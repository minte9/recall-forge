# API / REST / Advanced REST / Concurrency

## 1. Concurrent Updates

Two clients read the same resource, both edit, both PUT. The second write
silently overwrites the first: the **lost update** problem. Stateless
REST has no locks held between requests, so the API must detect the
conflict at write time.

```
A: GET /books/42 (v1)   B: GET /books/42 (v1)
A: PUT title="X"        B: PUT year=1966   <- A's change lost
```

## 2. Optimistic Locking

A `@Version` column is incremented on every update, and the UPDATE
includes `WHERE version = ?`. If someone else updated first, zero rows
match and JPA throws `OptimisticLockException`, which you map to 409
Conflict.

```java
@Entity class Book {
    @Id Long id;
    @Version long version;   // UPDATE ... WHERE id=? AND version=?
}
```

## 3. Preventing Lost Updates over HTTP

Expose the version as an `ETag`; clients send it back in `If-Match`. A
mismatch returns 412 Precondition Failed, and the client must re-fetch and
merge. Some APIs require `If-Match` on writes (428 if missing).

```
GET /books/42        -> ETag: "7"
PUT /books/42  If-Match: "7"  -> 200, ETag: "8"
PUT /books/42  If-Match: "7"  -> 412 (someone updated to 8)
```
