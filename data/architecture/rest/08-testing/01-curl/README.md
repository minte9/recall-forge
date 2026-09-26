# API / REST / Testing / curl

## 1. Testing REST APIs with curl

curl is the universal REST client: it's scriptable, available everywhere,
and shows exactly what goes over the wire. `-i` prints response headers,
`-v` shows the full exchange, and `-s` silences progress for piping into
`jq`.

```bash
curl -i http://localhost:8080/books/42        # status + headers + body
curl -s http://localhost:8080/books | jq '.[0].title'
```

## 2. Headers and JSON Bodies

`-H` adds headers and `-d` sends a body (which implies POST). Always set
`Content-Type: application/json` with a JSON body, or Spring responds 415.
`-d @file.json` reads the body from a file.

```bash
curl -i -X POST http://localhost:8080/books \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer '"$TOKEN" \
  -d '{"title":"Dune","authorId":7}'
```

## 3. GET/POST/PUT/DELETE Examples

`-X` sets the method. Together these cover a full CRUD smoke test,
which is handy right after deployment.

```bash
curl http://localhost:8080/books/43
curl -X PUT   localhost:8080/books/43 -H 'Content-Type: application/json' \
     -d '{"title":"Dune","year":1965}'
curl -X PATCH localhost:8080/books/43 -H 'Content-Type: application/json' \
     -d '{"year":1966}'
curl -i -X DELETE localhost:8080/books/43          # expect 204
```
