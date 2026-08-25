# JVM / Java / Streams / Basics

## 1. What a Stream Is

A Stream is not a data structure -- it doesn't store elements, it's a pipeline that pulls them from a source (a collection, array, file, etc.) and applies a chain of transformations. Streams are lazy and single-use: building a pipeline does nothing yet, and one already consumed by a terminal operation can't be reused.

```java
List<String> names = List.of("Ann", "Bob", "Cleo");

Stream<String> pipeline = names.stream() // no data touched yet
        .filter(n -> n.length() == 3);

pipeline.forEach(System.out::println); // only now does it actually run
```


## 2. Internal vs External Iteration

External iteration (a for-loop or `Iterator`) makes the developer control *how* elements are visited -- manual indexing, explicit `hasNext()`/`next()` calls. Internal iteration (a stream) flips that: the developer says *what* to do per element, and the library drives the looping.

```java
List<String> names = List.of("Ann", "bob", "Cleo");

// external: developer drives the loop
for (String name : names) {
    if (Character.isUpperCase(name.charAt(0))) System.out.println(name);
}

// internal: library drives the loop, developer just declares intent
names.stream()
        .filter(n -> Character.isUpperCase(n.charAt(0)))
        .forEach(System.out::println);
```


## 3. Laziness: Intermediate vs Terminal Operations

Calls like `filter`/`map`/`sorted` are intermediate -- they just record a step and return a new stream, without touching any element. Nothing actually runs until a terminal operation (`collect`, `forEach`, `count`, `reduce`...) is called, which then pulls elements through the whole chain in one pass.

```java
List<String> names = List.of("Ann", "Bob", "Cleo");

Stream<String> planned = names.stream()
        .filter(n -> {
            System.out.println("checking " + n); // never printed yet
            return n.startsWith("A");
        });

planned.collect(Collectors.toList()); // only now do the checks run
```
