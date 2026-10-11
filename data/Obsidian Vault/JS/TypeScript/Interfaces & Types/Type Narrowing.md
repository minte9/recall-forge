## 1. What Is Type Narrowing?

**Type narrowing** is the process by which TypeScript determines that a value with a broad type has a more specific type in a particular part of the code.

Consider a union:

```ts
function printValue(value: string | number): void {
    // value is string | number
}
```

At this point, TypeScript doesn't know which type `value` actually has.

Therefore this isn't allowed:

```ts
function printValue(value: string | number): void {
    console.log(value.toUpperCase());
    // Error: number doesn't have toUpperCase()
}
```

If we check the type first:

```ts
function printValue(value: string | number): void {
    if (typeof value === "string") {
        console.log(value.toUpperCase());
    } else {
        console.log(value.toFixed(2));
    }
}
```

TypeScript narrows the type. It performs this analysis automatically based on conditions in your code. This is called **control flow analysis**.

```txt
value: string | number
          |
          | typeof value === "string"
          |
     +----+----+
     |         |
    true      false
     |         |
  string     number
```

## 2. Narrowing with typeof

The `typeof` operator works particularly well with primitive values.

```ts
function process(value: string | number | boolean): void {

    if (typeof value === "string") {
        console.log(value.toUpperCase());

    } else if (typeof value === "number") {
        console.log(value.toFixed(2));

    } else {
        console.log(value ? "YES" : "NO");
    }
}
```

Usage:

```ts
process("hello");
process(12.345);
process(true);
```

Output:

```txt
HELLO
12.35
YES
```

TypeScript understands the narrowing in every branch:

```txt
string | number | boolean
           |
           v

typeof === "string"  → string

typeof === "number"  → number

otherwise            → boolean
```

Common results from JavaScript's `typeof` include:

```ts
typeof "hello"     // "string"
typeof 42          // "number"
typeof true        // "boolean"
typeof undefined   // "undefined"
typeof function() {} // "function"
```

There is one famous JavaScript oddity:

```ts
typeof null
```

returns:

```txt
"object"
```

So don't use this to check specifically for `null`:

```ts
if (typeof value === "object") {
    // This could include null!
}
```

Instead:

```ts
if (value === null) {
    console.log("Value is null");
}
```

##  3. Narrowing null

Narrowing is also commonly used with nullable values.

```ts
function printUsername(
    username: string | null
): void {

    if (username === null) {
        console.log("No username");
        return;
    }

    console.log(username.toUpperCase());
}
```

Initially:

```txt
username
   ↓
string | null
```

After:

```ts
if (username === null) {
    return;
}
```

the only possible remaining type is:

```txt
string
```

So TypeScript allows:

```ts
username.toUpperCase();
```

You can do the same with `undefined`:

```ts
function printEmail(
    email: string | undefined
): void {

    if (email === undefined) {
        return;
    }

    console.log(email.toLowerCase());
}
```

A common shortcut is:

```ts
if (email) {
    console.log(email.toLowerCase());
}
```

But remember that this tests **truthiness**, not specifically `undefined`.

For strings, this also excludes:

```ts
""
```

because an empty string is falsy.

When the distinction matters, use the explicit check:

```ts
if (email !== undefined) {
    // ...
}
```

## 4. Narrowing with `instanceof`

The `typeof` works well with primitive values. For class instances, TypeScript can use `instanceof`.

Suppose we have:

```ts
class Admin {
    constructor(
        public username: string,
        public permissions: string[]
    ) {}

    deleteUser(): void {
        console.log("Deleting user...");
    }
}

class Customer {
    constructor(
        public username: string,
        public orders: number
    ) {}

    placeOrder(): void {
        console.log("Placing order...");
    }
}
```

A function can accept either:

```ts
function processUser(
    user: Admin | Customer
): void {

    if (user instanceof Admin) {
        console.log(user.permissions);
        user.deleteUser();
    } else {
        console.log(user.orders);
        user.placeOrder();
    }
}
```

TypeScript understands:

```txt
Admin | Customer
       |
       | user instanceof Admin
       |
   +---+---+
   |       |
 true     false
   |       |
 Admin   Customer
```

For example:

```ts
const admin = new Admin(
    "john",
    ["READ", "WRITE"]
);

processUser(admin);
```

Output:

```txt
[ 'READ', 'WRITE' ]
Deleting user...
```

*Important*: `instanceof` needs a runtime value

This works:

```ts
class Admin {
}
```

because `Admin` exists in the generated JavaScript.

But consider an interface:

```ts
interface Admin {
    username: string;
    permissions: string[];
}
```

You cannot write:

```ts
if (user instanceof Admin) {
}
```

because interfaces disappear when TypeScript is compiled to JavaScript.

Remember:

```txt
class
  ↓
exists at runtime
  ↓
instanceof works


interface
  ↓
compile-time only
  ↓
instanceof cannot be used
```

This distinction is particularly important when dealing with DTOs.

## 5. Narrowing with in

What if we're working with interfaces instead of classes?

Suppose:

```ts
interface Admin {
    username: string;
    permissions: string[];
}

interface Customer {
    username: string;
    orders: number;
}
```

We can use the `in` operator to check whether a property exists:

```ts
function processUser(
    user: Admin | Customer
): void {

    if ("permissions" in user) {
        console.log("Admin");
        console.log(user.permissions);
    } else {
        console.log("Customer");
        console.log(user.orders);
    }
}
```

Here TypeScript understands that:

```ts
"permissions" in user
```

means the object must be an `Admin`.

Conceptually:

```txt
Admin | Customer
       |
       | "permissions" in user
       |
   +---+---+
   |       |
 true     false
   |       |
 Admin   Customer
```

This can be useful with DTO-like objects because interfaces don't exist at runtime. 

A very common and often cleaner approach is to give each object a property identifying its type.

For example:

```ts
interface Admin {
    type: "ADMIN";
    username: string;
    permissions: string[];
}

interface Customer {
    type: "CUSTOMER";
    username: string;
    orders: number;
}

type User = Admin | Customer;
```

Now we can narrow based on `type`:

```ts
function processUser(user: User): void {

    if (user.type === "ADMIN") {
        console.log(user.permissions);
    } else {
        console.log(user.orders);
    }
}
```

The property:

```ts
type: "ADMIN"
```

or:

```ts
type: "CUSTOMER"
```

is called a **discriminant**.

That's why this pattern is called a **discriminated union**.

It's particularly useful when there are several possibilities:

```ts
interface Admin {
    type: "ADMIN";
    permissions: string[];
}

interface Editor {
    type: "EDITOR";
    sections: string[];
}

interface Viewer {
    type: "VIEWER";
    lastViewedPage: string;
}

type User = Admin | Editor | Viewer;
```

Then a `switch` becomes very natural:

```ts
function processUser(user: User): void {

    switch (user.type) {

        case "ADMIN":
            console.log(user.permissions);
            break;

        case "EDITOR":
            console.log(user.sections);
            break;

        case "VIEWER":
            console.log(user.lastViewedPage);
            break;
    }
}
```

Inside every `case`, TypeScript knows the exact type.

This is one of the most useful TypeScript patterns for application development.

## 7. Custom Type Guards

Sometimes the narrowing logic is more complicated and we want to put it inside a reusable function.

Consider:

```ts
interface Admin {
    username: string;
    permissions: string[];
}

interface Customer {
    username: string;
    orders: number;
}

type User = Admin | Customer;
```

We could repeatedly write:

```ts
if ("permissions" in user) {
}
```

Instead, we can create a **custom type guard**:

```ts
function isAdmin(user: User): user is Admin {
    return "permissions" in user;
}
```

The important part is:

```ts
user is Admin
```

This is called a **type predicate**.

It tells TypeScript:

> If this function returns `true`, treat `user` as an `Admin`.

We can now write:

```ts
function processUser(user: User): void {

    if (isAdmin(user)) {
        console.log(user.permissions);
    } else {
        console.log(user.orders);
    }
}
```

The type guard can be reused:

```ts
const users: User[] = [
    {
        username: "john",
        permissions: ["READ", "WRITE"]
    },
    {
        username: "mary",
        orders: 5
    }
];

for (const user of users) {

    if (isAdmin(user)) {
        console.log(
            `${user.username} is an administrator`
        );
    }
}
```

## 8. Type Guards and API Data

Custom type guards become particularly interesting when dealing with external data.

Suppose your Spring Boot API returns:

```json
{
    "id": 1,
    "username": "john",
    "role": "ADMIN"
}
```

We might define:

```ts
type Role =
    | "ADMIN"
    | "EDITOR"
    | "VIEWER";

interface UserDto {
    id: number;
    username: string;
    role: Role;
}
```

However, remember an important point from the interfaces lesson:

```ts
const user: UserDto = await response.json();
```

doesn't magically validate the JSON.

The server could theoretically return:

```txt
{
    "id": "wrong",
    "username": 123,
    "role": "SUPERUSER"
}
```

TypeScript interfaces don't exist at runtime, so TypeScript cannot automatically inspect the response.

A basic runtime type guard could be:

```ts
function isUserDto(value: unknown): value is UserDto {

    if (typeof value !== "object" || value === null) {
        return false;
    }

    if (
        !("id" in value) ||
        !("username" in value) ||
        !("role" in value)
    ) {
        return false;
    }

    return (
        typeof value.id === "number" &&
        typeof value.username === "string" &&
        (
            value.role === "ADMIN" ||
            value.role === "EDITOR" ||
            value.role === "VIEWER"
        )
    );
}
```

Then:

```ts
async function getUser(): Promise<UserDto> {

    const response =
        await fetch("http://localhost:8080/api/users/1");

    const data: unknown = await response.json();

    if (!isUserDto(data)) {
        throw new Error("Invalid user response");
    }

    return data;
}
```

Notice the use of:

```ts
const data: unknown
```

rather than immediately claiming:

```ts
const data: UserDto
```

`unknown` means:

> I have a value, but I don't yet know what its type is.

After:

```ts
if (!isUserDto(data)) {
    throw new Error(...);
}
```

TypeScript knows:

```txt
data
 ↓
UserDto
```

This is a safer boundary between external data and your TypeScript application.

For larger applications, runtime validation libraries are often used instead of manually writing every validator, but the underlying principle is the same.

## 9. Complete Example

Create `type-narrowing.ts`:

```ts
type Role =
    | "ADMIN"
    | "EDITOR"
    | "VIEWER";

interface Admin {
    type: "ADMIN";
    username: string;
    permissions: string[];
}

interface Editor {
    type: "EDITOR";
    username: string;
    sections: string[];
}

interface Viewer {
    type: "VIEWER";
    username: string;
}

type User =
    | Admin
    | Editor
    | Viewer;

function isAdmin(user: User): user is Admin {
    return user.type === "ADMIN";
}

function printUser(user: User): void {

    console.log(`User: ${user.username}`);

    if (isAdmin(user)) {
        console.log("Administrator");
        console.log(
            `Permissions: ${user.permissions.join(", ")}`
        );

        return;
    }

    if (user.type === "EDITOR") {
        console.log("Editor");
        console.log(
            `Sections: ${user.sections.join(", ")}`
        );

        return;
    }

    console.log("Viewer");
}

const admin: Admin = {
    type: "ADMIN",
    username: "john",
    permissions: [
        "READ",
        "WRITE",
        "DELETE"
    ]
};

const editor: Editor = {
    type: "EDITOR",
    username: "mary",
    sections: [
        "products",
        "orders"
    ]
};

const viewer: Viewer = {
    type: "VIEWER",
    username: "david"
};

const users: User[] = [
    admin,
    editor,
    viewer
];

for (const user of users) {
    printUser(user);
    console.log("---");
}
```

Compile and run:

```sh
npx tsc type-narrowing.ts
node type-narrowing.js
```

Output:

```txt
User: john
Administrator
Permissions: READ, WRITE, DELETE
---
User: mary
Editor
Sections: products, orders
---
User: david
Viewer
---
```

The main narrowing mechanisms from this lesson are:

```txt
typeof
   ↓
Primitive types

typeof value === "string"


instanceof
   ↓
Classes / runtime objects

user instanceof Admin


in
   ↓
Property existence

"permissions" in user


Discriminated union
   ↓
Common literal property

user.type === "ADMIN"


Custom type guard
   ↓
Reusable narrowing logic

function isAdmin(user: User): user is Admin
```

For a **Spring Boot + TypeScript** application, you'll probably use discriminated unions and ordinary property checks frequently for application data, while custom type guards become especially useful when you need to validate data crossing a runtime boundary, such as an HTTP API.