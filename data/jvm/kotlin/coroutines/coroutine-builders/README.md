# JVM / Kotlin / Coroutines / Coroutine Builders

## 1. launch vs async

`launch` starts a coroutine and returns a `Job` -- fire-and-forget, for work whose result you don't need back. `async` returns a `Deferred<T>`, and you call `.await()` to get the result -- the coroutine equivalent of Java's `Future`/`CompletableFuture`, but non-blocking to wait on.

```kotlin
launch { logAnalyticsEvent() }         // no result needed
val result = async { computeTotal() }  // result needed later
println(result.await())
```

## 2. Dispatchers

A `Dispatcher` decides which thread pool a coroutine runs on: `Dispatchers.Main` for UI work, `Dispatchers.IO` for blocking I/O (backed by a large elastic pool, like Java's cached thread pool), `Dispatchers.Default` for CPU-bound work (sized to core count). Switching is just `withContext(Dispatchers.IO) { ... }`, no manual `ExecutorService` wiring.
