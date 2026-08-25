# JVM / Spring / Framework / Spring AOP

## 1. Cross-Cutting Concerns and @Aspect

A cross-cutting concern is behavior needed across many unrelated methods -- logging, security, transactions -- that would otherwise be duplicated everywhere. AOP defines it once in an `@Aspect` class, applied automatically to any method matching a "pointcut" expression, keeping business logic free of that boilerplate.

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@Aspect
@Component
class LoggingAspect {

    // Pointcut: matches every method in the service package
    @Before("execution(* com.example.service.*.*(..))")
    void logBeforeServiceMethods() {
        System.out.println("Entering a service method");
    }
}

@Component
class OrderService {
    void placeOrder() {
        System.out.println("Order placed");
    }
}

@EnableAspectJAutoProxy // turns on AOP proxying for the application context
class AopConfig {}
```


## 2. @Before, @After, @Around

`@Before` runs before the method; `@After` runs once it finishes, success or not (`@AfterReturning`/`@AfterThrowing` cover those cases specifically). `@Around` is the most powerful: it wraps the whole call, letting you inspect/modify args or the result -- but it must call `proceed()` or the real method never runs.

```java
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
class TimingAspect {

    @Around("execution(* com.example.service.*.*(..))")
    Object timeMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        Object result = joinPoint.proceed(); // must call this, or the method never runs
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        System.out.println(joinPoint.getSignature() + " took " + elapsedMs + "ms");
        return result;
    }
}
```


## 3. Proxy-Based Mechanism and Its Limits

Spring AOP wraps the target bean in a runtime proxy (JDK dynamic proxy, or CGLIB if there's no interface), routing external calls through it. Calls from *inside* the same class (`this.otherMethod()`) skip the proxy entirely -- so `@Around`/`@Before` advice silently won't fire for those. A classic AOP gotcha.

```java
import org.springframework.stereotype.Component;

@Component
class ReportService {

    void generateReport() {
        // Calling save() directly on "this" bypasses the AOP proxy --
        // any @Before/@Around advice targeting save() will NOT run here.
        save();
    }

    void save() {
        System.out.println("Report saved");
    }
}

// Fix: call save() through the proxy (e.g. inject the bean into itself),
// or move save() into a separate, independently-injected bean.
```
