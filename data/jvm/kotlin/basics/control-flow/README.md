# JVM / Kotlin / Basics / Control Flow

## 1. when Replaces switch

`when` is Kotlin's `switch` replacement -- no `break` needed (each branch ends automatically, so there's no fall-through bug to guard against), and it can also be used as an expression that produces a value directly.

```kotlin
val grade = when (score) {
    in 90..100 -> "A"
    in 80..89 -> "B"
    else -> "F"
}
```

## 2. Unified Iteration

Kotlin iterates ranges, arrays, and collections with the same `for (x in ...)` syntax -- no separate enhanced-for vs indexed-for split like Java. `forEach` with a trailing lambda covers the same ground for a more functional style.

```kotlin
for (i in 0..2) print(i)          // 012
for (n in listOf(1, 2, 3)) print(n)  // 123
listOf(1, 2, 3).forEach { print(it) } // 123
```
