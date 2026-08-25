# JVM / Java / Concurrency / Creating Threads

## 1. Two ways

Java threads can be created by extending Thread or implementing Runnable. Runnable is generally preferred: it separates the task from the thread executing it and leaves the class free to extend something else, since Java only allows single inheritance.

```java
class MyThread extends Thread {        // IS a thread
    public void run() {
        System.out.println("running");
    }
}

class MyTask implements Runnable {     // HAS a task
    public void run() {
        System.out.println("running");
    }
}
```

## 2. Extending Thread class

Extending Thread makes the class both the task and the thread that runs it — the two are tightly coupled, and the class can't extend anything else. Override run() to define the work, then call start() to run it.

```java
class Worker extends Thread {
    public void run() {
        System.out.println(Thread.currentThread().getName());
    }
}

new Worker().start();  // extending IS the thread itself
```

## 3. Implementing Runnable

Implementing Runnable separates what should run from how it runs: the task doesn't need to be a Thread itself, so the class stays free to extend something else. Being a functional interface, Runnable also works well with lambdas.

```java
class Order implements Runnable {
    private final String name;
    Order(String name) { this.name = name; }

    public void run() {
        System.out.println("preparing " + name);
    }
}

new Thread(new Order("Pizza")).start();
new Thread(new Order("Pasta")).start();
```
