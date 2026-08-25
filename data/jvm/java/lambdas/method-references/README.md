# JVM / Java / Lambdas / Method References

## 1. Static Method References

A method reference is shorthand for a lambda that just calls an existing method: `ClassName::methodName`. `Integer::parseInt` is equivalent to `s -> Integer.parseInt(s)` -- usable anywhere a functional interface expects a matching signature.

```java
Function<String, Integer> parse = Integer::parseInt; // same as s -> Integer.parseInt(s)

List<Integer> numbers = List.of("1", "2", "3").stream()
        .map(parse)
        .collect(Collectors.toList()); // [1, 2, 3]
```


## 2. Instance Method References (Particular and Unbound)

Two flavors: a "particular object" reference (`myList::add`) binds to an existing instance, like `x -> myList.add(x)`. An "unbound" reference (`String::toUpperCase`) has no fixed receiver -- the functional interface's first parameter becomes the object it's called on, so it's `s -> s.toUpperCase()`.

```java
Function<String, String> toUpper = String::toUpperCase; // unbound: s -> s.toUpperCase()
System.out.println(toUpper.apply("java")); // JAVA

List<String> collected = new ArrayList<>();
Consumer<String> addToList = collected::add; // particular object: x -> collected.add(x)
List.of("a", "b").forEach(addToList);
```


## 3. Constructor References

`ClassName::new` refers to a constructor, commonly used with `Supplier` or `Function` to defer object creation. It selects the constructor overload matching the target interface's parameter list, so `ArrayList::new` can be a `Supplier<List<String>>` in one context and a copy-constructor `Function` in another.

```java
Supplier<List<String>> newList = ArrayList::new; // () -> new ArrayList<>()
List<String> list = newList.get();

Function<String, StringBuilder> newBuilder = StringBuilder::new; // s -> new StringBuilder(s)
```
