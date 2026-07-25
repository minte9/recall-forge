## JVM / Kotlin / Collections / Collection Operations

### 1. map/filter/reduce vs Java Streams

Kotlin collection operations look like Java Streams but skip the `.stream()`/`.collect()` ceremony -- `map`/`filter`/`reduce` are methods directly on the collection and return a collection directly, no terminal collector needed.

~~~kotlin
val evens = listOf(1, 2, 3, 4)
    .filter { it % 2 == 0 }
    .map { it * 10 }
// [20, 40] -- no .collect(Collectors.toList())
~~~

### 2. Sequences vs Eager Collections

Standard `List` operations are eager -- each `map`/`filter` call fully processes and allocates a new list before the next step runs. `.asSequence()` switches to Java Stream-like lazy evaluation, processing element-by-element through the whole chain -- worth reaching for on large collections with multiple chained operations.

~~~kotlin
val result = (1..1_000_000).asSequence()
    .filter { it % 2 == 0 }
    .map { it * 2 }
    .first()   // stops after finding the first match
~~~
