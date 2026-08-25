# JVM / Java / Concurrency / Virtual Threads

## 1. Virtual Threads

Virtual threads (Java 21) are lightweight threads managed by the JVM instead of the OS, letting an app run millions of concurrent tasks with far less memory than platform threads. They're best suited for I/O-bound work that spends most of its time waiting.

```java
// platform threads: pool caps concurrency at 10
try (var pool = Executors.newFixedThreadPool(10)) {
    for (int i = 0; i < 1000; i++) pool.submit(() -> doWork());
}

// virtual threads: one per task, thousands are cheap
try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 1000; i++) pool.submit(() -> doWork());
}
```

## 2. Blocking Operations

Virtual threads shine for blocking calls — REST requests, DB queries, messaging. They don't speed up CPU-bound work, where a small, fixed pool sized to core count still performs better.

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> fetchCustomer());
    executor.submit(() -> queryDatabase());
    executor.submit(() -> sendNotification());
}
```
