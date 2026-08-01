## JVM / Java / Concurrency / Synchronization

### 1. Race condition

A race condition happens when multiple threads read and modify shared state without synchronization. Because thread scheduling is unpredictable, the outcome depends on timing — the same code can produce different, often wrong, results on different runs.

~~~java
class Account {
    private int balance = 100;

    void withdraw() {
        int seen = balance;      // read
        balance = seen - 80;     // write — not atomic together
    }
}
// Two threads both read 100, both compute 20 minus their own
// withdraw, then last write wins — one withdrawal is lost.
~~~

### 2. Locking

The synchronized keyword lets only one thread execute a block at a time. Other threads calling a synchronized method or block on the same object wait until the lock is released, protecting shared state from concurrent modification.

~~~java
class Account {
    private int balance;

    synchronized void withdraw(int amount) {  // one thread at a time
        if (balance >= amount) balance -= amount;
    }
}
~~~
