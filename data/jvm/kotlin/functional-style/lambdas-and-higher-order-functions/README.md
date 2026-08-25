# JVM / Kotlin / Functional Style / Lambdas & Higher-Order Functions

## 1. Lambda Syntax vs Java

A Kotlin lambda is `{ params -> body }`, with no `->` arrow-only form like Java's `(x) -> x + 1` -- the braces themselves delimit the lambda, and a single parameter can be referenced implicitly as `it` without naming it.

```kotlin
val square = { x: Int -> x * x }
val doubled = listOf(1, 2, 3).map { it * 2 }  // implicit 'it'
```

## 2. Functions as Parameters/Return Values

A function type like `(Int) -> Int` can be passed as a parameter or returned, the same idea as Java's `Function<Integer, Integer>` -- but it's a first-class type in the language, not an interface you're borrowing from `java.util.function`.

```kotlin
fun applyTwice(x: Int, f: (Int) -> Int): Int = f(f(x))

fun main() = println(applyTwice(3) { it + 1 })  // 5
```
