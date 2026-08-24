## Architecture / Architecture / Architectural Styles / Monolith vs Microservices

### 1. Monolith Architecture

A monolith ships one deployable unit with a single codebase, database, and release cycle. It's the simplest way to start -- no network calls between modules, easy transactions, easy local debugging. The cost shows up later: every change requires redeploying the whole app, and unrelated teams collide in the same build.

~~~java
// Monolith: direct in-process call, same transaction
orderService.placeOrder(order);
inventoryService.reserveStock(order);
~~~

### 2. Microservices Architecture

Microservices split that single deployable into independently deployable services, each owning its own data store. Teams release on their own schedule and scale services independently, but calls that were a method invocation become network calls -- introducing latency, partial failure, and the need for eventual consistency instead of a shared transaction.

~~~java
// Microservices: network call, no shared transaction
orderClient.placeOrder(order);
inventoryClient.reserveStock(order); // may fail independently
~~~

### 3. Conway's Law and Team Boundaries

Conway's Law: a system's architecture mirrors the communication structure of the organization that built it. If four teams don't talk to each other, the system ends up with four services whether or not microservices were the plan. This is why service boundaries should start from team boundaries, not a technology checklist.

~~~java
// Team boundaries -> service boundaries (Conway's Law)
// team-payments  -> payment-service
// team-inventory -> inventory-service
// team-shipping  -> shipping-service
~~~
