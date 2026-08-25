# Architecture / Architecture / SOLID / Interface Segregation Principle

## 1. Interface Segregation Principle (ISP)

The Interface Segregation Principle states that clients should not be forced to depend on methods they do not use. Instead of creating one large interface, split it into smaller, focused interfaces that represent specific capabilities.

```java
interface Refundable {
    void refund(String id, double amount);
}

interface Invoicable {
    void generateInvoice(String id);
}
```


## 2. Bad Implementation (violates ISP)

A design violates ISP when a single interface forces implementations to provide methods that are meaningless for them. This often results in empty methods or UnsupportedOperationException, making the code harder to maintain.

```java
package solid.isp.non_compliant;

public class BadProviderApplication {
    public static void main(String[] args) {

        PaymentProvider provider = new StripePaymentProvider();
        provider.authorizePayment(100);
        provider.capturePayment("7f3a2b1c-4d5e-678f-abcdef345678");
        provider.refund("9HX23456AB1234567", 50);
        provider.generateInvoice("9HX23456AB1234567");

        /*
            Stripe: authorizing payment
            Stripe: capturing payment
            Stripe: refunding payment
            Stripe: generating invoice
        */
    }
}

interface PaymentProvider {
    void authorizePayment(double amount);
    void capturePayment(String autorizationId);
    void refund(String transactionId, double amount);
    void generateInvoice(String transactionId);
}

// First implementation: Stripe (supports everything)
class StripePaymentProvider implements PaymentProvider {

    @Override
    public void authorizePayment(double amount) {
        System.out.println("Stripe: authorizing payment");
    }

    @Override
    public void capturePayment(String authorizationId) {
        System.out.println("Stripe: capturing payment");
    }

    @Override
    public void refund(String transactionId, double amount) {
        System.out.println("Stripe: refunding payment");
    }

    @Override
    public void generateInvoice(String transactionId) {
        System.out.println("Stripe: generating invoice");
    }
}

// Second implementation: Cash On Delivery (very common)
class CashOnDeliveryPaymentProvider implements PaymentProvider {

    @Override
    public void authorizePayment(double amount) {
        // ❌ no authorization for cash
        throw new UnsupportedOperationException();
    }

    @Override
    public void capturePayment(String authorizationId) {
        // ❌ meaningless
        throw new UnsupportedOperationException();
    }

    @Override
    public void refund(String transactionId, double amount) {
        System.out.println("Cash refund handled manually");
    }

    @Override
    public void generateInvoice(String transactionId) {
        System.out.println("Invoice generated for cash payment");
    }
}
```

## 3. ISP-compliant Implementation

A better design separates unrelated capabilities into independent interfaces. Each implementation chooses only the interfaces that make sense, while clients depend only on the capabilities they actually require.

```java
package solid.isp;

public class ProviderApplication {
    public static void main(String[] args) {

        StripePaymentProvider stripe = new StripePaymentProvider();
        CheckoutService checkout = new CheckoutService(stripe, stripe);
        checkout.checkout(100);
        stripe.refund("9HX23456AB1234567", 50);
        stripe.generateInvoice("9HX23456AB1234567");

        CashOnDeliveryPaymentProvider cash = new CashOnDeliveryPaymentProvider();
        cash.refund("9HX23456AB1234567890", 20);
        cash.generateInvoice("9HX23456AB1234567890");

        /*
            Stripe: authorizing payment
            Stripe: capturing payment
            Stripe: refunding payment
            Stripe: generating invoice
            Cash: refund handled manually
            Cash: Invoice generated
        */
    }
}

interface PaymentAuthorizer {
    void authorizePayment(double amount);
}

interface PaymentCapturer {
    void capturePayment(String authorizationId);
}

interface Refundable {
    void refund(String transactionId, double amount);
}

interface Invoicable {
    void generateInvoice(String transactionId);
}

// Stripe supports everything
class StripePaymentProvider 
        implements PaymentAuthorizer, 
                   PaymentCapturer, 
                   Refundable, 
                   Invoicable {

    @Override
    public void authorizePayment(double amount) {
        System.out.println("Stripe: authorizing payment");
    }

    @Override
    public void capturePayment(String authorizationId) {
        System.out.println("Stripe: capturing payment");
    }

    @Override
    public void refund(String transactionId, double amount) {
        System.out.println("Stripe: refunding payment");
    }

    @Override
    public void generateInvoice(String transactionId) {
        System.out.println("Stripe: generating invoice");
    }
}

// Cash on Delivery supports only what make sense
class CashOnDeliveryPaymentProvider
        implements Refundable, 
                   Invoicable {

    @Override
    public void refund(String transactionId, double amount) {
        System.out.println("Cash: refund handled manually");
    }

    @Override
    public void generateInvoice(String transactionId) {
        System.out.println("Cash: invoice generated");
    }
}

// Client code now depends on what it actually needs
class CheckoutService {
    private final PaymentAuthorizer authorizer;
    private final PaymentCapturer capturer;

    public CheckoutService(
            PaymentAuthorizer authorizer, 
            PaymentCapturer capturer
    ) {
        this.authorizer = authorizer;
        this.capturer = capturer;
    }

    public void checkout(double amount) {
        authorizer.authorizePayment(amount);
        capturer.capturePayment("auth-id");
    }
}
```

## 4. Spring Projects (ISP)

In Spring applications, ISP naturally works with dependency injection. A single bean can implement multiple interfaces, and each service receives only the capability it needs rather than depending on the entire implementation.

```java
@Component
public class StripePaymentProvider
        implements PaymentAuthorizer,
                   PaymentCapturer,
                   Refundable,
                   Invoicable {
}

@Service
public class CheckoutService {

    private final PaymentAuthorizer authorizer;
    private final PaymentCapturer capturer;

    public CheckoutService(
            PaymentAuthorizer authorizer,
            PaymentCapturer capturer) {
        this.authorizer = authorizer;
        this.capturer = capturer;
    }
}
```

Spring will inject the same bean instance (StripePaymentProvider)  
into different constructor parameters by role, not by class.  



## 5. Crucial Conceptual Point (ISP)

ISP is about clients, not objects. A single object may implement multiple interfaces, but each client should depend only on the capability it actually uses. This keeps dependencies focused and reduces unnecessary coupling.

```java
PaymentAuthorizer authorizer = new StripePaymentProvider();
authorizer.authorizePayment(100);
```
