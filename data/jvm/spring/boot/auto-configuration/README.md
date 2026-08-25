# JVM / Spring / Boot / Auto-Configuration

## 1. @SpringBootApplication and Component Scanning

`@SpringBootApplication` bundles three annotations: `@Configuration` (defines beans), `@ComponentScan` (scans this package and below for `@Component`s), and `@EnableAutoConfiguration` (auto-configures beans based on the classpath). That's why one annotated main class is usually enough -- as long as it sits at the root of your package structure.

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@Component
class Greeter {
    String greet() { return "Hello from Spring Boot"; }
}

@SpringBootApplication // = @Configuration + @ComponentScan + @EnableAutoConfiguration
public class DemoApplication {
    public static void main(String[] args) {
        var context = SpringApplication.run(DemoApplication.class, args);
        System.out.println(context.getBean(Greeter.class).greet());
        // Hello from Spring Boot
    }
}
```


## 2. Starters and the Classpath

A "starter" is a curated dependency bundle -- `spring-boot-starter-web` pulls in Tomcat, Spring MVC, and Jackson together, pre-matched to work. Auto-configuration detects what's on the classpath (like an embedded Tomcat) and registers the beans that make sense, quietly backing off if you've already defined your own.

```java
// build.gradle: implementation("org.springframework.boot:spring-boot-starter-web")
// That one dependency is enough to get a working embedded server + REST support.

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloController {
    @GetMapping("/hello")
    String hello() {
        return "Hello, World!";
    }
}

@SpringBootApplication
public class WebDemoApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(WebDemoApplication.class, args);
        // Starts an embedded server; GET /hello returns "Hello, World!"
    }
}
```


## 3. application.properties/yml

`application.properties` (or `.yml`) is where externalized config lives -- port, datasource URL, logging, custom values -- read automatically at startup. Pull single values with `@Value("${name}")`, or bind a whole group to a typed class with `@ConfigurationProperties(prefix = "...")` for related settings.

```java
// src/main/resources/application.properties
// app.greeting.message=Hello from config
// app.greeting.repeat=2

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
class SimpleGreeter {
    @Value("${app.greeting.message}") // injects a single property value
    private String message;
}

@Component
@ConfigurationProperties(prefix = "app.greeting") // binds the whole "app.greeting.*" group
class GreetingProperties {
    private String message;
    private int repeat;

    // getters and setters omitted for brevity
}
```
