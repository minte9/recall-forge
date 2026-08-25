# JVM / Java / Lambdas / Functional Interfaces

## 1. The @FunctionalInterface Contract

A functional interface has exactly one abstract method (SAM) -- that's what a lambda actually implements. `@FunctionalInterface` is optional but recommended: it makes the compiler reject a second abstract method being added later, catching a breaking change immediately. Default/static methods don't count toward the one-method limit.

```java
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b); // the single abstract method

    default Calculator andThenDouble() { // default methods are fine, don't break the rule
        return (a, b) -> calculate(a, b) * 2;
    }
}

Calculator add = (a, b) -> a + b;
System.out.println(add.calculate(2, 3)); // 5
```


## 2. Built-in Interfaces: Function, Predicate, Consumer, Supplier

`java.util.function` covers most cases so custom interfaces are rarely needed: `Function<T,R>` transforms, `Predicate<T>` tests, `Consumer<T>` acts (no return), `Supplier<T>` produces (no args). Each has `Bi-` two-arg variants and primitive-specialized ones (`IntPredicate`, `ToIntFunction`) that avoid boxing.

```java
Function<String, Integer> length = String::length; // T -> R
Predicate<String> isEmpty = String::isEmpty;        // T -> boolean
Consumer<String> print = System.out::println;       // T -> void
Supplier<String> greeting = () -> "Hello";           // () -> T
```


## 3. Custom Functional Interfaces

Write a custom functional interface when nothing in `java.util.function` fits -- a more descriptive name, a method that throws a checked exception (none of the built-ins do), or more than two parameters (the standard library stops at `Bi-`). Any single-abstract-method interface works as a lambda target.

```java
@FunctionalInterface
interface TriFunction<A, B, C, R> { // built-ins only go up to two params
    R apply(A a, B b, C c);
}

TriFunction<Integer, Integer, Integer, Integer> sumThree = (a, b, c) -> a + b + c;
System.out.println(sumThree.apply(1, 2, 3)); // 6
```
