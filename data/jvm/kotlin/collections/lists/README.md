# JVM / Kotlin / Collections / Lists

## 1. Immutable vs Mutable Lists

`listOf()` returns a read-only `List` -- no `add`/`remove` methods exist on its type at all, unlike Java's `Collections.unmodifiableList` which still exposes those methods and just throws at runtime. `mutableListOf()` is the Kotlin equivalent of `ArrayList`, fully modifiable.

```kotlin
val fixed = listOf(1, 2, 3)          // no add() available
val flexible = mutableListOf(1, 2, 3)
flexible.add(4)
```

## 2. sorted() vs sort()

`sorted()` (on an immutable `List`) returns a *new* sorted list, leaving the original untouched. `sort()` (on a `MutableList`) sorts in place. Mixing these up is a common bug source -- calling `sorted()` and discarding the result does nothing.

```kotlin
data class Person(val name: String, val age: Int)

val people = listOf(Person("Ana", 30), Person("Bo", 20))
val byAge = people.sortedBy { it.age }  // new list, 'people' unchanged
```
