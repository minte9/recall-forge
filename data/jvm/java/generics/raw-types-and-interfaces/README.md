# JVM / Java / Generics / Raw Types and Generic Interfaces

## 1. Generic Interfaces

Interfaces can be generic too -- a class implementing one supplies a concrete argument (`implements List<String>`) or stays generic and passes its own parameter through (`implements List<T>`). Much of the Collections Framework works this way: `List<E>`, `Map<K,V>`, `Comparable<T>` are all generic interfaces.

```java
interface Repository<T> {
    void save(T item);
}

class InMemoryRepository<T> implements Repository<T> { // stays generic, passes T through
    public void save(T item) { /* ... */ }
}

Repository<String> names = new InMemoryRepository<>();
```


## 2. Self-Referential Generics (Enum<E extends Enum<E>>)

A self-referential bound refers back to its own type parameter -- `<E extends Comparable<E>>`, or famously `Enum<E extends Enum<E>>`. This guarantees a type can only be compared to instances of itself (a `Day` compares only with another `Day`), which a plain `<E extends Comparable>` couldn't enforce.

```java
abstract class Measurement<T extends Measurement<T>> implements Comparable<T> {
    abstract double magnitude();
    public int compareTo(T other) { return Double.compare(magnitude(), other.magnitude()); }
}

class Distance extends Measurement<Distance> { // can only be compared to another Distance
    double magnitude() { return 5.0; }
}
```


## 3. Raw Types and Legacy Code

A raw type is a generic type used with no type argument -- `List` instead of `List<String>` -- legal only for backward compatibility, and it disables generic checking entirely, producing unchecked warnings and allowing runtime `ClassCastException`s. Avoid it in new code; it's only ever unavoidable when interoperating with old libraries.

```java
List raw = new ArrayList(); // raw type: no compile-time type checking
raw.add("a string");
raw.add(42); // no error here, even though this is inconsistent

List<String> strings = raw; // unchecked warning, compiles anyway
for (String s : strings) { /* throws ClassCastException on the Integer element */ }
```
