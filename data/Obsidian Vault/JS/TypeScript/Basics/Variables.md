## 1. let and const

Modern TypeScript primarily uses two keywords for declaring variables: `let` and `const`.

Use `let` when the value needs to change:

```ts
let score: number = 10;

score = 20;
score = 30;

console.log(score); // 30
```

Use `const` when the variable will not be reassigned:

```ts
const username: string = "Alice";

console.log(username);
```

This is not allowed:

```ts
const username: string = "Alice";

username = "Bob";
// Error: Cannot assign to 'username'
// because it is a constant.
```

A good rule for TypeScript is:

> **Use `const` by default. Use `let` only when you know the variable needs to be reassigned.**

For example:

```ts
const username = "Alice";
const maxAttempts = 3;

let attempts = 0;

attempts++;
```

This makes it immediately obvious which values can change.

Conceptually similar to a local `final` variable in Java:

```
final String username = "Alice";
```

There is an important detail, however. `const` prevents reassignment of the variable, not modification of an object stored inside it.

This is valid:

```ts
const user = {
    name: "Alice",
    age: 30
};

user.age = 31; // OK
```

But this isn't:

```ts
const user = {
    name: "Alice",
    age: 30
};

user = {
    name: "Bob",
    age: 40
};

// ERROR
```

The `user` variable must continue referring to the same object, but that object's properties can still change.

## 2. Inferred Types

TypeScript can usually determine the type from the initial value.

Explicit:

```ts
const username: string = "Alice";
const age: number = 30;
const active: boolean = true;
```

Inferred:

```ts
const username = "Alice";
const age = 30;
const active = true;
```

In both cases, TypeScript understands the types.

For local variables with obvious initial values, inference is usually preferable:

```ts
const name = "Alice";
const age = 30;

let score = 0;

score = 10; // OK
score = 20; // OK
```

The compiler still protects the variable:

```ts
let score = 0;

score = "ten";
// Error: Type 'string' is not assignable to type 'number'
```

Explicit types become more valuable when the compiler cannot determine your intended contract.

For example:

```ts
let username: string;

username = "Alice";
```

Or when a value may have more than one allowed type:

```ts
let userId: number | string;

userId = 10;
userId = "USR-10";
```

You'll also see explicit types extensively in function parameters:

```ts
function greet(name: string): string {
    return `Hello ${name}`;
}
```

Here the types communicate an important contract:

```txt
input  → string
output → string
```

This becomes especially valuable in Angular applications where TypeScript models describe data coming from Spring Boot.

## 3. Block scope

TypeScript also supports the older JavaScript keyword `var`:

```
var username = "Alice";
```

In modern TypeScript, you should generally avoid it.

The main reason is scope.

Consider:

```ts
if (true) {
    let message = "Hello";
}

console.log(message);
```

This produces an error because `message` only exists inside the `{ }` block.

The same applies to `const`:

```ts
if (true) {
    const message = "Hello";

    console.log(message); // OK
}

console.log(message); // ERROR
```

This behavior should feel natural coming from Java:

```java
if (true) {
    String message = "Hello";
}

System.out.println(message); // ERROR
```

But `var` behaves differently:

```ts
if (true) {
    var message = "Hello";
}

console.log(message); // Hello
```

`var` is function-scoped, rather than block-scoped.

For example:

```ts
function test(): void {
    if (true) {
        var message = "Hello";
    }

    console.log(message); // works
}
```

With `let`:

```ts
function test(): void {
    if (true) {
        let message = "Hello";
    }

    console.log(message); // ERROR
}
```

For modern TypeScript development, especially Angular, think primarily in terms of:

```ts
const → default choice
let   → value needs reassignment
var   → avoid
```

## 4. Complete example

Create:

```sh
touch variables.ts
```

Add:

```ts
const applicationName = "Product Manager";

let currentUser = "Alice";
let loginCount = 0;

console.log("Application:", applicationName);
console.log("Current user:", currentUser);

loginCount++;
loginCount++;

console.log("Login count:", loginCount);

// Reassignment
currentUser = "Bob";

console.log("Current user:", currentUser);

// Block scope
if (loginCount > 0) {
    const message = "The application has been used.";

    console.log(message);
}

// message is not available here.

// const objects can still be modified
const user = {
    name: "Alice",
    age: 30
};

user.age = 31;

console.log(user);
```

Compile and run:

```sh
npx tsc variables.ts

node variables.js
```

Output:

```txt
Application: Product Manager
Current user: Alice
Login count: 2
Current user: Bob
The application has been used.
{ name: 'Alice', age: 31 }
```

One difference from Java worth remembering is that TypeScript code commonly uses `const` much more aggressively:

```ts
const users = getUsers();
const activeUsers = users.filter(user => user.active);
const count = activeUsers.length;
```

Even though these variables contain objects or arrays, they don't need to be reassigned, so `const` is appropriate.

The basic rule you'll use throughout the roadmap is:

```txt
Need to reassign the variable?
        │
    ┌───┴───┐
   No      Yes
   │         │
 const      let
```