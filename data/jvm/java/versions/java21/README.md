## JVM / Java / Versions / Java 21

### 1. Virtual Threads

Virtual threads (finalized in 21) are lightweight, JVM-managed threads that let apps run millions of concurrent tasks without exhausting OS resources, unlocking thread-per-request code at scale. See the dedicated Concurrency subcategory for the full picture.

~~~java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 10_000; i++) {
        executor.submit(() -> Thread.sleep(100)); // cheap even at massive scale
    }
}
~~~


### 2. Pattern Matching for switch

Pattern matching for `switch` (finalized in 21) extends `switch` to match type patterns with guard clauses (`when`), replacing long if-else-instanceof chains. The compiler checks exhaustiveness against sealed hierarchies, and `case null` is finally allowed inside the switch instead of needing a separate check.

~~~java
static String describe(Object obj) {
    return switch (obj) {
        case null -> "it's null";                    // null handled inside switch now
        case Integer i when i > 0 -> "positive: " + i; // guard clause
        case Integer i -> "non-positive: " + i;
        case String s -> "string of length " + s.length();
        default -> "something else";
    };
}
~~~


### 3. Record Patterns

Record patterns (also finalized in 21) deconstruct a record's components directly in a pattern -- `case Point(int x, int y)` -- instead of matching then calling accessors separately. These nest, so you can reach into records within records, pairing naturally with sealed interfaces and switch.

~~~java
record Point(int x, int y) {}
record Rectangle(Point topLeft, Point bottomRight) {}

static int area(Object obj) {
    return switch (obj) {
        // deconstructs Rectangle AND both nested Points in one pattern
        case Rectangle(Point(var x1, var y1), Point(var x2, var y2)) ->
                Math.abs((x2 - x1) * (y2 - y1));
        default -> 0;
    };
}
~~~
