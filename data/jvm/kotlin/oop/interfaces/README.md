## JVM / Kotlin / OOP / Interfaces

### 1. Colon Syntax for Implementing Interfaces

A class implements an interface using `:`, the same symbol used for extending a class -- Kotlin doesn't distinguish `extends`/`implements` like Java. All interface members are implicitly open, so `override` is required on anything you implement.

~~~kotlin
interface Playable {
    fun play()
}

class Cat : Playable {
    override fun play() = println("The cat is playing.")
}
~~~

### 2. Default Methods You Can Skip Overriding

Like Java 8+ default methods, a Kotlin interface method can have a body. A class implementing the interface must override the abstract members but may skip the ones with defaults -- letting you add new interface behavior later without breaking existing implementers.

~~~kotlin
interface PaymentProcessor {
    fun processPayment(amount: Double)
    fun refundPayment(amount: Double) {
        println("Refund not supported by this processor!")
    }
}
~~~
