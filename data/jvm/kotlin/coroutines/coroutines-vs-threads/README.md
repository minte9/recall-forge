## JVM / Kotlin / Coroutines / Coroutines vs Threads

### 1. Coroutines vs Threads/CompletableFuture

A coroutine is a suspendable computation, not an OS thread -- thousands can run on a small pool of real threads because a suspended coroutine parks without blocking its carrier thread. This replaces the callback pyramid of `CompletableFuture.thenCompose`/`thenApply` chains with code that reads top-to-bottom like synchronous logic.

~~~kotlin
suspend fun fetchUser(id: Int): String {
    delay(100)          // suspends, doesn't block the thread
    return "User$id"
}
~~~

### 2. Coroutines vs Java Virtual Threads

Java's virtual threads (JEP 444) solve the same "cheap concurrency" problem but keep the blocking-style API -- ordinary blocking calls just don't tie up an OS thread anymore. Coroutines instead require `suspend` functions and a different async programming model, but add structured concurrency and cancellation as first-class language features, which virtual threads don't provide on their own.
