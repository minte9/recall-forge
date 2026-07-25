## JVM / Kotlin / OOP / Polymorphism

### 1. Automatic Type Inference

Treating a subclass instance as its superclass type works the same as Java, but Kotlin's smart casts mean you rarely need an explicit cast back down -- after an `is` check, the compiler already knows the narrower type for the rest of that branch.

~~~kotlin
fun describe(a: Any) {
    if (a is Dog) {
        a.bark()  // no (Dog) a cast needed, unlike Java
    }
}
~~~

### 2. Polymorphism + Dependency Injection

Injecting an interface type into a constructor and letting the concrete implementation vary at runtime works identically to Java -- the value is that the consuming class (`FileOpener`) never needs to know or branch on which concrete type it received.

~~~kotlin
interface Item { fun open() }
class Csv : Item { override fun open() = println("Opening CSV") }
class Xml : Item { override fun open() = println("Opening XML") }

class FileOpener(private val item: Item) {
    fun run() = item.open()   // same call, different behavior
}
~~~
