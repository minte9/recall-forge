## JVM / Java / Generics / Generic Classes and Methods

### 1. Declaring a Generic Class

A generic class declares type parameters in angle brackets, like `class Box<T>`, letting one definition work with any reference type while keeping compile-time checking -- no casts needed, wrong types rejected. `T` is just a placeholder, replaced by a concrete type argument like `Box<String>` at the point of use.

~~~java
class Box<T> {
    private T value;
    Box(T value) { this.value = value; }
    T get() { return value; }
}

Box<String> stringBox = new Box<>("Hello");
System.out.println(stringBox.get()); // Hello, no cast needed
// stringBox.set(42); // compile error: incompatible types
~~~


### 2. Generic Methods

A generic method declares its own type parameter before the return type -- `static <T> T firstOf(List<T> list)` -- independent of whether the enclosing class is generic. The type argument is almost always inferred from the call site, so a single static method works across every type with no boilerplate.

~~~java
static <T> T firstOf(List<T> list) {
    return list.get(0);
}

String firstName = firstOf(List.of("Alice", "Bob")); // T inferred as String
Integer firstNumber = firstOf(List.of(1, 2, 3));      // T inferred as Integer
~~~


### 3. Multiple Type Parameters

A class or method can declare multiple type parameters, separated by commas -- `class Pair<K, V>`. By convention: `T` for a general type, `E` for a collection element, `K`/`V` for a map's key/value, `R` for a return type -- any identifier works, but conventions aid readability.

~~~java
class Pair<K, V> {
    private final K key;
    private final V value;
    Pair(K key, V value) { this.key = key; this.value = value; }
}

Pair<String, Integer> age = new Pair<>("Ann", 30);
~~~
