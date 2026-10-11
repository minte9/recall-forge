## 1. Syntax

TypeScript uses essentially the same control-flow syntax as Java and JavaScript. An `if` statement evaluates a condition and executes a block when that condition is true.

```ts
const age = 25;

if (age >= 18) {
    console.log("Adult");
} else {
    console.log("Minor");
}
```

Multiple conditions use `else if`:

```ts
const score = 85;

if (score >= 90) {
    console.log("Excellent");
} else if (score >= 70) {
    console.log("Good");
} else if (score >= 50) {
    console.log("Pass");
} else {
    console.log("Fail");
}
```

Logical operators are also familiar from Java:

```ts
const age = 25;
const active = true;

if (age >= 18 && active) {
    console.log("Active adult user");
}

if (age < 18 || !active) {
    console.log("Access restricted");
}
```

The operators are:

```tx
&&    AND
||    OR
!     NOT
```

One JavaScript/TypeScript difference worth knowing is equality. You should normally use strict equality:

```ts
const age = 30;

if (age === 30) {
    console.log("Age is 30");
}

if (age !== 20) {
    console.log("Age is not 20");
}
```

Prefer:

```txt
===     equal value and compatible type
!==     not equal
```

over JavaScript's older coercing operators:

```
==
!=
```

For example, JavaScript's loose equality can produce surprising results. TypeScript may also warn about comparisons it can already determine are incompatible.

```ts
console.log(5 == "5");   // true in JavaScript
console.log(5 === "5");  // false
```


A `switch` statement is useful when one value has several possible cases:

```ts
const role = "admin";

switch (role) {
    case "admin":
        console.log("Full access");
        break;

    case "editor":
        console.log("Edit access");
        break;

    case "viewer":
        console.log("Read-only access");
        break;

    default:
        console.log("Unknown role");
}
```

This is almost identical to traditional Java `switch` syntax.

## 2. Loops

The traditional `for` loop works just like Java:

```ts
for (let i = 0; i < 5; i++) {
    console.log(i);
}
```

Output:

```
0
1
2
3
4
```

You can use it with arrays:

```ts
const languages = ["Java", "TypeScript", "Python"];

for (let i = 0; i < languages.length; i++) {
    console.log(languages[i]);
}
```

But TypeScript frequently uses `for...of` when you simply want each value:

```ts
const languages = ["Java", "TypeScript", "Python"];

for (const language of languages) {
    console.log(language);
}
```

This is conceptually similar to Java's enhanced `for`:

```java
for (String language : languages) {
    System.out.println(language);
}
```

So the mental mapping is straightforward:

```txt
Java                          TypeScript

for (String item : items)     for (const item of items)
```

TypeScript/JavaScript also has `for...in`, but it means something different.

Consider:

```ts
const languages = ["Java", "TypeScript", "Python"];

for (const index in languages) {
    console.log(index);
}
```

This prints the **keys/indexes**:

```
0
1
2
```

While:

```ts
for (const language of languages) {
    console.log(language);
}
```

prints:

```
Java
TypeScript
Python
```

Therefore, remember:

```txt
for...of → values
for...in → keys
```

For arrays, `for...of` is normally what you want.

You will also frequently encounter functional array operations:

```ts
const numbers = [1, 2, 3, 4];

numbers.forEach(number => {
    console.log(number);
});
```

and later:

```ts
const doubled = numbers.map(number => number * 2);

const even = numbers.filter(number => number % 2 === 0);
```

These will be covered more thoroughly in Modern TypeScript Features.

## 3. Type Narrowing

This is where TypeScript becomes more interesting than simply "JavaScript with type annotations."

Suppose a variable can contain either a string or a number:

```ts
let value: string | number;
```

This is called a **union type**, which we'll study properly later.

The variable can hold either:

```ts
value = "Hello";
```

or:

```ts
value = 100;
```

Now consider a function:

```ts
function printValue(value: string | number): void {
    console.log(value);
}
```

Inside the function, TypeScript initially only knows:

```txt
value → string OR number
```

Therefore, you cannot blindly use operations that only exist on strings.

For example:

```ts
function printValue(value: string | number): void {
    console.log(value.toUpperCase());
}
```

TypeScript rejects this because `number` doesn't have `toUpperCase()`.

But now add an `if`:

```ts
function printValue(value: string | number): void {
    if (typeof value === "string") {
        console.log(value.toUpperCase());
    }
}
```

Inside this block:

```ts
if (typeof value === "string") {
    // value is known to be string here
}
```

TypeScript has **narrowed**:

```txt
string | number
       ↓
     string
```

We can handle both possibilities:

```ts
function printValue(value: string | number): void {
    if (typeof value === "string") {
        console.log(value.toUpperCase());
    } else {
        console.log(value * 2);
    }
}
```

TypeScript understands:

```ts
                 value
                   │
           string | number
                   │
        typeof value === "string"
              /           \
           true           false
            │               │
         string           number
```

So this is valid:

```ts
function process(value: string | number): void {
    if (typeof value === "string") {
        console.log(value.toUpperCase());
        console.log(value.length);
    } else {
        console.log(value.toFixed(2));
        console.log(value * 10);
    }
}
```

Call it with:

```ts
process("typescript");
process(12.5);
```

Output:

```
TYPESCRIPT
10
12.50
125
```

This is called **type narrowing**.

Another very common case involves values that might be `null`.

For example:

```ts
function printUsername(username: string | null): void {
    if (username !== null) {
        console.log(username.toUpperCase());
    }
}
```

Initially:

```txt
username → string | null
```

After:

```ts
if (username !== null)
```

TypeScript knows:

```ts
username → string
```

This becomes extremely useful with backend data where a Spring Boot DTO might contain nullable fields.

For example, imagine Spring Boot returns:

```json
{
    "id": 1,
    "name": "Alice",
    "phone": null
}
```

Your TypeScript model could eventually represent that as:

```ts
interface User {
    id: number;
    name: string;
    phone: string | null;
}
```

Before using `phone` as a string:

```ts
if (user.phone !== null) {
    console.log(user.phone.toUpperCase());
}
```

TypeScript protects you from accidentally treating `null` as a string.

Narrowing can also happen with a `switch`.

Consider:

```ts
type Status = "pending" | "approved" | "rejected";

function printStatus(status: Status): void {
    switch (status) {
        case "pending":
            console.log("Waiting for approval");
            break;

        case "approved":
            console.log("Request approved");
            break;

        case "rejected":
            console.log("Request rejected");
            break;
    }
}
```

## 4. Complete example

Create:

```
touch control-flow.ts
```

Add:

```ts
const users = [
    "Alice",
    "Bob",
    "Charlie"
];

// Traditional for loop
for (let i = 0; i < users.length; i++) {
    console.log(`User ${i}: ${users[i]}`);
}

console.log("---");

// for...of
for (const user of users) {
    console.log(user);
}

console.log("---");

// if / else
const score = 85;

if (score >= 90) {
    console.log("Excellent");
} else if (score >= 70) {
    console.log("Good");
} else {
    console.log("Needs improvement");
}

console.log("---");

// Type narrowing
function processValue(value: string | number): void {
    if (typeof value === "string") {
        console.log("String value:");
        console.log(value.toUpperCase());
    } else {
        console.log("Number value:");
        console.log(value * 2);
    }
}

processValue("typescript");
processValue(25);

// Nullable value
function printEmail(email: string | null): void {
    if (email !== null) {
        console.log(`Email: ${email.toLowerCase()}`);
    } else {
        console.log("No email available");
    }
}

printEmail("ALICE@EXAMPLE.COM");
printEmail(null);
```

Compile and run

```sh
npx tsc control-flow.ts

node control-flow.js
```

You'll get output similar to:

```txt
User 0: Alice
User 1: Bob
User 2: Charlie
Alice
Bob
Charlie
Good
String value:
TYPESCRIPT
Number value:
50
Email: alice@example.com
No email available
```
