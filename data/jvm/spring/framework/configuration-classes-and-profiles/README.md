# JVM / Spring / Framework / Configuration Classes and Profiles

## 1. @Import and Modularizing Configuration

One `@Configuration` class doesn't have to declare every bean. `@Import(OtherConfig.class)` pulls in another class's bean definitions, letting you split config into focused pieces (datasource, security, messaging...) instead of one giant class. Libraries can also ship a `@Configuration` class for consumers to simply `@Import`.

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
class DataSourceConfig {
    @Bean
    String dataSourceUrl() {
        return "jdbc:postgresql://localhost/app";
    }
}

@Configuration
class SecurityConfig {
    @Bean
    String securityRealm() {
        return "app-realm";
    }
}

@Configuration
@Import({DataSourceConfig.class, SecurityConfig.class}) // composes both into one root config
class AppConfig {
}
```


## 2. @Profile for Environment-Specific Beans

`@Profile("name")` activates a bean only when that profile is enabled -- e.g. a real payment gateway under "prod", a fake one under "test". No code changes needed, just the active profile. Profiles are set externally via `spring.profiles.active` (property, env var, or command-line arg).

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

interface PaymentGateway {
    void charge(int amount);
}

@Configuration
class PaymentConfig {

    @Bean
    @Profile("prod") // only registered when "prod" is the active profile
    PaymentGateway realGateway() {
        return amount -> System.out.println("Charging real card: " + amount);
    }

    @Bean
    @Profile("test")
    PaymentGateway fakeGateway() {
        return amount -> System.out.println("Fake charge (test profile): " + amount);
    }
}

// Activated via: spring.profiles.active=test
```


## 3. @Value and @ConfigurationProperties

`@Value("${name}")` injects one property value, optionally with a default (`${name:default}`). `@ConfigurationProperties(prefix = "...")` instead binds a whole group of related settings onto a plain class's fields, giving you a typed, autocompletable config object instead of scattered `@Value` fields.

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// application.properties:
// app.retry.max-attempts=3
// app.retry.delay-ms=500

@Component
class SingleValueExample {
    @Value("${app.retry.max-attempts:1}") // 1 is the default if the property is missing
    private int maxAttempts;
}

@Component
@ConfigurationProperties(prefix = "app.retry") // binds the whole "app.retry.*" group
class RetryProperties {
    private int maxAttempts;
    private int delayMs;

    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

    public int getDelayMs() { return delayMs; }
    public void setDelayMs(int delayMs) { this.delayMs = delayMs; }
}
```
