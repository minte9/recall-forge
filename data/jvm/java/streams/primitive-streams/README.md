# JVM / Java / Streams / Primitive Streams

## 1. IntStream, LongStream, DoubleStream

A `Stream<Integer>` stores boxed objects, with real heap overhead per element. `IntStream`/`LongStream`/`DoubleStream` operate on raw primitives instead, avoiding that cost, and add numeric methods like `sum()`/`average()` a generic Stream lacks. `IntStream.range`/`rangeClosed` are the standard way to generate integer sequences.

```java
int sum = IntStream.rangeClosed(1, 5).sum(); // 15, no boxing involved

double average = IntStream.of(2, 4, 6, 8).average().orElse(0); // 5.0
```


## 2. Boxing/Unboxing Costs and mapToObj

`mapToInt(fn)` turns a `Stream<T>` into an `IntStream`; `boxed()` turns it back into `Stream<Integer>` when a boxed API is needed. `mapToObj(fn)` turns each primitive into an object. Minimizing back-and-forth conversions between boxed and primitive forms matters for numeric-heavy pipelines.

```java
List<Integer> numbers = List.of(1, 2, 3, 4);

int total = numbers.stream().mapToInt(Integer::intValue).sum(); // Stream<Integer> -> IntStream

List<Integer> boxedAgain = IntStream.rangeClosed(1, 3)
        .boxed() // IntStream -> Stream<Integer>, needed to use a Collector
        .collect(Collectors.toList());

List<String> labels = IntStream.rangeClosed(1, 3)
        .mapToObj(i -> "Item " + i) // int -> arbitrary object
        .collect(Collectors.toList());
```


## 3. Numeric Summary Statistics

`summaryStatistics()` computes count, sum, min, max, and average in a single pass, returned together as an `IntSummaryStatistics` (or Long/Double equivalent) -- cheaper than calling several separate terminal operations, each of which would otherwise re-scan a fresh stream.

```java
IntSummaryStatistics stats = IntStream.of(4, 8, 15, 16, 23, 42).summaryStatistics();

System.out.println(stats.getSum());     // 108 -- one pass computes all of these
System.out.println(stats.getMax());     // 42
System.out.println(stats.getAverage()); // 18.0
```
