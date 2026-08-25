# JVM / Java / Concurrency / Theory

## 1. What is a Thread?

A thread is the smallest unit of execution within a process. Every Java app starts with one main thread; more can run concurrently and share process memory but each keeps its own call stack. Calling start() spawns a new thread — calling run() directly just executes on the current one.

```java
Thread w = new Worker();
w.start();  // new thread; runs Worker.run() concurrently
w.run();    // no new thread; runs on the caller instead

class Worker extends Thread {
    public void run() {
        System.out.println(Thread.currentThread().getName());
    }
}
```

## 2. When to use Threads

Threads help when work can proceed independently: I/O waits, tasks that don't depend on each other, or background work that shouldn't block the caller. Shared state accessed from multiple threads still needs synchronization.

```java
// I/O-bound waits: network, DB, files, external APIs
callBankApi();
queryDatabase();

// Independent tasks, but shared state still needs sync
synchronized (account) {
    account.withdraw(amount);
}

// Background work: don't make the user wait for it
sendEmail();
generateReport();
```

## 3. When to NOT use threads

Threads add complexity and are not always the answer. Avoid them for steps that must run in strict order, and avoid spawning raw threads for many tasks — an unbounded thread count risks resource exhaustion. Most Spring Boot apps never create threads manually; the server's pool already handles requests.

```java
// Strict order: threading adds risk here, not speed
validateOrder();
chargeCard();
shipProduct();

new Thread(task).start();  // NOK: no bound on thread count
Executors.newFixedThreadPool(10).submit(task);  // OK: bounded

// Tomcat already pools threads per request — you mostly
// reach for threads yourself for background jobs/consumers.
```
