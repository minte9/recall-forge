# JVM / Kotlin / Functional Style / Scope Functions

## 1. let/run/with

`let` and `run` both give you the receiver as a lambda scope and return the lambda's result -- `let` refers to it as `it` (good for null checks: `x?.let { ... }`), `run` refers to it as `this` (good for configuring and computing a result in one block). `with` is like `run` but takes the receiver as a plain argument, not an extension call.

```kotlin
val length = nickname?.let { it.length } ?: 0
```

## 2. apply/also

`apply` and `also` both return the *original object*, not the lambda result -- useful for side effects mid-chain. `apply` uses `this` (classic object-configuration style), `also` uses `it` (classic logging/debug-print style).

```kotlin
val sb = StringBuilder()
    .apply { append("Hello") }   // returns sb itself
    .also { println(it.length) } // side effect, still returns sb
```
