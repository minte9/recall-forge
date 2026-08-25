# JVM / Java / OOP / Generics

## 1. Generics in Java

Generics let classes and methods work with different types while keeping compile-time type safety -- a type parameter like `T` replaces `Object`, catching type errors at compile time instead of causing a runtime `ClassCastException`. See the dedicated Generics subcategory for the full picture.

```java
MyClass<Integer> box = new MyClass<>();
box.set(10);
// box.set("John"); // compile-time error -- this is the whole point of generics

class MyClass<T> {
    private T t;
    void set(T t) { this.t = t; }
    T get() { return t; }
}
```


## 2. Multiple Types

A generic class can declare multiple type parameters at once, like `Box<T, U>` or the JDK's own `Map<K, V>` -- each parameter gets its own name and can represent a different type.

```java
class Box<T, U> {
    private T t;
    void set(T t) { this.t = t; }
    T get() { return t; }
}

Box<Integer, Integer> box = new Box<>();
```


## 3. Generics Limitations

Type erasure creates a few restrictions: type parameters can't be used in a static context (static fields belong to the class, not to a specific `T`), and primitive types like `int` can't be type arguments -- use the wrapper (`Integer`) instead.

```java
Box<Integer> numbers = new Box<>(); // OK
// Box<int> numbers = new Box<>();  // compile-time error, primitives not allowed
// static T value;                  // compile-time error, no static generic fields
```
