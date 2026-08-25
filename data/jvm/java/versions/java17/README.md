# JVM / Java / Versions / Java 17

## 1. Sealed Classes

Sealed classes/interfaces (finalized in 17) let a type declare exactly which subtypes may extend it via `permits`. This sits between fully open (anyone can extend) and fully closed (`final`) hierarchies, and pairs naturally with pattern matching -- the compiler can verify every permitted subtype is handled.

```java
sealed interface Shape permits Circle, Square {} // only these two may implement Shape

record Circle(double radius) implements Shape {}
record Square(double side) implements Shape {}

// A third `class Triangle implements Shape` would fail to compile
// unless added to the `permits` clause.
```


## 2. Records

Records (finalized in 16) are a compact way to declare an immutable data carrier -- `record Point(int x, int y)` auto-generates the constructor, accessors, `equals`, `hashCode`, and `toString`. They're implicitly `final` and can't extend a class, but can implement interfaces and add extra methods.

```java
record Point(int x, int y) {
    double distanceFromOrigin() { // custom method alongside the generated ones
        return Math.sqrt(x * x + y * y);
    }
}

Point p = new Point(3, 4);
System.out.println(p);    // Point[x=3, y=4] -- toString generated for free
System.out.println(p.x()); // 3 -- accessor generated for free
```


## 3. Pattern Matching for instanceof

Pattern matching for `instanceof` (finalized in 16) removes the redundant cast after a type check -- bind a variable directly: `if (obj instanceof String s)`. The compiler only treats `s` as assigned in branches where the check is true, keeping it safe while cutting boilerplate.

```java
Object obj = "Hello";

if (obj instanceof String) {          // before: check, then separate cast
    String s = (String) obj;
    System.out.println(s.length());
}

if (obj instanceof String s) {        // pattern matching: cast is implicit
    System.out.println(s.length());
}
```
