# JVM / Spring / Cloud / Centralized Configuration

## 1. Config Server Basics

Spring Cloud Config Server centralizes application.properties/yml for every microservice in one place (typically a Git repo), so configuration changes don't require rebuilding and redeploying each service individually. Each service becomes a Config Client, fetching its configuration from the Config Server at startup instead of bundling it locally.

```java
@SpringBootApplication
@EnableConfigServer // this app serves configuration to other services
public class ConfigServerApplication { }
```


## 2. @RefreshScope

@RefreshScope on a bean lets it pick up updated configuration values at runtime (triggered via the /actuator/refresh endpoint or an event bus) without restarting the whole service -- without it, a bean only reads its @Value-injected properties once, at startup, and ignores later config changes.

```java
@RefreshScope // re-reads @Value below when a refresh event fires, no restart needed
@Component
class FeatureFlags {
    @Value("${feature.new-checkout.enabled}")
    private boolean newCheckoutEnabled;
}
```


## 3. Config Server vs application.properties

Local application.properties still works fine for a single service or values that never change per-environment; Config Server earns its complexity once there are several services needing the same shared values (a database URL, feature flags) that should be updatable in one place without redeploying every service that uses them.

```properties
# bootstrap.properties (or spring.config.import in newer versions)
spring.config.import=optional:configserver:http://config-server:8888
```
