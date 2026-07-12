## JVM / Java / Concurrency / Virtual Threads

### 1. Virtual Threads

Virtual threads, introduced in Java 21, are lightweight threads managed by the Java runtime instead of the operating system. They allow applications to create thousands or even millions of concurrent tasks while using far fewer system resources than traditional platform threads. They are particularly well suited for applications that spend most of their time waiting for I/O.

~~~java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualThreadsExample {
    void main() throws Exception {

        // Java 8 (expensive threads)
        // ==========================
        System.out.println("Platform threads (fixed pool 10):\n");
        long start1 = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(10);  // Look Here

        for (int i=0; i<100; i++) {
            int taskId = i;
            executor.submit(() -> doWork("T-" + taskId));
        }
        executor.shutdown();
        while (!executor.isTerminated()) {}
        
        long end1 = System.currentTimeMillis();
        System.out.println("\nTime: " + (end1 - start1) + " ms");
        System.out.println("\n============================\n");


        // Java 21 (virtual threads)
        // =========================
        System.out.println("Virtual threads:\n");
        long start2 = System.currentTimeMillis();

        try (var virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()) {  // Look Here
            for (int i=0; i<100; i++) {
                int taskId = i;
                virtualExecutor.submit(() -> doWork("V-" + taskId));
            }
        }

        long end2 = System.currentTimeMillis();
        System.out.println("\nTime: " + (end2 - start2) + " ms");

    }

    
    static void doWork(String name) {
        try {
            System.out.println(name + " START");
            Thread.sleep(1000); // simulate blocking I/O
            System.out.println(name + " END");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/*
    Platform threads (fixed pool 10): 

    T-1 START 
    T-0 START 
    ...
    T-98 END 
    T-99 END 

    Time: 10714 ms

    ============================

    Virtual threads:

    V-0 START 
    V-1 START 
    ...
    V-99 END
    V-98 END

    Time: 1650 ms
*/
~~~


### 2. Blocking Operations

Virtual threads are ideal for blocking operations such as REST calls, database queries, messaging systems, and microservices. They are not intended to speed up CPU-intensive computations, where a fixed-size thread pool based on the number of processor cores is usually the better choice.

~~~java
// One virtual thread per task (Java 21)
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> fetchCustomer());
    executor.submit(() -> queryDatabase());
    executor.submit(() -> sendNotification());
}
~~~

