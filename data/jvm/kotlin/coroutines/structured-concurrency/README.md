## JVM / Kotlin / Coroutines / Structured Concurrency

### 1. suspend Functions

`suspend` marks a function that can pause and resume without blocking its thread -- it can only be called from another suspend function or a coroutine builder (`launch`/`async`/`runBlocking`), so the compiler enforces that suspension points are always used inside a coroutine context.

### 2. Structured Concurrency & Cancellation

`coroutineScope` ties a group of child coroutines to a parent -- if one child fails, siblings are cancelled and the scope doesn't complete until all children finish. This is the key difference from raw threads or unmanaged futures: leaked, orphaned background work becomes a compile-time/structural non-issue instead of a runtime leak to hunt down.

~~~kotlin
suspend fun loadDashboard() = coroutineScope {
    val user = async { fetchUser() }
    val orders = async { fetchOrders() }
    Dashboard(user.await(), orders.await())
}
~~~
