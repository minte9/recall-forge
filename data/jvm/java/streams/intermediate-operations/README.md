# JVM / Java / Streams / Intermediate Operations

## 1. filter and map

`filter(Predicate)` keeps only matching elements; `map(Function)` transforms each one, possibly to a different type. Both are lazy intermediate operations -- nothing runs until a terminal operation like `collect` executes the whole pipeline, one element at a time through the full chain.

```java
List<String> names = List.of("Ann", "bob", "Cleo", "dave");

List<String> result = names.stream()
        .filter(name -> Character.isUpperCase(name.charAt(0))) // keep capitalized
        .map(String::toUpperCase)                              // then transform
        .collect(Collectors.toList());

System.out.println(result); // [ANN, CLEO]
```


## 2. sorted and distinct

`sorted()` orders elements by natural order or a given `Comparator`; `distinct()` removes duplicates via `equals`/`hashCode`. Both are stateful, unlike `filter`/`map` -- they generally need to see the whole stream first (`sorted` must buffer everything), making them costlier on large or infinite streams.

```java
List<String> names = List.of("Cleo", "ann", "Bob", "ann", "Dave");

List<String> distinctSorted = names.stream()
        .distinct()                                 // dedupe first
        .sorted(Comparator.comparing(String::toLowerCase)) // then sort, case-insensitive
        .collect(Collectors.toList());               // [ann, Bob, Cleo, Dave]
```


## 3. flatMap

`flatMap(Function)` is for when mapping each element produces its own stream (e.g. splitting sentences into words) and you want one flattened result. Unlike `map`'s strict one-to-one, `flatMap` substitutes each element's stream in place and concatenates them all into a single continuous stream.

```java
List<String> sentences = List.of("the quick fox", "jumps over");

List<String> words = sentences.stream()
        .flatMap(s -> Arrays.stream(s.split(" "))) // one sentence -> many words
        .collect(Collectors.toList());
        // [the, quick, fox, jumps, over]

List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4));
List<Integer> flat = nested.stream()
        .flatMap(List::stream) // flattens the nested lists into one stream
        .collect(Collectors.toList()); // [1, 2, 3, 4]
```
