# JVM / Spring / Framework / Dependency Injection

## 1. Dependency Injection (DI)

Dependency Injection (DI) means an object receives its dependencies from outside instead of creating them itself. This lowers coupling and makes testing easier, since you can swap in a fake dependency. In Spring, DI is how IoC actually gets implemented.

```java
class NotificationController {
    private final MessageService service;

    public NotificationController() {
        this.service = new MessageService(); // tightly coupled, hard to swap or test
    }
}

class NotificationControllerInjected {
    private final MessageService service;

    // dependency is REQUIRED and immutable
    public NotificationControllerInjected(MessageService service) { // injected from outside
        this.service = service;
    }
}
```


## 2. Types of DI

Spring supports constructor, field, and setter injection. Constructor injection is recommended: it makes dependencies explicit, allows `final` fields, and is easy to test. Field injection (`@Autowired` on a field) is more common in tutorials but hides dependencies and complicates testing.

```java
class MessageService {
    public String getMessage() {
        return "Hello DI";
    }
}

class NotificationController {
    private final MessageService messageService; // final: only settable once, via constructor

    public NotificationController(MessageService messageService) {
        this.messageService = messageService;
    }

    public void print() {
        System.out.println(messageService.getMessage());
    }
}
```


## 3. IoC using DI

Spring ties IoC and DI together through the `ApplicationContext`: it creates beans, figures out what each one needs, and injects those dependencies automatically. Your classes just declare what they need in the constructor -- Spring supplies it.

```java
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

// @Service marks a Spring-managed component.
// @ComponentScan tells Spring where to look for them.
// OrderService depends on PaymentService -- Spring injects it automatically.

public class DependencyInjectionApp {
    public static void main(String[] args) {

        ApplicationContext context =
            new AnnotationConfigApplicationContext(AppConfig.class);

        OrderService service = context.getBean(OrderService.class);
        service.processOrder();
            // Payment executed ...
            // Order processed.
    }
}

@Configuration
@ComponentScan(basePackages = "dependency_injection")
class AppConfig {
}

@Service
class PaymentService {
    public void pay() {
        System.out.println("Payment executed ...");
    }
}

@Service
class OrderService {
    private final PaymentService paymentService;

    public OrderService(PaymentService paymentService) { // Spring finds and injects this
        this.paymentService = paymentService;
    }

    public void processOrder() {
        paymentService.pay();
        System.out.println("Order processed.");
    }
}
```
