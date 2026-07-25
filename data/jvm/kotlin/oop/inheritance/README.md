## JVM / Kotlin / OOP / Inheritance

### 1. Classes Final by Default

Every Kotlin class is implicitly `final` -- to allow subclassing you must mark it `open` (or `abstract`), the opposite default from Java where every class is inheritable unless marked `final`. This makes "can this be extended?" a deliberate decision instead of an accident.

~~~kotlin
open class Animal(val name: String) {
    open fun sound() = "..."
}

class Dog(name: String) : Animal(name) {
    override fun sound() = "Woof"
}
~~~

### 2. Super Call in the Class Header

Kotlin calls the superclass constructor in the subclass header (`: Animal(name)`), not inside the constructor body like Java's `super(name)` statement -- there's no separate line to remember to write first.

### 3. Sealed Classes vs Java Enums

A `sealed class` restricts its subclasses to a known, closed set defined in the same file -- like an enum, but each variant can carry different data and shape. Combined with `when`, the compiler can verify every case is handled without needing an `else` branch.

~~~kotlin
sealed class Result
class Success(val data: String) : Result()
class Failure(val error: String) : Result()

fun handle(r: Result) = when (r) {
    is Success -> "Got: ${r.data}"
    is Failure -> "Error: ${r.error}"
    // no 'else' needed -- compiler knows these are the only cases
}
~~~
