## JVM / Java / Lambdas / Lambda Syntax

### 1. Lambda Expression Forms

A lambda has the shape `(params) -> body`. A single parameter can drop parentheses (`x -> x * 2`); zero or multiple params require them. Parameter types are almost always inferred, but can be written explicitly -- though it's all-or-nothing across a lambda's parameter list.

~~~java
Function<Integer, Integer> doubleIt = x -> x * 2;         // single param, no parens
Supplier<String> greet = () -> "Hello";                   // zero params, parens required
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b; // multiple params, parens required
~~~


### 2. Expression Body vs Block Body

An expression body -- `x -> x * 2` -- is a single expression whose value is auto-returned, no braces or `return` needed. A block body -- `x -> { return x * 2; }` -- needs an explicit `return`, and is required once a lambda needs multiple statements or local variables.

~~~java
Function<Integer, Integer> square = x -> x * x; // implicit return, no braces

Function<Integer, Integer> squareVerbose = x -> {
    int result = x * x; // needs a block once there's a local variable
    return result;      // return is now required
};
~~~


### 3. Target Typing and Type Inference

A lambda has no type of its own -- the compiler infers which functional interface it implements from context (a variable's declared type, a parameter, a cast), called "target typing". The same `x -> x * 2` can implement different interfaces depending only on where it's assigned.

~~~java
Function<Integer, Integer> asFunction = x -> x * 2;
UnaryOperator<Integer> asUnaryOperator = x -> x * 2; // same syntax, different target type

System.out.println(asFunction.apply(5));     // 10
System.out.println(asUnaryOperator.apply(5)); // 10
~~~
