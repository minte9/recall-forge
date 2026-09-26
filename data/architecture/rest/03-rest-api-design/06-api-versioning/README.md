# API / REST / REST API Design / API Versioning

## 1. Why APIs Need Versions

Once clients depend on an API you can't change it freely: renaming a
field or changing a type breaks them. Versioning lets you ship breaking
changes as a new version while existing clients keep working on the old
one until they migrate.

```
v1: {"name": "Dune"}
v2: {"title": "Dune"}   # breaking rename -> needs v2
```

## 2. URI vs Header Versioning

URI versioning (`/v1/books`) is explicit, easy to test with curl and to
route. Header or media-type versioning keeps URLs stable but is less
visible. Most public APIs pick the URI for simplicity; consistency matters
more than the choice.

```
GET /v2/books/42                             # URI
GET /books/42  API-Version: 2                # custom header
GET /books/42  Accept: application/vnd.x.v2+json   # media type
```

## 3. Backward Compatibility

The best version bump is the one you avoid. Additive changes (new optional
fields, new endpoints) are safe if clients ignore unknown fields. Removing
or renaming fields, tightening validation or changing meaning are breaking.

```
safe:     add "subtitle" field, add GET /books/{id}/editions
breaking: remove "isbn", make "year" required, change id to string
```
