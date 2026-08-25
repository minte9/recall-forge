# JVM / Java / Concurrency / Executor Service

## 1. ExecutorService Framework

ExecutorService runs tasks on a managed pool of reusable threads instead of spawning a new thread per task. A fixed pool size caps concurrent work, protecting the app from resource exhaustion when many tasks arrive at once.

```java
ExecutorService pool = Executors.newFixedThreadPool(2);

pool.submit(() -> processTrade(101, "AAPL"));
pool.submit(() -> processTrade(102, "MSFT"));
pool.submit(() -> processTrade(103, "BTC"));

pool.shutdown();  // stop accepting new tasks; let queued ones finish
```

## 2. Named Threads

Pooled threads get generic names like pool-1-thread-1 by default, which makes logs hard to read. A custom ThreadFactory lets you assign meaningful names to threads as they're created.

```java
class NamedFactory implements ThreadFactory {
    private final AtomicInteger n = new AtomicInteger(1);

    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName("worker-" + n.getAndIncrement());  // vs pool-1-thread-N
        return t;
    }
}

Executors.newFixedThreadPool(2, new NamedFactory());
```
