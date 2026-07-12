## JVM / Spring / Framework / Bean Scopes and Lifecycle

### 1. Singleton vs Prototype Scope

By default every bean is a singleton -- one shared instance for the whole app. `@Scope("prototype")` makes Spring create a new instance every time it's requested. Singletons must be stateless/thread-safe since they're shared; prototypes can hold per-use state, but Spring stops managing them after creation (no destroy callbacks).

~~~java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
class AppConfig {

    @Bean
    @Scope("singleton") // default, explicit here for clarity
    Counter sharedCounter() {
        return new Counter();
    }

    @Bean
    @Scope("prototype") // a fresh instance every time this bean is requested
    Counter freshCounter() {
        return new Counter();
    }
}

class Counter {
    private int value = 0;
    void increment() { value++; }
    int get() { return value; }
}

public class ScopeExample {
    public static void main(String[] args) {
        var context = new org.springframework.context.annotation.AnnotationConfigApplicationContext(AppConfig.class);

        Counter a = context.getBean("sharedCounter", Counter.class);
        Counter b = context.getBean("sharedCounter", Counter.class);
        a.increment();
        System.out.println(a == b);  // true -- same singleton instance
        System.out.println(b.get()); // 1   -- state is shared

        Counter c = context.getBean("freshCounter", Counter.class);
        Counter d = context.getBean("freshCounter", Counter.class);
        System.out.println(c == d);  // false -- new instance each time
    }
}
~~~


### 2. @PostConstruct and @PreDestroy

`@PostConstruct` runs right after a bean's dependencies are injected -- the standard place for init logic like opening a connection. `@PreDestroy` runs just before a singleton bean is destroyed, for cleanup like closing resources; it never fires for prototype beans. These replace the older `InitializingBean`/`DisposableBean` interfaces, which coupled your code to Spring's API.

~~~java
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class ConnectionPool {
    @PostConstruct
    void open() { // runs after dependencies are set, before the bean is used
        System.out.println("Connection pool opened");
    }

    @PreDestroy
    void close() { // runs on container shutdown, singleton beans only
        System.out.println("Connection pool closed");
    }
}

@Configuration
class AppConfig {
    @Bean
    ConnectionPool connectionPool() {
        return new ConnectionPool();
    }
}

public class LifecycleAnnotationsExample {
    public static void main(String[] args) {
        var context = new org.springframework.context.annotation.AnnotationConfigApplicationContext(AppConfig.class);

        context.close();
        // Connection pool opened
        // Connection pool closed
    }
}
~~~
