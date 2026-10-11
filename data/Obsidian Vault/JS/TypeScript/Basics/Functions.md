## 1. Function parameters

TypeScript lets you specify the type of each function parameter and the function's return value. This is one of the places where explicit types are especially useful because they define the contract of the function

```ts
function add(a: number, b: number): number {
    return a + b;
}

const result = add(10, 20);

console.log(result); // 30
```

The syntax is:

```txt
function name(parameter: type): returnType {
    ...
}
```

For example:

```ts
function greet(name: string): string {
    return `Hello, ${name}!`;
}

console.log(greet("Alice"));
```

TypeScript prevents incorrect calls:

```ts
greet("Alice"); // OK
greet(100);     // ERROR
```

Compared with Java, the main difference is where the return type appears.

Java:

```java
String greet(String name) {
    return "Hello, " + name;
}
```

TypeScript:

```ts
function greet(name: string): string {
    return `Hello, ${name}`;
}
```

TypeScript can also infer the return type:

```ts
function multiply(a: number, b: number) {
    return a * b;
}
```

Still, explicitly declaring return types is often useful for public methods, services, and APIs because it makes the intended contract clear.

## 2. Optional

TypeScript supports optional parameters using `?`.

```ts
function greet(name: string, title?: string): string {
    if (title) {
        return `Hello, ${title} ${name}`;
    }

    return `Hello, ${name}`;
}
```

Both calls are valid:

```ts
console.log(greet("Alice"));
console.log(greet("Alice", "Dr."));
```

An optional parameter should normally come after required parameters:

```ts
function createUser(
    name: string,
    age?: number
): void {
    console.log(name, age);
}
```

You can also provide a default value:

```ts
function greet(
    name: string,
    greeting: string = "Hello"
): string {
    return `${greeting}, ${name}!`;
}
```

Now:

```ts
console.log(greet("Alice"));
// Hello, Alice!

console.log(greet("Alice", "Welcome"));
// Welcome, Alice!
```

This is similar to method overloading/default behavior you might implement explicitly in Java, although Java itself doesn't support default parameter values.

For example, in Java you might write:

```java
String greet(String name) {
    return greet(name, "Hello");
}

String greet(String name, String greeting) {
    return greeting + ", " + name;
}
```

TypeScript can simply use:

```ts
function greet(
    name: string,
    greeting: string = "Hello"
): string {
    return `${greeting}, ${name}`;
}
```

## 3. Arrow functions

You'll encounter arrow functions constantly in TypeScript and especially in Angular.

A normal function:

```ts
function add(a: number, b: number): number {
    return a + b;
}
```

can be written as:

```ts
const add = (a: number, b: number): number => {
    return a + b;
};
```

For a single expression, it can be shortened further:

```ts
const add = (a: number, b: number): number => a + b;
```

For example:

```ts
const greet = (name: string): string => `Hello, ${name}`;

console.log(greet("Alice"));
```

Arrow functions become particularly common when working with arrays:

```ts
const numbers = [1, 2, 3, 4];

const doubled = numbers.map(
    (number: number): number => number * 2
);

console.log(doubled);
```

Output:

```txt
[2, 4, 6, 8]
```

Usually, inference allows us to simplify this:

```ts
const numbers = [1, 2, 3, 4];

const doubled = numbers.map(number => number * 2);
```

TypeScript already knows that `number` is a `number` because `numbers` is a `number[]`.

This is conceptually close to Java:

```java
List<Integer> doubled = numbers.stream()
    .map(number -> number * 2)
    .toList();
```

A function that doesn't return a useful value usually has return type `void`.

```ts
function printMessage(message: string): void {
    console.log(message);
}
```

For example:

```ts
function logUser(name: string, age: number): void {
    console.log(`${name} is ${age} years old`);
}

logUser("Alice", 30);
```

This is comparable to Java:

```java
void logUser(String name, int age) {
    System.out.println(name + " is " + age + " years old");
}
```

TypeScript also has a `never` type.

It represents a function that never completes normally.

A common example is a function that always throws an exception:

```ts
function fail(message: string): never {
    throw new Error(message);
}
```

Because execution always stops at:

```ts
throw new Error(message);
```

the function can never return a value.

Another example is an infinite loop:

```ts
function runForever(): never {
    while (true) {
        // keep running
    }
}
```

So don't confuse:

```
void
```

with:

```
never
```

`void` means:

> The function completes, but doesn't return a useful value.

`never` means:

> The function never completes normally.

For example:

```ts
function printHello(): void {
    console.log("Hello");
}

function crash(): never {
    throw new Error("Application crashed");
}
```

## 4. Complete example

Create:

```sh
touch functions.ts
```

Add:

```ts
// Function with parameter and return types
function add(a: number, b: number): number {
    return a + b;
}

// Optional parameter
function greet(name: string, title?: string): string {
    if (title) {
        return `Hello, ${title} ${name}!`;
    }

    return `Hello, ${name}!`;
}

// Default parameter
function calculatePrice(
    price: number,
    quantity: number = 1
): number {
    return price * quantity;
}

// void
function printResult(value: number): void {
    console.log(`Result: ${value}`);
}

// Arrow function
const multiply = (a: number, b: number): number => {
    return a * b;
};

// Short arrow function
const square = (value: number): number => value * value;


// Usage

const result = add(10, 20);

printResult(result);

console.log(greet("Alice"));
console.log(greet("Alice", "Dr."));

console.log(calculatePrice(10));
console.log(calculatePrice(10, 5));

console.log(multiply(4, 5));
console.log(square(6));
```

Compile and run:

```sh
npx tsc functions.ts

node functions.js
```

Output:

```txt
Result: 30
Hello, Alice!
Hello, Dr. Alice!
10
50
20
36
```

### Java → TypeScript comparison

|Concept|Java|TypeScript|
|---|---|---|
|Parameter|`String name`|`name: string`|
|Return type|`String greet()`|`greet(): string`|
|No return value|`void`|`void`|
|Optional parameter|Usually overloads|`name?: string`|
|Default parameter|Not directly supported|`count = 1`|
|Lambda|`x -> x * 2`|`x => x * 2`|
|Never returns normally|No direct equivalent type|`never`|

One syntax is especially worth becoming comfortable with:

```
const calculate = (value: number): number => value * 2;
```
