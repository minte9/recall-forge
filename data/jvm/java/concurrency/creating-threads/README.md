## JVM / Java / Concurrency / Creating Threads

### 1. Two ways 

There are two ways to create a new thread in Java: by extending the Thread class or by implementing the Runnable interface. Both approaches execute code in a separate thread, but implementing Runnable is generally preferred because it promotes better object-oriented design and allows your class to extend another class if needed.

~~~java
// Option 1: extend Thread
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Running...");
    }
}

// Option 2: implement Runnable (recommended)
class MyTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Running...");
    }
}
~~~

### 2. Extending Thread class

When a class extends Thread, it becomes both the task and the thread that executes it. To define the work performed by the thread, override the run() method and start the thread by calling start(). Calling run() directly executes the method on the current thread instead of creating a new one.

 ~~~java
package threads.creating_threads;

public class Extending {
    public static void main(String[] args) {

        // main thread
        String name = Thread.currentThread().getName();
        System.out.println(name);

        // other threads
        Thread w1 = new Worker();
        Thread w2 = new Worker();
        Thread w3 = new Worker();
        w1.start();
        w2.start();
        w3.start();

        // with lambdas
        Runnable task = () -> {
            System.out.println(Thread.currentThread().getName());
        };
        Thread t = new Thread(task, "Task-1");
        t.start();

        /*  
            Output (order may vary):
            ========================
            main
            Thread-0
            Thread-2
            Thread-1
            Task-1
            Thread-2
            Thread-0
            Thread-1
        */
    }
}

class Worker extends Thread { // Look Here

    @Override
    public void run() {
        try {
            String threadName = Thread.currentThread().getName();
            System.out.println(threadName);
            Thread.sleep(1000);
            System.out.println(threadName);
            Thread.sleep(1000);
        } catch (InterruptedException ex) {}
    }
}
~~~

### 3. Implementing Runnable

Java supports single inheritance (you can extend only one class) so interface implementation allows better design and reuse. Runnable represents a task that can be executed by a thread. It separates what should be executed (Runnable) from how it is executed (Thread), making code more flexible and reusable. Because Runnable is a functional interface, it can be implemented using anonymous classes or lambda expressions.

~~~java
/**
 * Creating threads by implementing Runnable.
 * 
 * Real-life analogy:
 *  - Imagine a restaurant 
 *  - Each customer order can be prepared independently
 *  - The chef (main program) doesn't cook everything personally
 *  - Instead, the chef hands tasks to kitchen workers who work in parallel
 * 
 * In Java:
 *  - The task = Runnable
 *  - The worker = Thread
 * 
 * This shows true concurrency - orders are prepared in parallel, not sequentially.  
 */

package threads.creating_threads;

public class Implementing {  // Restaurant App
    public static void main(String[] args) {

        Runnable order1 = new OrderPreparation("Pizza");
        Runnable order2 = new OrderPreparation("Pasta");        
        Runnable order3 = new OrderPreparation("Salad");  
        
        Thread worker1 = new Thread(order1, "Chef-1");
        Thread worker2 = new Thread(order2, "Chef-2");
        Thread worker3 = new Thread(order3, "Chef-3");

        worker1.start();
        worker2.start();
        worker3.start();

        /*
            Output (order may vary):
            ========================
            Chef-2 is preparing Pasta
            Chef-1 is preparing Pizza
            Chef-3 is preparing Salad
            Chef-2 finished Pasta
            Chef-3 finished Salad
            Chef-1 finished Pizza
        */
    } 
}

class OrderPreparation implements Runnable {

    private final String orderName;
    public OrderPreparation(String orderName) {
        this.orderName = orderName;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        
        System.out.println(threadName + " is preparing " + orderName);
        try {
            // Simulate time needed to prepare the order
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println(threadName + " finished " + orderName);
    }
}
~~~


