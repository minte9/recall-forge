## JVM / Java / Concurrency / Deadlock

### 1. Deadlock

A deadlock occurs when two or more threads wait indefinitely for resources held by each other. The application does not crash, but the affected threads can no longer continue. Deadlocks commonly happen when multiple locks are acquired in an inconsistent order.

~~~java
/**
 * Deadlock Example
 * ================
 * Two threads lock resources in different order.  
 * This creates a circular wait - DEADLOCK 
 * 
 * Thread A locks Account 1
 * Thread B locks Account 2
 * 
 * Now:
 * A wants Account 2 -> waits for B
 * B wants Account 1 -> waits for A
 */

public class DeadlockExample {
    
    public static final Object account1 = new Object();
    public static final Object account2 = new Object();

    public static void main(String[] args) {

        Runnable t1 = () -> {
            synchronized (account1) {
                System.out.println("Thread 1: locked account1"); 
                sleep(100);

                System.out.println("Thread 1: waiting for account2");
                synchronized(account2) {
                    System.out.println("Thread 1: locked account2");
                }
            }
        };

        Runnable t2 = () -> {
            // acquire locks in the WRONG order, different from t1 (deadlock)
            synchronized (account2) {
                System.out.println("Thread 2: locked account1"); 
                sleep(100);

                System.out.println("Thread 2: waiting for account2");
                synchronized(account1) {
                    System.out.println("Thread 2: locked account2");
                }
            }
        };

        Thread thread1 = new Thread(t1, "T1");
        Thread thread2 = new Thread(t2, "T2");
        thread1.start();
        thread2.start();

        /*
            Thread 1: locked account1
            Thread 2: locked account1
            Thread 1: waiting for account2
            Thread 2: waiting for account2

            ... and then nothing (program freezes)
        */
    }

    private static void sleep(int miliseconds) {
        try { 
            Thread.sleep(miliseconds); 
        } catch (InterruptedException e) {}
    } 
}   
~~~


### 2. FX system (example)

A common way to prevent deadlocks is to acquire multiple locks in a consistent order. In a financial transfer system, accounts can be locked by account ID so every thread follows the same sequence. This removes the circular wait condition that causes deadlocks.

~~~java
/**
 * Deadlock-safe bank transfer
 * ===========================
 * Problem:
 *  - Transfer needs to lock TWO accounts.
 *  - If two threads lock accounts in different order, deadlock can happen. 
 * 
 * Solution:
 *  - Always lock accounts in a consistent order.
 *  - Here we lock by account ID: smaller first, bigger second.
 */

public class FxSystem {
    public static void main(String[] args) {
        
        Account accountA = new Account(1, 1000);
        Account accountB = new Account(2, 1000);

        BankService bankService = new BankService();

        Thread t1 = new Thread(() -> {
            bankService.transfer(accountA, accountB, 100);
        });

        Thread t2 = new Thread(() -> {
            bankService.transfer(accountB, accountA, 200);
        });

        t1.start();
        t2.start();

        try {
            t1.join();  // wait for this thread to finish before continuing
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Account A balance: " + accountA.getBalance());
        System.out.println("Account B balance: " + accountB.getBalance());

        /*
            Transferred 100 from account 1 to account 2 
            Transferred 200 from account 2 to account 1 
            Account A balance: 1100
            Account B balance: 900
        */
    }
}

class BankService {

    public void transfer(Account from, Account to, int amount) {

        Account firstLock;
        Account secondLock;

        // Always choose the same lock order
        if (from.getId() < to.getId()) {
            firstLock = from;
            secondLock = to;
        } else {
            firstLock = to;
            secondLock = from;
        }

        synchronized (firstLock) {
            synchronized (secondLock) {

                if (from.getBalance() < amount) {
                    System.out.println(
                        "Not enough money in account " + from.getId());
                    return;
                }

                from.withdraw(amount);
                to.deposit(amount);

                System.out.printf(
                    "Transferred %d from account %d to account %d %n",
                    amount, from.getId(), to.getId()
                );
            }
        }
    }
}

class Account {
    private final int id;
    private int balance;

    public Account(int id, int balance) {
        this.id = id;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public int getBalance() {
        return balance;
    }

    public void withdraw(int amount) {
        balance -= amount;  // DB transaction in real-systems
    }

    public void deposit(int amount) {
        balance += amount;
    }
}
~~~
