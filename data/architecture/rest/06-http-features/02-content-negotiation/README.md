# API / REST / HTTP Features / Content Negotiation

## 1. Media Types

A media type (`type/subtype`) names a format: `application/json`,
`application/xml`, `text/csv`, `application/problem+json`. Parameters
refine it (`charset=utf-8`), and vendor types (`application/vnd.x+json`)
can carry versions.

```
application/json
application/problem+json         # RFC 9457 errors
application/vnd.books.v2+json    # vendor type, e.g. versioning
```

## 2. JSON Responses

Spring chooses the converter by matching the `Accept` header against the
producible types of the return value. With only Jackson on the classpath,
JSON is the default; adding `jackson-dataformat-xml` enables XML for the
same endpoint.

```
GET /books/42  Accept: application/json -> JSON
GET /books/42  Accept: application/xml  -> XML (if converter present)
GET /books/42  (no Accept)              -> JSON (first match)
```

## 3. produces and consumes

`produces` restricts which response types a handler offers (checked
against `Accept`, else 406); `consumes` restricts accepted request bodies
(checked against `Content-Type`, else 415). They also let two handlers
share a path with different formats.

```java
@PostMapping(value = "/books",
             consumes = MediaType.APPLICATION_JSON_VALUE,
             produces = MediaType.APPLICATION_JSON_VALUE)
Book create(@RequestBody NewBook b) { ... }

@GetMapping(value = "/books", produces = "text/csv")
String csv() { ... }            // same path, CSV variant
```
