# API / REST / Basic / JSON

## 1. Objects and Arrays

JSON has two structures: objects (`{}` of key/value pairs, keys always
double-quoted strings) and arrays (`[]` of ordered values). Values are
strings, numbers, booleans, `null`, objects or arrays. There are no dates
or comments, so dates travel as ISO-8601 strings.

```json
{
  "id": 42,
  "title": "Dune",
  "tags": ["sci-fi", "classic"],
  "published": "1965-08-01"
}
```

## 2. Java Objects to JSON

Spring Boot uses Jackson to map fields to JSON properties automatically:
getters/record components become keys, nested objects become nested JSON,
and collections become arrays. Annotations tweak names or hide fields.

```java
record Book(Long id, String title,
            @JsonIgnore String internalNote) {}
// -> {"id":42,"title":"Dune"}  (internalNote omitted)
```

## 3. Request and Response Payloads

In REST, JSON is the usual body for both directions. The client sends
`Content-Type: application/json` to describe what it sends and
`Accept: application/json` to say what it wants back. Keep payloads flat
and predictable; they are your API's public contract.

```
POST /books
Content-Type: application/json

{"title": "Dune", "authorId": 7}
-> 201 {"id": 43, "title": "Dune", "authorId": 7}
```
