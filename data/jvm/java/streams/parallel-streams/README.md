## JVM / Java / Streams / Parallel Streams

### 1. parallelStream Basics

Any stream goes parallel via `parallelStream()` on a collection, or `.parallel()` on an existing stream. Internally, the source is split and processed concurrently on the common `ForkJoinPool`, then merged. The API looks identical to sequential -- easy to opt into, easy to misuse.

~~~java
List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8);

int sumOfSquares = numbers.parallelStream()
        .mapToInt(n -> n * n) // split across threads, then combined automatically
        .sum();
~~~


### 2. When Parallel Helps vs Hurts

Parallel only pays off when the dataset is large and per-element work is expensive enough to beat the fixed cost of splitting/coordinating/merging -- for small collections, sequential is usually faster. The source matters too: array/`ArrayList` sources split cleanly; `LinkedList` or `Files.lines` split poorly and gain little.

~~~java
List<Integer> small = List.of(1, 2, 3, 4, 5);
small.stream().mapToInt(Integer::intValue).sum(); // sequential: overhead would dominate here

IntStream.rangeClosed(1, 10_000_000)
        .parallel() // large, CPU-bound, array-backed -- a much better fit
        .mapToLong(n -> (long) n * n)
        .sum();
~~~


### 3. Statelessness and Thread-Safety Pitfalls

Lambdas in a parallel stream must be stateless -- mutating a shared `ArrayList` or counter from `forEach` is a data race, not a safe way to collect results. Let a real `Collector` (designed to merge partial results safely across threads) do the accumulation instead.

~~~java
// List<Integer> unsafe = new ArrayList<>();
// IntStream.range(0, 1000).parallel().forEach(unsafe::add); // data race, don't do this

List<Integer> safe = IntStream.range(0, 1000)
        .parallel()
        .boxed()
        .collect(Collectors.toList()); // Collectors.toList() merges safely across threads
~~~
