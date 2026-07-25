## JVM / Kotlin / Functional Style / Function Features

### 1. Default & Named Arguments

Parameters can have default values, so callers only supply what differs from the default -- removing the need for Java's telescoping method overloads. Named arguments let a call specify parameters by name, in any order, which is especially useful when several parameters share a type.

~~~kotlin
fun greet(name: String, greeting: String = "Hello") =
    "$greeting, $name!"

fun main() {
    println(greet("Ana"))                       // Hello, Ana!
    println(greet(name = "Ana", greeting = "Hi")) // Hi, Ana!
}
~~~

### 2. Single-Expression Functions

When a function body is a single expression, the braces and `return` can be dropped in favor of `= expression` -- the return type is then usually inferred too. This turns many small Java getter-style methods into one-liners.

~~~kotlin
fun double(x: Int) = x * 2   // no braces, no explicit Int
~~~
