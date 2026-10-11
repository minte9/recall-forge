## 1. Primitive types

TypeScript has several basic types that you'll use constantly. The most common are `string`, `number`, and `boolean`.

```ts
let username: string = "John";
let age: number = 35;
let active: boolean = true;

console.log(username);
console.log(age);
console.log(active);
```

Unlike Java, TypeScript doesn't have separate numeric types such as `int`, `long`, `float`, and `double`. Regular JavaScript numbers use the TypeScript type `number`.

For comparison:

```java
int age = 35;
double price = 19.99;
```

TypeScript:

```ts
let age: number = 35;
let price: number = 19.99;
```

You can also explicitly use `null` and `undefined`:

```ts
let nothing: null = null;
let notAssigned: undefined = undefined;
```

Their importance becomes clearer once we enable TypeScript's strict mode and work with optional values.

## 2. Arrays and Tuples

Arrays can be declared in two common ways:

```ts
let numbers: number[] = [10, 20, 30];
let names: string[] = ["Alice", "Bob", "John"];

console.log(numbers);
console.log(names);
```

Alternatively, you can use the generic `Array<T>` syntax:

```ts
let numbers: Array<number> = [10, 20, 30];
let names: Array<string> = ["Alice", "Bob", "John"];
```

You can think of the second syntax as being somewhat similar to Java syntax:

```java
List<String> names = List.of("Alice", "Bob", "John");
```

TypeScript also has tuples. A tuple is an array where the positions have predefined types.

For example:

```ts
let user: [number, string] = [1, "Alice"];

console.log(user[0]); // 1
console.log(user[1]); // Alice
```

TypeScript knows that:

```txt
user[0] → number
user[1] → string
```

Therefore this is invalid, because the order matters.

```ts
let user: [number, string] = ["Alice", 1];
```

A practical example might be:

```ts
let response: [number, string] = [200, "OK"];

console.log(`Status: ${response[0]}`);
console.log(`Message: ${response[1]}`);
```

For application/domain objects, however, we'll usually prefer interfaces or object types rather than tuples:

```ts
// We'll study this properly later.
interface User {
    id: number;
    name: string;
}
```

## 3. Type inference

One of the biggest differences you'll notice coming from Java is that TypeScript doesn't require explicit type declarations everywhere.

Instead of:

```ts
let username: string = "Alice";
let age: number = 30;
let active: boolean = true;
```

you can write:

```ts
let username = "Alice";
let age = 30;
let active = true;
```

TypeScript automatically infers:

```txt
username → string
age      → number
active   → boolean
```

This doesn't mean the variables are dynamically typed.

For example:

```ts
let age = 30;

age = 40;      // OK
age = 50;      // OK
age = "fifty"; // ERROR
```

TypeScript inferred `age` as `number`, so assigning a string later is rejected.

Inference also works with arrays:

```ts
let scores = [10, 20, 30];

scores.push(40);      // OK
scores.push("fifty"); // ERROR
```

You can verify the same idea with strings:

```ts
let languages = ["Java", "TypeScript", "Python"];

languages.push("Kotlin"); // OK
languages.push(100);      // ERROR
```

In real TypeScript code, you normally allow inference when the type is obvious:

```ts
const name = "Alice";
const age = 30;
const enabled = true;
```

rather than unnecessarily writing:

```ts
const name: string = "Alice";
const age: number = 30;
const enabled: boolean = true;
```

But explicit types become very useful when defining function parameters, API models, interfaces, and other contracts:

```ts
function printUser(name: string, age: number): void {
    console.log(`${name} is ${age} years old.`);
}

printUser("Alice", 30);
```

That distinction will be important when you later consume DTOs coming from a Spring Boot backend.

## 4. Complete example

Create:

```sh
touch basic-types.ts
```

```ts
// Primitive types
const username: string = "Alice";
const age: number = 30;
const active: boolean = true;

// Arrays
const languages: string[] = [
    "Java",
    "TypeScript",
    "Python"
];

const scores: number[] = [90, 85, 95];

// Tuple
const user: [number, string] = [1, "Alice"];

// Type inference
const city = "Bucharest";
const temperature = 22.5;

// Output
console.log("Username:", username);
console.log("Age:", age);
console.log("Active:", active);

console.log("Languages:", languages);
console.log("Scores:", scores);

console.log("User ID:", user[0]);
console.log("User name:", user[1]);

console.log("City:", city);
console.log("Temperature:", temperature);
```

Compile and run:

```sh
npx tsc basic-types.ts
node basic-types.js
```

You should get approximately:

```txt
Username: Alice
Age: 30
Active: true
Languages: [ 'Java', 'TypeScript', 'Python' ]
Scores: [ 90, 85, 95 ]
User ID: 1
User name: Alice
City: Bucharest
Temperature: 22.5
```

A useful mental mapping from Java is:

| Java                             | TypeScript                            |
| -------------------------------- | ------------------------------------- |
| `String`                         | `string`                              |
| `int`, `long`, `float`, `double` | `number`                              |
| `boolean`                        | `boolean`                             |
| `String[]`                       | `string[]`                            |
| `List<String>`                   | commonly `string[]` / `Array<string>` |
| `null`                           | `null`                                |
| no direct equivalent             | `undefined`                           |
| inferred `var`                   | type inference with `let` / `const`   |
|                                  |                                       |

The important principle is that TypeScript is statically type-checked, but its inference means you don't need to write types everywhere.