# API / REST / Basic / What is REST

## 1. What REST Is

REST (Representational State Transfer) is an architectural style, not a
protocol or library. A REST API exposes **resources** at URLs and lets
clients manipulate them with a small, uniform set of operations. Servers
stay **stateless**: every request carries everything needed to handle it.

```
GET    /books/42      # read book 42
DELETE /books/42      # remove it
# same URL, different verb -> different operation
```

## 2. REST vs HTTP

HTTP is the transport protocol; REST is a set of constraints for designing
APIs, and HTTP happens to fit them well. You can use HTTP without being
RESTful (e.g. `POST /getBookById`), and REST is defined independently of
HTTP, even though in practice they almost always go together.

```
POST /getBook?id=42   # HTTP, but RPC-style (verb in the URL)
GET  /books/42        # HTTP used RESTfully (verb = method)
```

## 3. Resources and Representations

A resource is the thing (a book); a representation is one serialized view
of it (JSON, XML, HTML) sent over the wire. The client never touches the
resource directly, only representations, which is why the same resource
can be served in several formats.

```
GET /books/42
Accept: application/json
-> {"id": 42, "title": "Dune"}   // JSON representation of book 42
```
