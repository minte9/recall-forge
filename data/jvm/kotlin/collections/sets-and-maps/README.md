# JVM / Kotlin / Collections / Sets & Maps

## 1. HashSet/TreeSet Wrap Java's Implementations

`hashSetOf()`/`sortedSetOf()` wrap Java's `HashSet`/`TreeSet` directly -- same performance and ordering guarantees (`HashSet` unordered, `TreeSet` sorted via `Comparable`), just with a lighter-weight construction syntax and value-based equality from `data class`.

```kotlin
val ids = hashSetOf("A", "A", "B")
println(ids)  // [A, B] -- duplicate silently ignored
```

## 2. mapOf()/mutableMapOf()

Same immutable/mutable split as lists: `mapOf()` is read-only, `mutableMapOf()` is Kotlin's `HashMap` equivalent. Access uses `map[key]` (returns `null` if absent) instead of Java's `.get(key)`, and `getOrDefault`/`getOrElse` cover the fallback case idiomatically.

```kotlin
val ages = mutableMapOf("Ana" to 30)
ages["Bo"] = 25
println(ages["Cy"] ?: -1)  // -1, key not found
```
