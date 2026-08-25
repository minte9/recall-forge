# JVM / Java / Streams / Stream Creation

## 1. Creating Streams from Collections and Values

Every Collection exposes `stream()` (and `parallelStream()`), the most common way to get a Stream. For known values, `Stream.of(...)` builds one directly from varargs. Streams are lazy and single-use: building one does nothing yet, and a stream already consumed by a terminal operation can't be reused.

```java
List<String> names = List.of("Ann", "Bob", "Cleo");

Stream<String> fromList = names.stream();       // from a collection
fromList.forEach(System.out::println);

Stream<Integer> fromValues = Stream.of(1, 2, 3); // from individual values

Stream<String> consumed = names.stream();
consumed.count();
consumed.forEach(System.out::println); // throws: stream already operated upon
```


## 2. Stream.generate and Stream.iterate

`Stream.generate(Supplier)` produces an infinite stream by repeatedly calling a supplier; `Stream.iterate(seed, fn)` produces one by repeatedly applying a function to the previous value. Both must be combined with `limit(n)` (or iterate's 3-arg predicate overload) to avoid an unbounded loop once a terminal operation runs.

```java
List<String> hellos = Stream.generate(() -> "Hello")
        .limit(3) // required -- generate() never stops on its own
        .collect(Collectors.toList()); // [Hello, Hello, Hello]

List<Integer> powersOfTwo = Stream.iterate(1, n -> n * 2)
        .limit(5)
        .collect(Collectors.toList()); // [1, 2, 4, 8, 16]

// Java 9+: iterate with a stop condition instead of limit
Stream.iterate(1, n -> n <= 100, n -> n * 2);
```


## 3. Reading Streams from Files and Arrays

`Arrays.stream(array)` creates a stream over an array, with primitive overloads (`int[]` -> `IntStream`, etc.) avoiding boxing. `Files.lines(path)` lazily streams a text file line by line -- memory-efficient for large files, and should be used in try-with-resources so the file handle gets closed.

```java
int[] numbers = {5, 3, 8, 1};
int max = Arrays.stream(numbers).max().orElseThrow(); // 8

try (Stream<String> lines = Files.lines(Path.of("notes.txt"))) { // closes the file
    long nonEmpty = lines.filter(line -> !line.isBlank()).count();
}
```
