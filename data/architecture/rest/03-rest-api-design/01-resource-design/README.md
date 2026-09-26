# API / REST / REST API Design / Resource Design

## 1. Resource-Oriented Design

Start from the domain's nouns (books, authors, loans), give each a URL,
and let the uniform HTTP methods act on them. Clients learn one pattern
and apply it everywhere, instead of memorizing a custom operation per
endpoint as in RPC-style APIs.

```
RPC style:    POST /createLoan, POST /returnBook
Resource:     POST /loans, DELETE /loans/{id}
```

## 2. Nouns Instead of Verbs

The HTTP method is the verb, so URLs should be nouns. When an action
doesn't fit CRUD, model it as a resource of its own (a "cancellation"),
or as a sub-resource action as a last resort.

```
POST /orders/7/cancel          # acceptable: explicit action
POST /orders/7/cancellations   # noun: the cancellation is a resource
GET  /cancelOrder?id=7         # avoid: verb + GET with side effect
```

## 3. Nested Resources

Nest a URL when the child only makes sense within the parent (a book's
reviews). Keep nesting to one level; deeper chains become brittle.
Top-level access still works for resources with their own identity.

```
GET /books/42/reviews          # reviews of book 42
GET /reviews/913               # a review by its own id
GET /authors/7/books/42/reviews/913   # too deep
```
