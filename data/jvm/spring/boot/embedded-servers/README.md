# JVM / Spring / Boot / Embedded Servers

## 1. spring-boot-starter-web and Embedded Tomcat

`spring-boot-starter-web` bundles an embedded Tomcat server with Spring MVC and Jackson, so `SpringApplication.run()` starts a fully working HTTP server with no external servlet container to install or configure. The app becomes a self-contained, runnable JAR instead of a WAR deployed onto a separately managed server.

```java
// build.gradle: implementation("org.springframework.boot:spring-boot-starter-web")

@RestController
class HelloController {
    @GetMapping("/hello")
    String hello() { return "Hello, World!"; }
}

@SpringBootApplication
public class WebApp {
    public static void main(String[] args) {
        SpringApplication.run(WebApp.class, args); // starts embedded Tomcat on port 8080
    }
}
```


## 2. Switching Embedded Servers (Jetty/Undertow)

Tomcat is the default, but swapping to Jetty or Undertow just means excluding the Tomcat starter and adding the alternative -- no code changes required, since they all implement the same `ServletWebServerFactory` abstraction that Spring Boot auto-configures against.

```java
// build.gradle: exclude the default Tomcat starter, add Jetty instead
//
// implementation("org.springframework.boot:spring-boot-starter-web") {
//     exclude group: "org.springframework.boot", module: "spring-boot-starter-tomcat"
// }
// implementation("org.springframework.boot:spring-boot-starter-jetty")
```


## 3. Servlet Container Auto-Configuration

Auto-configuration detects which servlet container is on the classpath and configures a matching `ServletWebServerFactory` bean automatically -- registering Tomcat's factory if Tomcat is present, Jetty's if Jetty is present. Same classpath-scanning mechanism as Auto-Configuration, applied specifically to the web layer.

```java
// No manual bean needed -- Spring Boot inspects the classpath at startup and
// registers the right ServletWebServerFactory (Tomcat/Jetty/Undertow) for you.

@Bean
ServletWebServerFactory servletContainer() { // only needed for custom tuning
    TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
    factory.setPort(9090);
    return factory;
}
```
