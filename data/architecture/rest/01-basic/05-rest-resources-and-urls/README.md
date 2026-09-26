# API / REST / Basic / Resources and URLs

## 1. Resources and Identifiers

Anything worth naming (a book, an author, an order) is a resource, and each
one gets a stable URI. The identifier should stay the same regardless of
representation or operation, so clients can bookmark, cache and link to it.

```
/books/42          # identifies book 42, whatever the format
/authors/7         # identifies author 7
```

## 2. Designing REST URLs

Use plural nouns, lowercase, hyphens between words, and keep verbs out: the
HTTP method is the verb. Use path segments for identity and hierarchy, and
query parameters for filtering, sorting and paging.

```
GET /book-reviews?rating=5     # good: noun, hyphen, filter in query
GET /getAllBookReviews         # bad: verb and camelCase in URL
```

## 3. Collections vs Individual Resources

A collection URL (`/books`) represents the set: GET lists it, POST adds to
it. An item URL (`/books/42`) represents one member: GET, PUT, PATCH and
DELETE act on it. This two-level pattern repeats for every resource type.

```
/books        GET list | POST create
/books/{id}   GET one  | PUT/PATCH update | DELETE remove
```
