# JVM / Java / Exceptions / Common Runtime Exceptions

## 1. ArrayIndexOutOfBoundsException

Thrown when accessing an array index that's negative or >= the array's length -- a classic off-by-one bug, especially with loops using <= instead of < against array.length. Unlike a List, an array has no bounds-checked alternative like get(); the index is always used directly.

```java
int[] numbers = {1, 2, 3};
System.out.println(numbers[3]); // throws: index 3 out of bounds for length 3

for (int i = 0; i <= numbers.length; i++) { // off-by-one: should be <
    // System.out.println(numbers[i]); // fails on the last iteration
}
```


## 2. ClassCastException

Thrown when casting an object to a type it isn't actually an instance of at runtime -- the compiler allows the cast syntactically if there's a valid inheritance relationship, but the actual runtime type is checked when the cast executes. Pattern matching for instanceof (see Versions / Java 17) avoids this by combining the check and cast.

```java
Object obj = "a string";
Integer number = (Integer) obj; // compiles, but throws ClassCastException at runtime

if (obj instanceof Integer i) { // safe: only casts if the check passes
    System.out.println(i);
}
```


## 3. NumberFormatException

Thrown by Integer.parseInt(), Double.parseDouble(), and similar methods when the input string isn't a valid number -- extra whitespace, letters, or an empty string all trigger it. Since it's a RuntimeException, it's easy to forget to handle when parsing user input or data from an external source.

```java
int value = Integer.parseInt("abc"); // throws NumberFormatException

try {
    int safe = Integer.parseInt(userInput.trim());
} catch (NumberFormatException e) {
    System.out.println("Invalid number: " + userInput);
}
```
