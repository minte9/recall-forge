# JVM / Java / Versions / Java 8

## 1. Lambda Expressions and Functional Interfaces

Java 8's headline feature: lambda expressions, a concise way to pass behavior as a value via `x -> x * 2` syntax, backed by `java.util.function` interfaces like `Function`/`Predicate`. See the dedicated Lambdas subcategory for syntax, method references, and composition.

```java
Runnable oldStyle = new Runnable() { // pre-Java 8: anonymous class, lots of ceremony
    public void run() { System.out.println("run"); }
};

Runnable newStyle = () -> System.out.println("run"); // same thing, one line
```


## 2. Stream API

The Stream API brought declarative, pipeline-based processing -- `filter`/`map`/`collect` instead of manual loops and mutable accumulators. See the dedicated Streams subcategory for creation, collectors, primitive streams, and parallel streams in depth.

```java
List<String> names = List.of("Alice", "Bob", "Charlie");

List<String> result = names.stream()
        .filter(n -> n.length() > 3)   // keep long names
        .map(String::toUpperCase)      // transform each one
        .collect(Collectors.toList()); // declarative, no manual loop
```


## 3. Optional

`Optional<T>` wraps a value that might be absent, making that possibility explicit in a method's return type instead of relying on `null`. It nudges API design toward handling the "no value" case via `orElse`/`ifPresent`, rather than scattered null checks. Best used as a return type, not a field or parameter.

```java
Optional<String> found = findUserById(1);
System.out.println(found.orElse("Unknown")); // safe default if absent

found.ifPresent(name -> System.out.println("Found: " + name));

static Optional<String> findUserById(int id) {
    return id == 1 ? Optional.of("Alice") : Optional.empty();
}
```
