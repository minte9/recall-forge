# API / REST / Basic / HTTP Basics

## 1. Request and Response

HTTP is a request/response protocol: the client sends one request, the
server returns exactly one response. A request has a method, a target URL,
headers and an optional body; a response has a status code, headers and an
optional body.

```
GET /books/42 HTTP/1.1          <- request line: method + target
Host: api.example.com

HTTP/1.1 200 OK                 <- status line
Content-Type: application/json

{"id": 42, "title": "Dune"}
```

## 2. URL, Headers, and Body

The URL identifies *what* (path) and filters *how* (query string). Headers
carry metadata such as content type, auth and caching. The body carries the
payload itself, typically JSON for REST, and is usually empty for GET.

```
POST /books?notify=true HTTP/1.1     # path + query string
Content-Type: application/json       # metadata about the body
Authorization: Bearer eyJ...

{"title": "Dune"}                    # the payload
```

## 3. Client-Server Communication

Client and server evolve independently and share only the HTTP contract.
Because the server keeps no session state between calls, each request must
be self-contained (auth token, ids, parameters). That's what lets you add
servers behind a load balancer freely.

```
client --(GET /books, token)--> server A
client --(GET /books, token)--> server B   # works: no server session
```
