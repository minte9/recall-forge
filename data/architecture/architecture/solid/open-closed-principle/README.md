# Architecture / Architecture / SOLID / Open/Closed Principle

## 1. Open/Close Principle (OCP)

The Open/Closed Principle states that software should be open for extension but closed for modification. New behavior should be added through new classes or implementations instead of changing stable, existing code.

```java
interface Notification {
    void send(String message);
}
```


## 2. Bad Implementation (violates OCP)

A design violates OCP when adding a new type requires modifying existing conditional logic. Each new notification channel forces changes to the service class, increasing the risk of breaking behavior that already works.

```java
/**
 * OCP - Non compliant example
 * ============================
 */
package solid.ocp.non_compliant;

public class BadNotificationApplication {
    public static void main(String[] args) {
        Bad_NotificationService service = new Bad_NotificationService();

        service.send("email", "Welcome!");
        service.send("sms", "Your code is 1234");
        service.send("push", "New message received");
    }
}

class Bad_NotificationService {
    public void send(String type, String message) {

        if (type.equals("email")) {
            System.out.println("Sending EMAIL: " + message);
        } else
        if (type.equals("sms")) {
            System.out.println("Sending SMS: " + message);
        } else
        if (type.equals("push")) {
            System.out.println("Sending PUSH: " + message);
        }

    }
}
```

## 3. OCP-compliant Implementation

An OCP-compliant design depends on an abstraction and uses polymorphism to support new behavior. Adding another notification type requires only a new implementation, while the existing service remains unchanged.

```java
/**
 * OCP - Open/Closed Principle
 * ======================
 */
package solid.ocp;

public class NotificationApplication {
    public static void main(String[] args) {
        NotificationService service = new NotificationService();

        service.send(new EmailNotification(), "Welcome!");
        service.send(new SmsNotification(), "Your code is 1234");
        service.send(new PushNotification(), "New message received");

        /*
            Sending EMAIL: Welcome!
            Sending SMS: Your code is 1234
            Sending PUSH: New message received
        */
    }
}

// Contract
interface Notification   {
    void send(String message);    
}

// Service
class NotificationService {

    // Open for extension / Closed for modifications
    public void send(Notification notification, String message) {
        notification.send(message);
    }
} 

// Extensions
class EmailNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("Sending EMAIL: " + message);
    }
}

class SmsNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("Sending SMS: " + message);
    }
}

class PushNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("Sending PUSH: " + message);
    }
}
```


## 4. Why interfaces are perfect for OCP?  

Interfaces define a stable contract while allowing many independent implementations. The calling code works with the abstraction, so implementations can be added, replaced, or selected at runtime without modifying the existing workflow.

```java
Notification notification = new EmailNotification();
notification.send("Welcome!");
```
