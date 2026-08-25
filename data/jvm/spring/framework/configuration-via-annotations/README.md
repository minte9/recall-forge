# JVM / Spring / Framework / Configuration via Annotations

## 1. Annotations

Annotations are metadata attached to code with `@`. They don't change behavior by themselves -- Spring reads them at runtime to configure beans. Common ones: `@Configuration`, `@Bean`, `@Component`, `@Service`.

```java
@Configuration // marks this class as a source of bean definitions
class AppConfig {

    @Bean // Spring calls this and manages the returned object
    MessageService messageService() {
        return new MessageService();
    }
}
```


## 2. IoC using annotations

Instead of writing `@Bean` methods by hand, Spring can scan the classpath and find components automatically. Any class annotated `@Component` becomes a managed bean, and its dependencies get injected without extra config.

```java
@Component // discovered automatically by component scanning
class MessageService {
}

@Component
class NotificationController {
    // constructor injection -- Spring finds a MessageService bean and supplies it
    public NotificationController(MessageService service) {
    }
}
```


## 3. Annotations Example App

A complete annotation-only Spring app: `@ComponentScan` finds the components, `ApplicationContext` creates the beans, and constructor injection wires them together -- no `@Bean` methods needed.

```java
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

public class AnnotationsExampleApp {
    public static void main(String[] args) {

        ApplicationContext context =
            new AnnotationConfigApplicationContext(AppConfig.class);

        NotificationController controller =
            context.getBean(NotificationController.class);

        controller.print();
            // Hello from Message Service
    }
}

@Configuration
@ComponentScan(basePackages = "annotations_example") // where to look for @Component classes
class AppConfig {
    // no @Bean methods needed -- scanning finds everything below
}

@Component
class MessageService {
    public String getMessage() {
        return "Hello from Message Service";
    }
}

@Component
class NotificationController {
    private final MessageService messageService;

    public NotificationController(MessageService messageService) { // auto-wired by Spring
        this.messageService = messageService;
    }

    public void print() {
        System.out.println(messageService.getMessage());
    }
}
```
