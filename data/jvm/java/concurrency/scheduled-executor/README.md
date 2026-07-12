## JVM / Java / Concurrency / Scheduled Executor

### ScheduledExecutorService

ScheduledExecutor is a `specialized` ExecutorService that can:

- Run tasks after a delay
- Run tasks periodically (fixed rate or fixed delay)

Before `Java 5`, developers used Timer / TimerTask.  
There was a single thread and one bad task could kill the timer.  

SchedulerExecutorService `fixed` all of this.  
Think of it as an Executor + timer + thread pool.

~~~java
/**
 * =================================
 * Trading Background Jobs
 * ==================================
 * ScheduledExecutorService - Example
 */

package threads.scheduled_executor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TradingBackgroundJobs {
    public static void main(String[] args) throws InterruptedException {
        
        ScheduledExecutorService scheduler = 
            Executors.newScheduledThreadPool(
                2,
                new SchedulerThreadFactory()
            );

        // 1. Market data refresh - fixed rate
        scheduler.scheduleAtFixedRate(
            new MarketDataRefresher(),      // command
            0,                              // initial delay
            1,                              // period
            TimeUnit.SECONDS                // unit
        );

        // 2. Risk checks - fixed delay
        scheduler.scheduleWithFixedDelay(
            new RiskCheckTask(),
            0,
            5,
            TimeUnit.SECONDS
        );

        // 3. One-time cleanup after delay
        scheduler.schedule(
            new ExpiredOrderCleanup(), 
            10, 
            TimeUnit.SECONDS
        );

        // Let the system run for a while
        Thread.sleep(20_000);

        System.out.println("Shutting down scheduler ...");
        scheduler.shutdown();

        /*
            [scheduler-worker-1] Refreshing market price ...
            [scheduler-worker-2] Running risk checks ...
            [scheduler-worker-1] Refreshing market price ...
            [scheduler-worker-1] Refreshing market price ...
            [scheduler-worker-2] Risk checks completed

            [scheduler-worker-1] Refreshing market price ...
            [scheduler-worker-1] Refreshing market price ...
            [scheduler-worker-1] Refreshing market price ...
            [scheduler-worker-1] Refreshing market price ...
            [scheduler-worker-1] Refreshing market price ...
            [scheduler-worker-2] Running risk checks ...

            [scheduler-worker-1] Refreshing market price ...
            [scheduler-worker-1] Refreshing market price ...
            [scheduler-worker-2] Risk checks completed

            [scheduler-worker-2] Refreshing market price ...
            [scheduler-worker-1] Cleaning up expired orders ...
            [scheduler-worker-2] Refreshing market price ...
            [scheduler-worker-1] Cleanup completed

            [scheduler-worker-2] Refreshing market price ...
            [scheduler-worker-2] Refreshing market price ...
            [scheduler-worker-2] Refreshing market price ...
            [scheduler-worker-1] Running risk checks ...

            [scheduler-worker-2] Refreshing market price ...
            [scheduler-worker-2] Refreshing market price ...
            [scheduler-worker-1] Risk checks completed
            
            [scheduler-worker-1] Refreshing market price ...
            [scheduler-worker-1] Refreshing market price ...
            [scheduler-worker-1] Refreshing market price ...
            Shutting down scheduler ...
            [scheduler-worker-1] Refreshing market price ...
        */
    }
}

class SchedulerThreadFactory implements ThreadFactory {
    
    private final AtomicInteger counter = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName("scheduler-worker-" + counter.getAndIncrement());
        return t;
    }
}

/** 
 * ===================================
 * Market Data - Refresher
 * ===================================
 *  - runs at a fixed rate
 *  - exception-safe
 *  - typical low-latency trading task
 */
class MarketDataRefresher implements Runnable {

    @Override
    public void run() {
        try {
            System.out.printf(
                "[%s] Refreshing market price ... %n",
                Thread.currentThread().getName()
            );

            // Simulate market data fetch
            Thread.sleep(300);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // IMPORTANT: never let exceptions escape
            System.err.println("Market data refresh failed: " + e.getMessage());
        }
    }
}

/**
 * ============================================
 * Risk Check - Task
 * ============================================
 *  - No overlap
 *  - Each run starts after the previous finishes
 *  - Perfect for audits and controls
 */
class RiskCheckTask implements Runnable {

    @Override
    public void run() {
        try {
            System.out.printf(
                "[%s] Running risk checks ... %n",
                Thread.currentThread().getName()
            );

            // Simulate heavy calculations
            Thread.sleep(2000);

            System.out.printf(
                "[%s] Risk checks completed %n",
                Thread.currentThread().getName()
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Risk check failed: " + e.getMessage());
        }
    }
}

/**
 * Expired Order Clean
 * ==========================
 *  - Runs once
 *  - Delayed startup
 *  - Typical maintenance job
 */
class ExpiredOrderCleanup implements Runnable {

    @Override
    public void run() {
        try {
            System.out.printf(
                "[%s] Cleaning up expired orders ... %n",
                Thread.currentThread().getName()
            );

            // Simulate clean up
            Thread.sleep(1000);

            System.out.printf(
                "[%s] Cleanup completed %n",
                Thread.currentThread().getName()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Expired order cleanup failed: " + e.getMessage());
        }
    }
}
~~~
