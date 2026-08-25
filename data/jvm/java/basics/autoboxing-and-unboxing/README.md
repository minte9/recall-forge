# JVM / Java / Basics / Autoboxing and Unboxing

## 1. Autoboxing and Unboxing Basics

Autoboxing automatically wraps a primitive into its wrapper class (int -> Integer) when an object is needed, like adding to a List<Integer>; unboxing does the reverse. The compiler inserts these conversions silently, which is convenient but has real costs -- both performance (extra object allocation) and correctness (see next topics).

```java
List<Integer> numbers = new ArrayList<>();
numbers.add(5); // autoboxing: int -> Integer

int first = numbers.get(0); // unboxing: Integer -> int
```


## 2. The Integer Cache (-128 to 127)

Java caches boxed Integer values from -128 to 127 and reuses those instances, so == comparison on values in that range can appear to work by coincidence. Outside that range, autoboxing creates new objects each time, and == compares references, not values -- a classic source of confusing bugs.

```java
Integer a = 100, b = 100;
System.out.println(a == b); // true -- both come from the cache

Integer c = 200, d = 200;
System.out.println(c == d);          // false -- outside the cache, different objects
System.out.println(c.equals(d));     // true -- always compare wrapper values with equals()
```


## 3. NullPointerException from Unboxing null

Unboxing a null wrapper throws NullPointerException at the point of unboxing, which can happen invisibly -- e.g. a Map<String, Boolean> lookup returning null gets unboxed in an if condition and crashes. Watch for this whenever a wrapper type feeds into a primitive context (arithmetic, conditions, array indices).

```java
Integer value = null;
// int x = value; // throws NullPointerException at unboxing, not at assignment site

Map<String, Boolean> flags = new HashMap<>();
if (flags.get("enabled")) { // NPE if "enabled" isn't in the map -- get() returns null
    // ...
}
```
