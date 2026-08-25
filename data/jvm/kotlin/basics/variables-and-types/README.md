# JVM / Kotlin / Basics / Variables & Types

## 1. val/var & Type Inference

`val` declares a read-only reference (like Java's `final`), `var` a reassignable one. Kotlin infers the type from the initializer, so explicit types are usually only written on function signatures and public APIs, not local variables.

```kotlin
val name = "John"     // inferred as String, cannot be reassigned
var age = 30           // inferred as Int, can be reassigned
age = 31
```

## 2. Return Type After Parameters

Kotlin places the return type after the parameter list, separated by `:` -- the reverse of Java's `int sum(int a, int b)`. This reads left-to-right as "given these inputs, produce this output."

```kotlin
fun sum(n1: Int, n2: Int): Int {
    return n1 + n2
}
```

## 3. Primitives Without Wrapper Classes

Kotlin has no separate wrapper types like Java's `Integer`/`Double`. It uses `Int`, `Double`, etc. directly and boxes them automatically only when needed (e.g. storing in a generic collection) -- you never choose between `int` and `Integer` yourself.
