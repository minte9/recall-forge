# JVM / Java / Versions / Java 25

## 1. Compact Source Files and Instance Main Methods

Compact source files (finalized in 25) drop the ceremony new Java learners hit first: no `public class`, no `public static void main(String[] args)`. A file can start with a plain `void main()`, and `java` runs a `.java` file directly with no separate compile step.

```java
// HelloWorld.java -- a complete, valid Java 25 program

void main() {
    println("Hello, Java 25!");
}
```


## 2. Scoped Values

Scoped values (finalized in 25) share immutable data across a thread and the methods it calls, without passing it as a parameter. Unlike `ThreadLocal`, a scoped value is bound only for one call and auto-unbinds when it returns -- no leaks, and safe to share across structured concurrency tasks.

```java
static final ScopedValue<String> CURRENT_USER = ScopedValue.newInstance();

ScopedValue.where(CURRENT_USER, "alice").run(() -> {
    handleRequest(); // CURRENT_USER.get() is visible here, no parameter needed
});

static void handleRequest() {
    System.out.println("User: " + CURRENT_USER.get());
}
```


## 3. Module Import Declarations

Module import declarations (finalized in 25) let a file import every package a module exports with one line -- `import module java.base` -- instead of several individual package imports. It only imports what the module actually exports, so encapsulation is unaffected.

```java
import module java.base; // pulls in java.util, java.util.stream, etc. at once

void main() {
    List<String> names = List.of("Ann", "Bob"); // no explicit java.util.List import
    names.forEach(System.out::println);
}
```
