## JVM / Java / Concurrency / Deadlock

### 1. Deadlock

A deadlock happens when two or more threads wait forever for locks held by each other. The app doesn't crash — the affected threads just stop making progress. It's usually caused by acquiring the same locks in inconsistent order.

~~~java
Object account1 = new Object(), account2 = new Object();

Runnable t1 = () -> {
    synchronized (account1) {
        synchronized (account2) { /* transfer */ }
    }
};
Runnable t2 = () -> {
    synchronized (account2) {           // opposite order — deadlock
        synchronized (account1) { /* transfer */ }
    }
};
~~~

### 2. FX system (example)

The fix is to always acquire locks in the same order, regardless of call direction — e.g. by a stable key like account ID. That removes the circular wait that causes deadlocks.

~~~java
Account first = (a.getId() < b.getId()) ? a : b;
Account second = (first == a) ? b : a;

synchronized (first) {
    synchronized (second) {
        first.withdraw(amount);
        second.deposit(amount);
    }
}
~~~
