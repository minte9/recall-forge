
# JVM / Java / Concurrency / Synchonization

## 1. Race condition

A race condition occurs when the outcome depends on the `timing of` threads.  

~~~java
/**
 * Two or more threads may have access to a single object's data.
 * Not good.
 */

package threads.synchronization;

public class RaceCondition {    
    public static void main(String[] args) {
    
        ConcurentRunner runner = new ConcurentRunner();

        Thread a = new Thread(runner, "Alpha");
        Thread b = new Thread(runner, "Beta");
        a.start();
        b.start();
    }    
}

class ConcurentRunner implements Runnable {
    private int balance = 100;

    @Override 
    public void run() {
        withdraw();
    }

    private void withdraw() {
        String threadName = Thread.currentThread().getName();

        System.out.println(threadName + " sees balance : " + balance); 
        balance -= 80;  // NOT atomic

        System.out.println(threadName + " withdraws 80, new balance = " + balance);
    }
}

/* 
    Beta sees balance : 100
    Alpha sees balance : 100
    Beta withdraws 80, new balance = 20
    Alpha withdraws 80, new balance = -60
*/
~~~

## 2. Locking

A lock is a mechanism that allows `one thread` at a time to execute a protected block of code.  
All other threads must wait until the lock is `released`.  

Synchronized is Java's built-in locking mechanism.  

~~~java
package threads.synchronization;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BankingSystem {
    public static void main(String[] args) {

        ExecutorService executor = Executors.newFixedThreadPool(
            2, new BankingThreadFactory()
        );

        // Simulate concurrent withdrawals
        Account account = new Account(1000);
        executor.submit(new WithdrawTask(account, 400));
        executor.submit(new WithdrawTask(account, 300));
        executor.submit(new WithdrawTask(account, 500));
        executor.submit(new WithdrawTask(account, 200));
        executor.shutdown();

        /*
            Output (order vary):
            ====================
            [bank-worker-2] TX-1 withdraw 300 / Balance before = 1000
            [bank-worker-2] TX-1 completed / Balance after = 700
            [bank-worker-1] TX-1 withdraw 400 / Balance before = 700
            [bank-worker-1] TX-1 completed / Balance after = 300
            [bank-worker-2] TX-1 FAILED / Insufficient funds, balance=300
            [bank-worker-1] TX-1 withdraw 200 / Balance before = 300
            [bank-worker-1] TX-1 completed / Balance after = 100
        */
    }
}

class Account {
    private int balance;

    public Account(int initialBalance) {
        this.balance = initialBalance;
    }

    public void withdraw(int amount, AtomicLong tradeId) {
       synchronized (this) {  // Look Here
            String threadName = Thread.currentThread().getName();

            if (balance >= amount) {
                System.out.printf("
	                [%s] TX-%s withdraw %d / Balance before = %d %n",
                    threadName, tradeId, amount, balance);

                balance -= amount;

                System.out.printf("[%s] TX-%s completed / Balance after = %d %n",
                    threadName, tradeId, balance);

            } else {
                System.out.printf(
	                "[%s] TX-%s FAILED / Insufficient funds, balance=%d %n",
                    threadName, tradeId, balance);
            }
        }
    }
}

class BankingThreadFactory implements ThreadFactory {
    private final AtomicInteger counter = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName("bank-worker-" + counter.getAndIncrement());
        return t;
    }
}

class WithdrawTask implements Runnable {

    private final AtomicLong tradeId = new AtomicLong(1);
    private final Account account;
    private final int amount;

    public WithdrawTask(Account account, int amount) {
        this.account = account;
        this.amount = amount;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        
        // Simulate processing delay (validation, logging)
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.printf("[%s] Trade %d interrupted %n", threadName, tradeId);
            return;
        }

        account.withdraw(amount, tradeId);
    }
}
~~~