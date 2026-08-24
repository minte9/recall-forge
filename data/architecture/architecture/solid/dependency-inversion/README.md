## Architecture / Architecture / SOLID / Dependency Inversion

### 1. Dependency Inversion Principle (DIP)

The Dependency Inversion Principle states that high-level business logic should depend on abstractions rather than concrete implementations. This keeps business rules independent of infrastructure details and makes the application easier to extend, test, and maintain.

~~~java
interface PaymentProcessor {
    void process(double amount);
}
~~~


### 2. Bad Implementation (violates DIP)

A design violates DIP when business logic directly creates or depends on a concrete implementation. This tightly couples the application to one technology and makes replacing or testing that dependency more difficult.

~~~java
package solid.dip.non_compliant;

public class BadCheckoutApplication {
    public static void main(String[] args) {
        
        CheckoutService checkoutService = new CheckoutService();
        checkoutService.checkout(100); // Charging with Stripe: 100.0
    }
}

class CheckoutService {
    private final StripePaymentProvider stripe;

    public CheckoutService() {
        this.stripe = new StripePaymentProvider();
    }

    public void checkout(double amount) {
        stripe.charge(amount);
    }
}

class StripePaymentProvider {

    public void charge(double amount) {
        System.out.println("Charging with Stripe: " + amount);
    }
}
~~~


### 3. DIP-compliant Implementation

A better design depends on an abstraction instead of a concrete class. The implementation is supplied from the outside, allowing the same business logic to work with different payment providers without modification.

~~~java
package solid.dip;

public class CheckoutApplication {
    public static void main(String[] args) {

        PaymentProcessor paymentProcessor = new StripePaymentProcessor();
        CheckoutService checkoutService = new CheckoutService(paymentProcessor);
        checkoutService.checkout(100);

        PaymentProcessor paymentProcessor2 = new PayPalPaymentProcessor();
        CheckoutService checkoutService2 = new CheckoutService(paymentProcessor2);
        checkoutService2.checkout(200);

        /*
            Charging with Stripe: 100.0
            Paying with PayPal: 200.0
        */
    }
}

interface PaymentProcessor {
    void process(double amount);
}

// Low-level details depend on the abstraction
class StripePaymentProcessor implements PaymentProcessor {

    @Override
    public void process(double amount) {
        System.out.println("Charging with Stripe: " + amount);
    }
}

class PayPalPaymentProcessor implements PaymentProcessor {

    @Override
    public void process(double amount) {
        System.out.println("Paying with PayPal: " + amount);
    }
}

// High-level module depends only on the abstraction
class CheckoutService {

    private final PaymentProcessor paymentProcessor;

    public CheckoutService(PaymentProcessor paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }

    public void checkout(double amount) {
        paymentProcessor.process(amount);
    }
}
~~~


### 4. Spring Projects (DIP)

Spring naturally supports DIP through dependency injection. Components implement interfaces, while services declare dependencies on those interfaces. The framework creates the concrete objects and injects them automatically.

~~~java
@Component
public class StripePaymentProcessor implements PaymentProcessor {
    @Override
    public void process(double amount) {
        System.out.println("Stripe payment");
    }
}

@Service
public class CheckoutService {

    private final PaymentProcessor processor;

    public CheckoutService(PaymentProcessor processor) {
        this.processor = processor;
    }
}
~~~


### 5. Important Clarification (DIP)

DIP is about the direction of dependencies, not about creating interfaces everywhere. High-level business policies define the abstractions they need, while low-level infrastructure classes implement those abstractions. This keeps the core business logic independent of implementation details.

~~~java
PaymentProcessor processor =
    new PayPalPaymentProcessor();

processor.process(100);
~~~
