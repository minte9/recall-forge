# JVM / Java / Generics / Bounded Type Parameters

## 1. Upper Bounds with extends

A bounded type parameter restricts what can substitute for `T` via an upper bound -- `<T extends Number>` (the `extends` keyword is used for both classes and interfaces here). This lets generic code call methods declared on the bound, like `Number`'s `doubleValue()`, which an unbounded `<T>` wouldn't allow.

```java
static <T extends Number> double sumOf(T a, T b) {
    return a.doubleValue() + b.doubleValue(); // only works because T extends Number
}

System.out.println(sumOf(3, 4));     // 7.0
// sumOf("a", "b"); // compile error: String does not extend Number
```


## 2. Multiple Bounds

Multiple bounds combine with `&` -- `<T extends Comparable<T> & Serializable>` -- meaning `T` must satisfy every bound at once. At most one bound may be a class, and it must come first; the rest must be interfaces, mirroring Java's single-class-inheritance rule.

```java
static <T extends Comparable<T> & Serializable> T max(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
}

System.out.println(max(3, 7)); // 7 -- Integer implements both bounds
```


## 3. Bounded Type Parameters in Practice (Comparable)

`<T extends Comparable<T>>` is the JDK's own pattern for methods like `Collections.max`/`sort`, which need to call `compareTo`. The bound guarantees that method exists at compile time -- recognize this pattern whenever a generic algorithm needs to order or compare, not just store, its values.

```java
static <T extends Comparable<T>> T max(List<T> items) {
    T largest = items.get(0);
    for (T item : items) {
        if (item.compareTo(largest) > 0) largest = item; // needs compareTo to exist
    }
    return largest;
}
```
