# JVM / Kotlin / OOP / Static Keyword & Objects

## 1. companion object vs Java's static

Kotlin has no `static` keyword. A `companion object` inside a class holds members shared across all instances, initialized once -- the direct equivalent of Java's static fields/methods, just addressed through the class name like a Java static member would be.

```kotlin
class ClassRoom {
    companion object {
        var totalStudents: Int = 0
    }
    fun newEntry() { totalStudents++ }
}

fun main() {
    ClassRoom().newEntry()
    println(ClassRoom.totalStudents)  // 1
}
```

## 2. Top-Level Properties as Shared State

Because Kotlin allows top-level (non-class) properties and functions, simple shared state doesn't need a class or companion object at all -- on the JVM these compile to static fields/methods, but the source reads as plain package-level code.

```kotlin
var totalEmployees = 0   // compiles to a static field

fun newEntry() { totalEmployees++ }
```

## 3. Object Declarations (Singletons)

`object Foo { ... }` declares a class with exactly one instance, created lazily on first access -- Kotlin's built-in replacement for the classic Java double-checked-locking or enum singleton pattern, with no boilerplate.

```kotlin
object AppConfig {
    val version = "1.0"
}

fun main() = println(AppConfig.version)  // no getInstance()
```
