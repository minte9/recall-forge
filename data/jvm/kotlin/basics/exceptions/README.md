## JVM / Kotlin / Basics / Exceptions

### 1. No Checked Exceptions

Kotlin has no `throws` clause and no checked exceptions -- the compiler never forces a `try`/`catch` or a `throws` declaration. This produces cleaner call sites but shifts the responsibility for knowing what can fail entirely onto documentation and discipline.

~~~kotlin
fun check(s: String) {
    if (s == "wrong") throw Exception("Wrong value!")
}

fun main() {
    try {
        check("wrong")
    } catch (e: Exception) {
        println(e.message)  // Wrong value!
    }
}
~~~

### 2. require() and check() vs Manual Throwing

`require()` throws `IllegalArgumentException` for bad input, `check()` throws `IllegalStateException` for bad internal state -- both take a lazy message lambda. They replace the common Java pattern of `if (...) throw new IllegalArgumentException(...)` with a one-line idiom.

~~~kotlin
fun processAge(age: Int) {
    require(age >= 0) { "Age cannot be negative!" }
}
~~~
