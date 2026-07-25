## JVM / Kotlin / Functional Style / Extension Functions

### 1. Extension Functions Basics

An extension function adds a method to an existing type -- including types you don't own, like `String` -- without inheritance or a wrapper class. It's syntactic sugar: it compiles to a static method taking the receiver as its first argument, so it can't access private members.

~~~kotlin
fun String.shout() = this.uppercase() + "!"

fun main() = println("hello".shout())  // HELLO!
~~~

### 2. vs Java's Static Utility Classes

Java achieves the same goal with static helpers (`StringUtils.shout(s)`), but the call site reads awkwardly outside-in. Kotlin extensions read left-to-right as if the method belonged to the type, which is why the standard library itself is full of them (`map`, `filter`, etc. on collections).
