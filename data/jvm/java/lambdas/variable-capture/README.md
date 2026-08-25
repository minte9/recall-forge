# JVM / Java / Lambdas / Variable Capture

## 1. Effectively Final Variables

A lambda can reference local variables only if they're final or "effectively final" -- assigned exactly once, never reassigned. This exists because a lambda may outlive the method that created it, so the compiler captures a snapshot of the value at creation time, not a live reference to a mutable variable.

```java
int count = 5; // never reassigned after this -> effectively final
Supplier<Integer> supplier = () -> count * 2;
System.out.println(supplier.get()); // 10

// count = 10; // would break compilation: no longer effectively final
```


## 2. Capturing this vs Instance State

Inside a lambda, `this` refers to the enclosing instance, not the lambda itself -- lambdas don't introduce their own `this` the way anonymous classes do. A lambda inside an instance method reaches the outer object's fields directly, with no qualification needed.

```java
class Counter {
    private int value = 0;

    Supplier<Integer> incrementer() {
        return () -> ++this.value; // "this" is the Counter instance, not the lambda
    }
}

Counter counter = new Counter();
Supplier<Integer> increment = counter.incrementer();
System.out.println(increment.get()); // 1
System.out.println(increment.get()); // 2
```


## 3. Common Capture Pitfalls in Loops

Capturing a loop counter directly inside a lambda created in the loop body fails to compile -- a traditional `for (int i...)` reassigns `i` each iteration, so it's not effectively final. Fix: copy it into a fresh local variable inside the loop body before creating the lambda.

```java
List<Supplier<Integer>> suppliers = new ArrayList<>();

// for (int i = 0; i < 3; i++) {
//     suppliers.add(() -> i); // compile error: i is not effectively final
// }

for (int i = 0; i < 3; i++) {
    int captured = i; // fresh effectively-final copy each iteration
    suppliers.add(() -> captured);
}
```
