## JVM / Java / Concurrency / Theory

### 1. What is a Thread?

A thread is the smallest unit of execution within a process. Every Java application starts with a single main thread, and additional threads can be created to perform work concurrently. Threads within the same process share memory but each has its own call stack and execution path. Calling start() creates a new thread but execution begin at run().

~~~java
package threads.theory;

public class MainThread {
    public static void main(String[] args) {
    
        // main thread
        String name = Thread.currentThread().getName();
        System.out.println(name); // main

        // other threads
        Thread w1 = new Worker();
        Thread w2 = new Worker();
        w1.start();  // Thread-0
        w2.start();  // Thread-1
    }    
}

class Worker extends Thread {
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());
    }
}
~~~

### 2. When to use Threads

Threads are useful when multiple tasks can make progress at the same time. They are especially beneficial for operations that spend time waiting (such as I/O), for independent tasks that can execute concurrently, and for background work that should not block the main application. 

~~~java
// Use threads when the task waits a lot like waiting for network, 
// database, files, APIs, messages.

callBankApi();
readLargeFile();
processRabbitMqMessage();
queryDatabase();

// Use threads when work can run independently, but only if 
// shared data is protected correctly. Each transaction can be 
// processed independently.

processTransaction(tx1);
processTransaction(tx2);
processTransaction(tx3);

synchronized (account) {
    account.withdraw(amount);
}

// Use threads for background, the user should not wait unnecessary.  
// Do not block the main application.

sendEmail();
generateReport();
writeAuditLog();
~~~

### 3. When to NOT use threads

Although threads enable concurrency, they are not the solution to every problem. They add complexity and require careful synchronization when data is shared. In many applications, especially server-side Java, thread pools managed by frameworks are preferred over creating threads manually.

~~~java
// Do not use threads when code must happen in order.  
// This sequence has strict order, threads may make it harder and unsafe. 
 
validateOrder();
chargeCard();
shipProduct();

// Do not use raw threads for many tasks.  
// For real application prefer ExecutorService, because 
// ExecutorService controls how many threads are created.  

new Thread(task).start();  // NOK
new Thread(task).start();
new Thread(task).start();

ExecutorService executor = Executors.newFixedThreadPool(10);  // OK
executor.submit(task);

// For backend Java/Spring Boot, you usually do not manually create threads.  
Servers like Tomcat already use thread pools to handle HTTP requests.  

You use threads more directly for background jobs, message consumers, 
batch processing, or stress-test clients.

