# JVM / Java / Basics / String Pool

## 1. Why Strings Are Immutable

A String's characters can never change after creation -- every "modifying" method (substring, concat, replace) returns a new String object. Immutability makes strings safe to share freely (as HashMap keys, across threads) without defensive copying, and is what makes the string pool in the next topic possible at all.

```java
String s = "hello";
s.toUpperCase(); // returns a NEW string -- s itself is unchanged
System.out.println(s); // hello

String upper = s.toUpperCase(); // must capture the return value to use it
```


## 2. The String Pool and intern()

String literals are automatically interned -- stored once in a shared pool, so two identical literals reference the same object. new String("x") explicitly creates a new, separate object outside the pool, even with the same content. intern() manually adds a string to the pool and returns the pooled reference.

```java
String a = "hello";
String b = "hello";
System.out.println(a == b); // true -- both point to the same pooled literal

String c = new String("hello");
System.out.println(a == c);          // false -- c is a separate object
System.out.println(a == c.intern()); // true -- intern() returns the pooled reference
```


## 3. StringBuilder for Concatenation in Loops

Because strings are immutable, concatenating in a loop with + creates a new String object on every iteration, which is O(n^2) for n concatenations. StringBuilder mutates an internal buffer in place instead, making repeated concatenation O(n) -- always prefer it inside loops.

```java
String result = "";
for (int i = 0; i < 1000; i++) {
    result += i; // creates a new String object every iteration -- slow
}

StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) {
    sb.append(i); // mutates the same buffer -- fast
}
```
