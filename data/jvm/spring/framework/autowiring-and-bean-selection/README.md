## JVM / Spring / Framework / Autowiring and Bean Selection

### 1. @Autowired Basics

`@Autowired` tells Spring to find a matching bean by type and inject it automatically. It can go on a constructor (recommended), a field, or a setter. When exactly one bean of that type exists, Spring resolves it with no extra config needed.

~~~java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
class MessageService {
    String getMessage() { return "Hello, autowired!"; }
}

@Component
class NotificationController {

    private final MessageService messageService;

    @Autowired // optional on a single constructor since Spring 4.3, shown here for clarity
    NotificationController(MessageService messageService) {
        this.messageService = messageService;
    }

    void print() { System.out.println(messageService.getMessage()); }
}

@Configuration
@ComponentScan(basePackages = "autowiring_example")
class AppConfig {}

public class AutowiredBasicsExample {
    public static void main(String[] args) {
        var context = new org.springframework.context.annotation.AnnotationConfigApplicationContext(AppConfig.class);
        context.getBean(NotificationController.class).print();
        // Hello, autowired!
    }
}
~~~


### 2. Resolving Ambiguity with @Qualifier

When two+ beans implement the same type, plain `@Autowired` is ambiguous -- Spring throws `NoUniqueBeanDefinitionException` at startup. `@Qualifier("beanName")` fixes this by naming exactly which bean to inject, using either its default name (lowercased class name) or an explicit one.

~~~java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

interface PaymentGateway {
    void charge(int amount);
}

@Component("stripeGateway") // explicit bean name
class StripeGateway implements PaymentGateway {
    public void charge(int amount) { System.out.println("Stripe charged " + amount); }
}

@Component("payPalGateway")
class PayPalGateway implements PaymentGateway {
    public void charge(int amount) { System.out.println("PayPal charged " + amount); }
}

@Component
class CheckoutService {
    private final PaymentGateway gateway;

    @Autowired
    CheckoutService(@Qualifier("stripeGateway") PaymentGateway gateway) { // picks one candidate
        this.gateway = gateway;
    }

    void checkout(int amount) { gateway.charge(amount); }
}
// Stripe charged 100
~~~


### 3. @Primary as a Default Candidate

`@Primary` marks one implementation as the default pick whenever autowiring by type is ambiguous -- no need to `@Qualifier` every injection point. If both are present, `@Qualifier` wins. Use `@Primary` for the usual choice, `@Qualifier` for the occasional exception.

~~~java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

interface Notifier {
    void send(String message);
}

@Component
@Primary // the default when Notifier is autowired with no @Qualifier
class EmailNotifier implements Notifier {
    public void send(String message) { System.out.println("Email: " + message); }
}

@Component
class SmsNotifier implements Notifier {
    public void send(String message) { System.out.println("SMS: " + message); }
}

@Component
class AlertService {
    private final Notifier notifier;

    @Autowired
    AlertService(Notifier notifier) { // resolves to EmailNotifier via @Primary
        this.notifier = notifier;
    }

    void alert(String message) { notifier.send(message); }
}
// Email: Server down
~~~
