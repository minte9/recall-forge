## JVM / Spring / Framework / Inversion of Control

### 1. Inversion of Control (IoC)

Inversion of Control (IoC) means an external container creates and wires your objects, instead of your code doing it with `new`. In Spring, that container is the `ApplicationContext` — it manages beans and their dependencies for you.

~~~java
ApplicationContext context =
    new AnnotationConfigApplicationContext(AppConfig.class);

// Ask the container for the bean, instead of building it yourself
MessageService service =
    context.getBean(MessageService.class);
~~~


### 2. Manual creation (objects)

Without a framework, you create objects yourself with `new` and pass dependencies by hand. This works for small programs, but wiring grows painful as the object graph gets bigger.

~~~java
public class ManualCreationApp {
    public static void main(String[] args) {

        // No container involved -- you build and wire everything yourself
        MessageService service = new MessageService();
        NotificationController controller = new NotificationController(service);

        controller.print(); // Hello from Service / Manual Creation
    }
}

class MessageService {
    public String getMessage() {
        return "Hello from Service / Manual Creation";
    }
}

class NotificationController {
    private final MessageService messageService;

    public NotificationController(MessageService messageService) {
        this.messageService = messageService; // manually passed in
    }

    public void print() {
        System.out.println(messageService.getMessage());
    }
}
~~~


### 3. Factory pattern

A Factory class centralizes `new` calls in one place instead of scattering them everywhere. It's more organized than manual creation, but your code is still the one doing the wiring -- Spring isn't involved yet.

~~~java
public class FactoryPatternApp {
    public static void main(String[] args) {

        // Object creation is centralized here, but still not managed by a container
        NotificationController controller = AppFactory.createNotificationController();
        controller.print(); // Hello from Service / Factory Pattern
    }
}

class AppFactory {
    public static NotificationController createNotificationController() {
        MessageService service = new MessageService();
        return new NotificationController(service);
    }
}

class NotificationController {
    private final MessageService messageService;

    public NotificationController(MessageService messageService) {
        this.messageService = messageService;
    }

    public void print() {
        System.out.println(messageService.getMessage());
    }
}

class MessageService {
    public String getMessage() {
        return "Hello from Service / Factory Pattern";
    }
}
~~~


### 4. Spring IoC

With Spring, the container takes over: it creates beans, resolves their dependencies, and wires everything together. Your code just asks the `ApplicationContext` for a bean -- it no longer builds the object graph itself.

~~~java
// Maven/Gradle: add spring-context as a dependency

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class SpringIocApp {
    public static void main(String[] args) {

        ApplicationContext context =
            new AnnotationConfigApplicationContext(AppConfig.class);

        NotificationController controller =
            context.getBean(NotificationController.class); // Spring built this for us

        controller.print(); // Hello from Service / Spring IoC
    }
}

@Configuration // tells Spring this class defines beans
class AppConfig {

    @Bean // Spring calls this method once and manages the returned object
    MessageService messageService() {
        return new MessageService();
    }

    @Bean
    NotificationController notificationController(MessageService messageService) {
        return new NotificationController(messageService); // Spring injects the dependency
    }
}

class MessageService {
    public String getMessage() {
        return "Hello from Service / Spring IoC";
    }
}

class NotificationController {
    private final MessageService messageService;

    public NotificationController(MessageService messageService) {
        this.messageService = messageService;
    }

    public void print() {
        System.out.println(messageService.getMessage());
    }
}
~~~


### 5. Spring Boot

Spring Boot builds on top of Spring Framework and auto-configures common components for you. It still uses the same `ApplicationContext` underneath, but needs far less manual setup to get an app running.

~~~java
// Maven/Gradle: add spring-boot-starter as a dependency

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@SpringBootApplication // auto-configuration + component scanning + @Configuration, all in one
public class SpringBootApp {
    public static void main(String[] args) {

        ApplicationContext context =
            SpringApplication.run(SpringBootApp.class, args);

        NotificationController controller =
            context.getBean(NotificationController.class);

        controller.print(); // Hello from Service / Spring Boot
    }
}

@Service // discovered automatically, no @Bean method needed
class MessageService {
    public String getMessage() {
        return "Hello from Service / Spring Boot";
    }
}

@Component
class NotificationController {
    private final MessageService messageService;

    public NotificationController(MessageService messageService) {
        this.messageService = messageService;
    }

    public void print() {
        System.out.println(messageService.getMessage());
    }
}
~~~
