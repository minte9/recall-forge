## JVM / Kotlin / Collections / Arrays

### 1. Fixed-Size Arrays

`arrayOf()` (or `IntArray`/`DoubleArray` for primitives, avoiding boxing) creates a fixed-size array, same as Java -- size can't grow or shrink, and an out-of-bounds access throws, it doesn't silently return null or resize.

~~~kotlin
val nums = IntArray(2)
nums[0] = 1
nums[1] = 2

val names = arrayOf("John", "Mary", "Ana")
println(names[2])  // Ana
~~~

### 2. joinToString() vs String.join

`joinToString()` is available on any array or collection and covers what Java's `String.join()` plus a `Collectors.joining()` stream does -- with extra optional prefix/suffix/limit parameters built in, not bolted on via a separate collector.

~~~kotlin
val a = arrayOf("a", "b", "c")
println(a.joinToString(", "))  // a, b, c
~~~
