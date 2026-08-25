# JVM / Spring / Framework / ApplicationContext

## 1. ApplicationContext

`ApplicationContext` is Spring's central IoC container. It creates beans, resolves their dependencies, and manages their whole lifecycle. Instead of calling `new`, your code just asks the context for what it needs.

```java
ApplicationContext context =
    new AnnotationConfigApplicationContext(AppConfig.class);

// The container owns creation and lifecycle -- we just retrieve the bean
MyService service =
    context.getBean(MyService.class);
```


## 2. Basic Example

This example creates a context, lets it discover components via scanning, then retrieves a bean from it. Spring owns the full lifecycle of `MyService` -- creation, wiring, and later, destruction.

```java
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

public class Main {
    public static void main(String[] args) {

        AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext(AppConfig.class);

        MyService s = context.getBean(MyService.class);
        s.run(); // Running ...

        context.close(); // see topic 4
    }
}

@Configuration
@ComponentScan // scans this package for @Component classes
class AppConfig {
}

@Component
class MyService {
    public void run() {
        System.out.println("Running ...");
    }
}
```


## 3. Getting Beans

Beans can be fetched by type, by name, or both. Fetching by type (`getBean(MyService.class)`) is preferred in most code -- it's type-safe and needs no casting.

```java
MyService service =
    context.getBean(MyService.class); // preferred: type-safe, no cast

// or, by name -- useful when multiple beans share the same type
MyService service2 =
    context.getBean("myService", MyService.class);
```


## 4. Closing the Context

When the app finishes, close the context to release resources and trigger destruction callbacks. `context.close()` shuts down the container and calls any `@PreDestroy` methods on singleton beans.

```java
AnnotationConfigApplicationContext context =
    new AnnotationConfigApplicationContext(AppConfig.class);

context.close(); // triggers @PreDestroy on singleton beans, releases resources
```
