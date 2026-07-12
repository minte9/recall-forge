## JVM / Java / Versions / Java 9

### 1. Module System (JPMS)

The Java Platform Module System (JPMS) let developers group packages into modules with explicit `exports`/`requires`, fixing "JAR hell" where the classpath had no real visibility control. A `module-info.java` declares what a module needs and what it exposes -- public classes in non-exported packages stay truly hidden.

~~~java
// module-info.java
module com.example.orders {
    requires java.sql;

    exports com.example.orders.api;      // visible to other modules
    // com.example.orders.internal is NOT exported -- stays hidden, even if public
}
~~~


### 2. JShell (REPL)

JShell added an interactive REPL to the JDK -- evaluate statements and expressions one at a time with no class or `main` method needed. Great for testing snippets or exploring an unfamiliar API without full compilation ceremony.

~~~java
// typed interactively at the `jshell>` prompt, not compiled as a file

jshell> int x = 10
x ==> 10

jshell> x + 5
$2 ==> 15
~~~


### 3. Collection Factory Methods

`List.of`, `Set.of`, and `Map.of` create small immutable collections in one line, replacing the old `Arrays.asList` + `Collections.unmodifiableList` dance. They reject `null` elements and throw `UnsupportedOperationException` on any mutation attempt -- a fail-fast guard instead of a silent bug.

~~~java
List<String> colors = List.of("Red", "Green", "Blue");
Map<String, Integer> ages = Map.of("Alice", 30, "Bob", 25);

try {
    colors.add("Yellow"); // throws: list is immutable
} catch (UnsupportedOperationException e) {
    System.out.println("Cannot modify: " + e.getClass().getSimpleName());
}
~~~
