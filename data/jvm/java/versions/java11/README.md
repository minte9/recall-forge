# JVM / Java / Versions / Java 11

## 1. Local-Variable Type Inference (var)

`var` lets the compiler infer a local variable's type from its initializer -- introduced in Java 10, extended to lambda parameters in Java 11. It's not dynamic typing; the type is fixed at compile time, just the syntax is shorter. Best when the type is already obvious from the right-hand side.

```java
var names = new ArrayList<String>(); // inferred as ArrayList<String>
names.add("Alice");

for (var name : names) { // inferred as String
    System.out.println(name);
}
```


## 2. New HTTP Client

`java.net.http.HttpClient` finally gave Java a modern built-in HTTP client -- HTTP/2 by default, connection reuse, sync and async requests -- replacing the clunky `HttpURLConnection`. Async calls return `CompletableFuture`, integrating naturally with the rest of Java's concurrency utilities.

```java
HttpClient client = HttpClient.newHttpClient();

HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.example.com/status"))
        .GET()
        .build();

HttpResponse<String> response =
        client.send(request, HttpResponse.BodyHandlers.ofString()); // blocking call
```


## 3. String and Collection API Additions

Java 11 added everyday `String` convenience methods: `isBlank`, `strip` (Unicode-aware `trim`), `lines()` (splits into a line stream), and `repeat()`. These closed gaps developers had been working around with manual loops or Apache Commons for years.

```java
String text = "  Hello World  ";

System.out.println(text.strip());    // "Hello World"
System.out.println("   ".isBlank()); // true
System.out.println("ab".repeat(3));  // "ababab"
```
