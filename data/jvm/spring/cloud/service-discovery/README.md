# JVM / Spring / Cloud / Service Discovery

## 1. Why Service Discovery

In a microservices setup, service instances scale up/down and get rescheduled with new network addresses constantly -- hardcoding IPs/ports in configuration doesn't survive that. A service registry (like Eureka) lets services register themselves on startup and look each other up by logical name instead of a fixed address.

```java
// Instead of a hardcoded URL:
// restTemplate.getForObject("http://192.168.1.5:8081/orders", Order.class);

// Services look each other up by name through the registry:
restTemplate.getForObject("http://order-service/orders", Order.class);
```


## 2. Eureka Server and Client

@EnableEurekaServer turns a Spring Boot app into the registry itself; @EnableEurekaClient (or just having the client dependency on the classpath) makes any other service register with it on startup and periodically send heartbeats, so the registry always reflects which instances are actually alive.

```java
@SpringBootApplication
@EnableEurekaServer // this app becomes the service registry
public class DiscoveryServerApplication { }

@SpringBootApplication
@EnableEurekaClient // this app registers itself with the discovery server on startup
public class OrderServiceApplication { }
```


## 3. Client-Side Load Balancing

Because the registry knows every healthy instance of a service, calling by logical name (via a load-balanced RestTemplate/WebClient) automatically spreads requests across all registered instances -- the caller never manages a list of addresses itself, and instances that stop sending heartbeats are automatically excluded.

```java
@Bean
@LoadBalanced // enables logical-name resolution + client-side load balancing
RestTemplate restTemplate() {
    return new RestTemplate();
}
```
