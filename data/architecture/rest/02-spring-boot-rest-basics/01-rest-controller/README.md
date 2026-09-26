# API / REST / Spring Boot REST Basics / REST Controller

## 1. @RestController

`@RestController` is `@Controller` plus `@ResponseBody` on every method:
return values are written straight to the HTTP body (as JSON via Jackson)
instead of being resolved as view names. It's the entry point of every
Spring REST API.

```java
@RestController   // return values -> response body, not templates
public class HelloController { }
```

## 2. @RequestMapping

`@RequestMapping` maps URLs to handlers. On the class it sets a shared path
prefix; on methods it adds the rest of the path. The shortcut annotations
(`@GetMapping`, `@PostMapping`...) are just `@RequestMapping` with the
method fixed.

```java
@RestController
@RequestMapping("/api/books")       // prefix for all methods
class BookController {
    @GetMapping("/count")           // GET /api/books/count
    long count() { return 3; }
}
```

## 3. The First Endpoint

With `spring-boot-starter-web` on the classpath, one annotated class is
enough: Boot starts embedded Tomcat, registers the controller and
configures JSON conversion. No servlet or XML setup is needed.

```java
@RestController
class HelloController {
    @GetMapping("/hello")
    Map<String, String> hello() {
        return Map.of("message", "hi");   // -> {"message":"hi"}
    }
}
```
