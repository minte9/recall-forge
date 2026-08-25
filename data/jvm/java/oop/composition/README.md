# JVM / Java / OOP / Composition

## 1. Notification Service Application

Composition models a has-a relationship by building a class from other objects instead of inheriting from them. In this example, NotificationService has a MessageSender, allowing it to work with different notification channels through the same interface. This makes the application flexible and easy to extend.

```java
package com.minte9.oop.composition;

public class NotificationServiceApp {
    public static void main(String[] args) {

        NotificationService service =
            new NotificationService(new EmailSender());  // polimorphism

        service.notifyUser("Your order was shipped.");
            // Sending email: Your order was shipped.

    }
}

interface MessageSender {
    void send(String message);
}

class EmailSender implements MessageSender {
    @Override
    public void send(String message) {
        System.out.println("Sending email: " + message);
    }
}

class SmsSender implements MessageSender {
    @Override
    public void send(String message) {
        System.out.println("Sending sms: " + message);
    }
}

// Composition: NotificationService HAS-A MessageSender
class NotificationService {

    // Dependency Inversion
    private MessageSender sender;  // service depends on the abstraction

    // Dependecny Injection
    public NotificationService(MessageSender sender) {
        this.sender = sender;
    }

    public void notifyUser(String message) {
        sender.send(message);
    }
}
```


## 2. Dependency Inversion

The Dependency Inversion Principle -- high-level code should depend on abstractions, not concrete implementations -- is exactly why `NotificationService` depends on the `MessageSender` interface instead of a concrete `EmailSender`. See the dedicated SOLID subcategory for the full principle.

```java
class NotificationService {
    private MessageSender sender; // depends on the abstraction, not EmailSender directly
}
```


## 3. Default Method

A default method is a method with an implementation declared inside an interface. Introduced in Java 8, it allows new behavior to be added to an interface without requiring every existing implementation to change. Implementing classes may either inherit the default implementation or override it.

```java
package com.minte9.oop.composition.default_note;

public class DefaultMethods {
    public static void main(String[] args) {

        NotificationService emailService = new NotificationService(new EmailSender());
        NotificationService smsService = new NotificationService(new SmsSender());

        emailService.send("Your order was shipped.");
        emailService.log("Your order was shipped.");

        smsService.send("Your code is 1234.");
        smsService.log("Your code is 1234.");

        /*
            Sending email: Your order was shipped.
            Logging message: Your order was shipped.
            Sending sms: Your code is 1234.
            Logging message: Your code is 1234.
        */
    }
}

interface MessageSender {
    void send(String message);

    default void log(String message) {  // Look Here
        System.out.println("Logging message: " + message);
    }
}

class EmailSender implements MessageSender {
    @Override
    public void send(String message) {
        System.out.println("Sending email: " + message);
    }
}

class SmsSender implements MessageSender {
    @Override
    public void send(String message) {
        System.out.println("Sending sms: " + message);
    }
}

class NotificationService {
    private MessageSender sender;
    public NotificationService(MessageSender sender) {
        this.sender = sender;
    }

    public void send(String message) {
        sender.send(message);
    }

    public void log(String message) {
        sender.log(message);
    }
}
```
