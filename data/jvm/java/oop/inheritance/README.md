## JVM / Java / OOP / Inheritance 

### 1. Payment Processing System

“Prefer composition and interfaces over inheritance” is good advice in many situations. However, inheritance is still appropriate when there is a genuine is-a relationship, shared state, and common behavior that should be reused by multiple subclasses. 

~~~java
// Different payment methods share the same general workflow: 
// validate the payment, execute it, and send a receipt.

package com.minte9.oop.inheritance;

public class PaymentApp {
    public static void main(String[] args) {
        Payment p1 = new CreditCardPayment(100);
        Payment p2 = new PayPalPayment(200);

        p1.processPayment();
        p2.processPayment();

        /*
            Validating payment of $100.0
            Processing credit card payment...
            Sending receipt...

            Validating payment of $200.0
            Processing PayPal payment...
            Sending receipt...
        */
    }
}

// Superclass
abstract class Payment {
    protected double amount;

    public Payment(double amount) {
        this.amount = amount;
    }

    public void processPayment() {
        validate();
        execute();  // subclass-specific
        sendReceipt();
    }

    public void validate() {
        System.out.println("Validating payment of $" + amount);
    }

    protected abstract void execute();  // Look Here (must be implemented)

    public void sendReceipt() {
        System.out.println("Sending receipt...\n");
    }
}

// Subclass 1
class CreditCardPayment extends Payment {

    public CreditCardPayment(double amount) {
        super(amount);
    }

    @Override
    protected void execute() {
        System.out.println("Processing credit card payment...");
    }
}

// Subclass 2
class PayPalPayment extends Payment {

    public PayPalPayment(double amount) {
        super(amount);
    }

    @Override
    protected void execute() {
        System.out.println("Processing PayPal payment...");
    }
}
~~~


### 2. Why inheritance is the right choise here?

Inheritance is appropriate because CreditCardPayment and PayPalPayment are specialized types of Payment. They share the same amount field, validation logic, receipt logic, and processing sequence.

~~~java
Payment p1 = new CreditCardPayment(100); 
Payment p2 = new PayPalPayment(200);

// The superclass controls the complete algorithm, while subclasses 
// customize only the step that differs.

public final void processPayment() { 
    validate(); 
    execute(); 
    sendReceipt(); 
}
~~~


### 3. Why not use interfaces?

An interface can define required behavior, but it does not naturally provide shared instance state such as amount. Using only an interface may also cause each implementation to repeat the same validation and receipt logic.

~~~java
// You could use interface:

interface Payment() {
    void process();
}

// But then you lose shared state (amount), duplicate workflow logic
// and you can't enforce the sequence (validate -> execute -> receipt)
// Better:

abstract class Payment { 
    protected double amount; 
    public final void processPayment() { 
       validate(); 
       execute(); 
       sendReceipt(); 
    }
}
~~~

### 4. Rule of thumb

Use inheritance when classes form a true hierarchy, share state and behavior, and differ only in specific parts of an algorithm. Inheritance should represent specialization rather than simply being used for code reuse. Use interfaces when defining capabilities or roles that unrelated classes may implement. Common examples include Runnable, Comparable, and Serializable.

~~~java
class CreditCardPayment extends Payment {
    // CreditCardPayment is a Payment
}

class PaymentTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Processing payment...");
    }
}
~~~

### 5. Payment system (done wrong)

Using only an interface in this example causes every payment implementation to repeat the same fields and workflow logic. This increases maintenance costs and makes inconsistencies more likely. Nothing forces every implementation to follow the correct sequence. A developer could accidentally execute the payment before validation or forget to send the receipt.

~~~java
public class PaymentWrongApp {
    public static void main(String[] args) {
        Payment p1 = new CreditCardPayment(100);
        Payment p2 = new PayPalPayment(200);

        p1.processPayment();
        p2.processPayment();

        /*
            Validating $100.0
            Processing credit card...
            Sending receipt...

            Validating $200.0
            Processing PayPal...
            Sending receipt...
        */
    }
}

interface Payment  {
    void processPayment();    
}

class CreditCardPayment implements Payment {
    private double amount;

    public CreditCardPayment(double amount) {
        this.amount = amount;
    }

    @Override
    public void processPayment() {
        System.out.println("Validating $" + amount);
        System.out.println("Processing credit card...");
        System.out.println("Sending receipt...\n");
    }
}

class PayPalPayment implements Payment {
    private double amount;

    public PayPalPayment(double amount) {
        this.amount = amount;
    }

    @Override
    public void processPayment() {
        System.out.println("Validating $" + amount);
        System.out.println("Processing PayPal...");
        System.out.println("Sending receipt...\n");
    }
}
~~~

### 6. Abstract Keyword

An abstract class represents an incomplete concept that should not be instantiated directly. It may contain fields, constructors, concrete methods, and abstract methods that subclasses are required to implement. A concrete subclass provides the missing implementation and can then be instantiated normally.

~~~java
public class AbstractKeyword {

    public static void main(String[] args) {

        CreditCardPayment payment = new CreditCardPayment();
        
        payment.setAmount(250);
        payment.processPayment();  // Processing credit card payment of $250.0
    }
}

abstract class Payment {
    protected double amount;

    public abstract void processPayment();  // abstract (must be implemented)

    public void setAmount(double amount) {  // non-abstract method (shared behavior)
        this.amount = amount;
    }
}

class CreditCardPayment extends Payment {

    @Override
    public void processPayment() {
        System.out.println("Processing credit card payment of $" + amount);
    }
}
~~~


### 7. Override Annotation

The @Override annotation tells the compiler that a method is intended to replace an inherited method. It detects spelling mistakes, incorrect parameters, and incompatible return types during compilation. Without @Override, a misspelled method may be treated as a completely new method rather than an override. Using the annotation consistently makes inheritance safer and easier to maintain.

~~~java
package com.minte9.oop.inheritance.override_note;

public class OverrideAnnotation {

    public static void main(String[] args) {
        
        CreditCardPayment payment = new CreditCardPayment();
        payment.processPayment();  // Wrong method called!
    }
}

abstract class Payment {
    public abstract void processPayment();
}

class CreditCardPayment extends Payment {

    // Typo: processsPayment instead of processPayment
    // No @Override annotation to protect us
    public void processsPayment() {
        System.out.println("Processing credit card payment...");
    }

    // This method was auto-generated later just to satisfy compilation
    @Override
    public void processPayment() {
        System.out.println("Wrong method called!");
    }
}
~~~
