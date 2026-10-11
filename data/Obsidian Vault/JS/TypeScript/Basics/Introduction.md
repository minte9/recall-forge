## 1. What is TypeScript

Microsoft TypeScript is a programming language built on top of JavaScript. TypeScript adds features such as static types, interfaces, generics, enums, and better compile-time checking. 
Browsers and Node.js traditionally execute JavaScript, not TypeScript source directly. 

```txt
TypeScript (.ts)
      ↓
 TypeScript compiler (tsc)
      ↓
JavaScript (.js)
      ↓
Node.js / Browser
```

For example, JavaScript allows a variable to be changed from a string to a number. JavaScript permits this because it is dynamically typed. 

```js
let username = "John";
username = 10;
```

TypeScript reports an error during compilation:

```ts
let username: string = "John";
username = 10;

// Type 'number' is not assignable to type 'string'.
```

## 2. Static Typing

TypeScript's type system will feel familiar to Java developers.

```java
String username = "John";
int age = 35;
boolean active = true;
```

The equivalent TypeScript is:

```ts
let username: string = "John";
let age: number = 35;
let active: boolean = true;
```

Notice an important syntax difference:

```txt
Java
Type variable

String username
int age


TypeScript
variable: type

username: string
age: number
```

Unlike Java, however, TypeScript frequently uses type inference. You don't always have to specify the type:

```ts
let username = "John";
let age = 35;
let active = true;
```

TypeScript infers:

```txt
username → string
age      → number
active   → boolean
```

So this still fails, because TypeScript inferred `username` as a `string`.

```ts
let username = "John";
username = 100;
```

This distinction is important, both variables are effectively treated as strings.

```ts
// Explicit typing
let username: string = "John";

// Type inference
let username2 = "John";
```

## 3. Installing TypeScript

First, verify whether Node.js and npm are installed. If they aren't installed, on Ubuntu you can install the distribution packages with. 

```sh
node --version
npm --version

sudo apt update
sudo apt install nodejs npm
```

For learning TypeScript, create a dedicated project and initialize an npm project.

```sh
mkdir typescript-demo
cd typescript-demo

npm init -y
```

Then install TypeScript as a development dependency and check the compiler.

```sh
npm install --save-dev typescript

npx tsc --version
```

I recommend the local installation rather than:

```sh
sudo npm install -g typescript
```

Real projects normally declare the TypeScript version they depend on.

Create your first file:

```sh
touch hello.ts
```

Add:

```ts
const message: string = "Hello TypeScript!";

console.log(message);
```

Compile it:

```sh
npx tsc hello.ts
```

You should now have:

```txt
typescript-learning/
├── hello.ts
├── hello.js
├── node_modules/
├── package.json
└── package-lock.json
```

Look at `hello.js`:

```js
"use strict";
const message = "Hello TypeScript!";
console.log(message);
```

Run the generated JavaScript:

```sh
node hello.js
```

Output:

```txt
Hello TypeScript!
```

So the complete process is:

```
hello.ts
   │
   │ npx tsc hello.ts
   ▼
hello.js
   │
   │ node hello.js
   ▼
Hello TypeScript!
```

 Try a compiler error, change `hello.ts`:

```ts
let message: string = "Hello TypeScript!";

message = 100;

console.log(message);
```

Compile:

```
npx tsc hello.ts
```

You'll get an error similar to:

```
Type 'number' is not assignable to type 'string'.
```

This demonstrates one of the central reasons for using TypeScript: the compiler finds type errors before the JavaScript reaches your application at runtime.