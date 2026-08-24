## Architecture / Architecture / API Design / REST API Design Fundamentals

### 1. Richardson Maturity Model

The Richardson Maturity Model ranks APIs from level 0 (one endpoint, everything via POST, like RPC-over-HTTP) to level 3 (resources, proper HTTP verbs, and hypermedia links guiding the client on what it can do next). Most "REST" APIs stop at level 2 -- resources plus verbs -- and skip hypermedia, a pragmatic, defensible choice.

~~~java
// Level 0: one endpoint, action encoded in the body
POST /api  { "action": "getOrder", "id": 42 }

// Level 2: resource + verb
GET /orders/42
~~~

### 2. Resource Naming and HTTP Verbs

Resources are nouns, not verbs -- `/orders/42`, not `/getOrder?id=42` -- and the HTTP verb carries the action: GET reads, POST creates, PUT replaces, PATCH partially updates, DELETE removes. Consistently following this lets clients predict the URL and verb for a new operation without reading documentation every time.

~~~java
@GetMapping("/orders/{id}")     // read
@PostMapping("/orders")         // create
@DeleteMapping("/orders/{id}")  // remove
~~~

### 3. Idempotency

An operation is idempotent if calling it once has the same effect as calling it five times -- GET, PUT, and DELETE are supposed to be idempotent; POST usually isn't. This matters because clients retry failed requests on flaky networks: retrying an idempotent PUT is safe, retrying a non-idempotent POST can create the same order twice.

~~~java
// Idempotent: retrying leaves the same end state
PUT /orders/42  { "status": "SHIPPED" }

// Not idempotent: retrying can create a second order
POST /orders  { "item": "Widget" }
~~~
