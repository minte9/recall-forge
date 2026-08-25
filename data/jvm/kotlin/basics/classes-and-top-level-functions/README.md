# JVM / Kotlin / Basics / Classes & Top-Level Functions

## 1. No Class Wrapper Needed

In Java, `main` and every function must live inside a class. Kotlin allows top-level functions directly in a file, and the default class visibility (when you do write one) is `public` -- Java's default is package-private, so a Kotlin class is visible everywhere unless you explicitly narrow it.

```kotlin
// No class needed -- this compiles as a top-level function
fun main() {
    println("Hello, Kotlin!")
}
```

## 2. Auto-Generated Getters/Setters

A Kotlin `var` property gets a getter and setter generated for you; `val` gets a getter only. There's no hand-written boilerplate to keep in sync -- you only write custom accessor logic when it actually differs from the default.

```kotlin
class Student {
    var name: String = ""  // getter+setter generated
}

fun main() {
    val s = Student()
    s.name = "John"       // compiles to s.setName("John")
    println(s.name)       // compiles to s.getName()
}
```
