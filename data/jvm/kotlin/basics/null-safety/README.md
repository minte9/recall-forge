# JVM / Kotlin / Basics / Null Safety

## 1. Nullable Types

Every type in Kotlin is non-nullable by default -- `String` can never hold null. To allow null you must opt in explicitly with `?` (`String?`), which pushes the null check to compile time instead of a runtime `NullPointerException` in Java.

```kotlin
var name: String = "John"
name = null              // compile error

var nickname: String? = "J"
nickname = null           // OK, type says it's allowed
```

## 2. Safe Calls and the Elvis Operator

`?.` calls a member only if the receiver isn't null, short-circuiting to null otherwise. `?:` (Elvis) supplies a fallback when the left side is null -- together they replace most of Java's defensive `if (x != null)` chains.

```kotlin
val length = nickname?.length ?: 0  // 0 if nickname is null
```

## 3. !! and Smart Casts

`!!` forces a nullable value to non-null, throwing `NullPointerException` if it actually is null -- it's an explicit "trust me" escape hatch, so it should be rare and deliberate. After a null check, the compiler smart-casts the variable to non-null for the rest of that scope, no manual cast needed.

```kotlin
if (nickname != null) {
    println(nickname.length)  // smart-cast to String, no ?. needed
}
```
