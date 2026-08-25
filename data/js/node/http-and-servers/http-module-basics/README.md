# JS / Node / HTTP & Servers / HTTP Module Basics

## 1. Creating a Server

`http.createServer` takes a callback invoked once per incoming request,
and `.listen(port)` starts accepting connections — the foundation every
Node web framework (Express included) is built on.

```javascript
const http = require("http");
http.createServer((req, res) => {
  res.end("Hello");
}).listen(3000);
```


## 2. Request & Response Objects

`req` is a readable stream carrying method/url/headers and the request
body; `res` is a writable stream — `res.writeHead` sets status/headers,
`res.end` sends the final chunk and closes.

```javascript
http.createServer((req, res) => {
  res.writeHead(200, { "Content-Type": "text/plain" });
  res.end(`${req.method} ${req.url}`);
});
```


## 3. Routing Manually

Without a framework, routing means branching on `req.url`/`req.method`
yourself — this manual boilerplate is exactly what frameworks like
Express exist to remove.

```javascript
if (req.url === "/users" && req.method === "GET") {
  res.end("user list");
} else {
  res.writeHead(404); res.end();
}
```
