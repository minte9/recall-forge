# Architecture / API / gRPC / gRPC Runtime Concerns

## 1. HTTP/2 Transport & Multiplexing

gRPC runs on HTTP/2, so many concurrent RPCs share one TCP connection as independent streams -- no head-of-line blocking at the HTTP layer and no connection-pool churn. Headers are HPACK-compressed and messages are binary-framed. This is why gRPC stays efficient under heavy fan-out between services.

```
1 TCP connection
 |- stream 1: GetOrder    (in flight)
 |- stream 3: ListOrders  (in flight, same socket)
```

## 2. Deadlines & Cancellation

Every gRPC call should carry a deadline -- an absolute time by which it must finish -- propagated across downstream calls. When it passes, the whole call tree is cancelled and resources freed, preventing pile-ups behind a slow dependency. A client that goes away also cancels its in-flight calls automatically.

```java
stub.withDeadlineAfter(2, TimeUnit.SECONDS).getOrder(req);
// downstream calls inherit the remaining time budget
```

## 3. Status Codes & Interceptors

gRPC replies with a code from a fixed set (`OK`, `NOT_FOUND`, `DEADLINE_EXCEEDED`, `UNAVAILABLE`, ...) plus a message and optional details, so clients branch on a known enum rather than a string. Interceptors wrap calls on both sides for auth, logging, metrics, and retries without touching service methods.

```java
Status.NOT_FOUND.withDescription("order 42").asRuntimeException();
```
