## JVM / Kotlin / Interop & Tooling / Java Interoperability

### 1. Calling Java from Kotlin (Platform Types)

Calling existing Java code from Kotlin works directly -- no wrapper needed. But Java types carry no null information, so Kotlin treats them as "platform types" (`String!`), suppressing the compiler's null checks for that value. This is the main place `NullPointerException` can still sneak into Kotlin code.

### 2. Calling Kotlin from Java (@JvmStatic, etc.)

Kotlin compiles to ordinary JVM bytecode, so Java can call it back -- but companion object members need `@JvmStatic` to appear as real static methods to Java (otherwise Java sees `Foo.Companion.bar()`), and top-level functions land on a synthetic `FooKt` class. These annotations exist specifically to make Kotlin libraries feel natural from Java call sites.

~~~kotlin
class ClassRoom {
    companion object {
        @JvmStatic
        fun create() = ClassRoom()  // callable as ClassRoom.create() from Java
    }
}
~~~
