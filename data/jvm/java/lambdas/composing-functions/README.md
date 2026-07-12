## JVM / Java / Lambdas / Composing Functions

### 1. Function.andThen and compose

`Function` offers `andThen(after)` (run this, then feed result into `after`) and `compose(before)` (run `before` first, feed result into this) -- they differ only in which runs first. Chaining functions builds a pipeline out of reusable pieces instead of one lambda doing several unrelated things.

~~~java
Function<Integer, Integer> timesTwo = x -> x * 2;
Function<Integer, Integer> plusThree = x -> x + 3;

timesTwo.andThen(plusThree).apply(5); // (5*2)+3  = 13 -- timesTwo runs first
timesTwo.compose(plusThree).apply(5); // (5+3)*2 = 16 -- plusThree runs first
~~~


### 2. Predicate.and/or/negate

`Predicate` offers `and`, `or`, and `negate()` as default methods for combining conditions -- `isEven.and(isPositive)` reads naturally left to right. They short-circuit the same way `&&`/`||` do, so the second predicate isn't evaluated once the result is already known.

~~~java
Predicate<Integer> isEven = n -> n % 2 == 0;
Predicate<Integer> isPositive = n -> n > 0;

Predicate<Integer> isEvenAndPositive = isEven.and(isPositive);
System.out.println(isEvenAndPositive.test(4));  // true
System.out.println(isEvenAndPositive.test(-4)); // false
~~~


### 3. Chaining Consumers with andThen

`Consumer` has `andThen(after)` too, running two side-effecting actions in sequence on the same input -- log it, then also save it -- without a lambda that manually calls both. If either consumer throws, the chain stops there.

~~~java
Consumer<String> logIt = s -> System.out.println("LOG: " + s);
Consumer<String> shoutIt = s -> System.out.println(s.toUpperCase() + "!");

logIt.andThen(shoutIt).accept("hello");
// LOG: hello
// HELLO!
~~~
