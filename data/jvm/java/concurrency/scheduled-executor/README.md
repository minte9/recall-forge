## JVM / Java / Concurrency / Scheduled Executor

### 1. ScheduledExecutorService

ScheduledExecutorService runs tasks after a delay or on a repeating schedule, backed by a thread pool instead of the single thread Timer used before Java 5 — so one failing task can't kill the scheduler. scheduleAtFixedRate() re-triggers on a fixed cadence; scheduleWithFixedDelay() waits for one run to finish before starting the next.

~~~java
ScheduledExecutorService s = Executors.newScheduledThreadPool(2);

// runs every 1s, regardless of how long each run takes
s.scheduleAtFixedRate(() -> refreshPrices(), 0, 1, TimeUnit.SECONDS);

// next run starts 5s after the previous one finished
s.scheduleWithFixedDelay(() -> runRiskChecks(), 0, 5, TimeUnit.SECONDS);

// runs once, 10s from now
s.schedule(() -> cleanupExpiredOrders(), 10, TimeUnit.SECONDS);
~~~
