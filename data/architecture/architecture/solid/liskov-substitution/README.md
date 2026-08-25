# Architecture / Architecture / SOLID / Liskov Substitution

## 1. Liskov Substitution Principle (LSP)

The Liskov Substitution Principle states that objects of a subclass should be usable wherever objects of the base type are expected. A subtype must preserve the behavior promised by the base contract and should not surprise its clients by imposing stronger restrictions or changing expected behavior.

```java
PaymentMethod payment = new CreditCardPayment();
payment.pay(100);   // Works as expected

payment = new PayPalPayment();
payment.pay(100);   // Same contract, different implementation
```


## 2. Bad Implemenation (violates LSP)

A subtype violates LSP when it cannot honor the contract defined by its base type. In this example, FreeTrialPayment introduces an additional restriction that is not part of the PaymentMethod contract, causing code that works with the base type to fail unexpectedly.

```java
/**
 * LSB - Non compliant example
 * ===========================
 */
package solid.lsp.non_compliant;

public class BadPaymentApplication {
    public static void main(String[] args) {
        
        PaymentMethod p1 = new CreditCardPayment();
        PaymentMethod p2 = new FreeTrialPayment();  // problematic subtype

        p1.pay(200);
        p2.pay(300);  // This will break at runtime
    }
}

interface PaymentMethod {
    void pay(double amount);
}

class CreditCardPayment implements PaymentMethod {
    @Override
    public void pay(double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        System.out.println("Paid $" + amount + " using Credit Card");
    }
}

/**
 * This class strengthens preconditions:
 * It does NOT allow payments above 100.
 * The base contract does not define such limitation. 
 * This violates LSP.
 */
class FreeTrialPayment implements PaymentMethod {
    @Override
    public void pay(double amount) {

        if (amount > 100) {
            throw new UnsupportedOperationException(
                "FreeTrial cannot process payments above 100");
        }

        System.out.println("Free trial payment processed: $" + amount);
    }
}
```

## 3. LSB-compliant Implementation

An LSP-compliant design ensures that every implementation behaves consistently with the base contract. Clients can work with the abstraction without needing to know which concrete implementation is being used.

```java
/**
 * LSP - Liskov Substitution Principle
 * ====================================
 * Subtypes must be substituable for their base types.
 * All payment types behave consistently. 
 */
package solid.lsp;

public class PaymentApplication {
    public static void main(String[] args) {
        PaymentMethod p1 = new CreditCardPayment();
        PaymentMethod p2 = new PayPalPayment();

        p1.pay(200);
        p2.pay(300);
    }
}

interface PaymentMethod {
    void pay(double amount);
}

abstract class BasePayment implements PaymentMethod {
    protected void validate(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
    }
}

class CreditCardPayment extends BasePayment {
    @Override
    public void pay(double amount) {
        validate(amount);
        System.out.println("Paid $" + amount + " using Credit Card");
    }
}

class PayPalPayment extends BasePayment {
    @Override
    public void pay(double amount) {
        validate(amount);
        System.out.println("Paid $" + amount + " using PayPal");
    }
}
```
