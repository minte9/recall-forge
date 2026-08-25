# JVM / Kotlin / OOP / Classes & Data Classes

## 1. Data Classes vs Java POJOs/Records

`data class` generates `equals()`, `hashCode()`, `toString()`, and a `copy()` method from the primary constructor properties -- the boilerplate a Java POJO needs Lombok or manual code for, and that a Java `record` only partially covers (records can't have mutable fields; Kotlin data classes can mix `val`/`var`).

```kotlin
data class Point(val x: Int, val y: Int)

fun main() {
    val p1 = Point(1, 2)
    val p2 = p1.copy(y = 5)   // Point(x=1, y=5)
    println(p1 == p2)          // false, equals() by value
}
```

## 2. Properties vs Manual Getters/Setters

A class property is declared once (`var name: String`) and the compiler generates the accessor pair -- there's no separate field + getter + setter to write and keep in sync, and custom logic only needs a `get()`/`set()` block when it actually deviates from the default.

```kotlin
class Cat {
    var name: String = ""
        get() = field.uppercase()   // custom getter
}
```

## 3. Primary vs Secondary Constructors

The primary constructor lives in the class header. Secondary constructors must delegate to it via `this(...)` -- Kotlin doesn't allow a secondary constructor to skip the primary the way Java allows independent overloaded constructors.

```kotlin
class Rectangle(val w: Int, val h: Int) {
    constructor() : this(1, 1)          // delegates to primary
    constructor(side: Int) : this(side, side)
}
```
