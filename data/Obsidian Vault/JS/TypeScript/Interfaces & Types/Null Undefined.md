## 1. null vs undefined

TypeScript inherits both `null` and `undefined` from JavaScript. They are related, but they usually represent slightly different situations.

`undefined` generally means that **a value has not been provided or assigned**:

```ts
let username: string | undefined;

console.log(username); // undefined

username = "john";

console.log(username); // john
```

`null` is normally used when we **explicitly say that there is no value**:

```ts
let selectedUser: string | null = null;

selectedUser = "john";

console.log(selectedUser);
```

A useful conceptual distinction is:

```
undefined
    ↓
No value was provided / assigned.

null
    ↓
We explicitly assigned "no value".
```

For example:

```ts
interface User {
    id: number;
    email?: string;
}
```

If `email` wasn't supplied:

```ts
const user: User = {
    id: 1
};

console.log(user.email); // undefined
```

By contrast, an API could deliberately return:

```json
{
    "id": 1,
    "email": null
}
```

In that case we could model it as:

```ts
interface User {
    id: number;
    email: string | null;
}
```

This distinction is especially relevant when working with REST or GraphQL APIs.

## 2. strictNullChecks

One of the most important TypeScript compiler settings is:

```json
{
    "compilerOptions": {
        "strictNullChecks": true
    }
}
```

In modern TypeScript projects, you'll normally enable the broader:

```json
{
    "compilerOptions": {
        "strict": true
    }
}
```

which includes `strictNullChecks`.

With strict null checking enabled, this is invalid:

```ts
let username: string = "john";

username = null;      // Error
username = undefined; // Error
```

A `string` really means:

```ts
string
```

not:

```ts
string | null | undefined
```

If `null` is a legitimate value, you must say so explicitly:

```ts
let username: string | null = null;

username = "john";
username = null;
```

Similarly:

```ts
let username: string | undefined;

username = "john";
username = undefined;
```

Or all three possibilities:

```ts
let username: string | null | undefined;
```

Why `strictNullChecks` matters?

Consider this function:

```ts
function printUsername(username: string): void {
    console.log(username.toUpperCase());
}
```

Without proper null checking, passing a missing value could eventually result in a runtime error because:

```ts
null.toUpperCase()
```

doesn't make sense.

With strict null checking, TypeScript catches the problem earlier:

```ts
let username: string | null = null;

printUsername(username);
// Error
```

We must first prove that `username` isn't `null`:

```ts
if (username !== null) {
    printUsername(username);
}
```

TypeScript performs type narrowing:

```
Before:

username
   ↓
string | null


username !== null
       ↓
     string
```

Another common pattern is:

```ts
function printUsername(username: string | null): void {
    if (username === null) {
        console.log("No username");
        return;
    }

    console.log(username.toUpperCase());
}
```

After the `if` block, TypeScript knows `username` must be a string.

## 3. Optional Properties

From the interfaces lesson, we've already seen:

```ts
interface User {
    id: number;
    username: string;
    email?: string;
}
```

The `?` means `email` is optional.

Therefore this is valid:

```ts
const user: User = {
    id: 1,
    username: "john"
};
```

When accessing:

```ts
console.log(user.email);
```

the type is effectively:

```ts
string | undefined
```

because the property might not exist.

This becomes important when calling methods:

```ts
console.log(user.email.toUpperCase());
```

TypeScript reports an error because `email` could be `undefined`.

We can check it first:

```ts
if (user.email !== undefined) {
    console.log(user.email.toUpperCase());
}
```

Or more commonly:

```ts
if (user.email) {
    console.log(user.email.toUpperCase());
}
```

There's also an important difference between:

```ts
email?: string;
```

and:

```ts
email: string | null;
```

The first means the property itself may be absent:

```ts
const user = {
    id: 1
};
```

The second means the property is required, but its value can explicitly be `null`:

```ts
const user = {
    id: 1,
    email: null
};
```

Conceptually:

```txt
email?: string

Possible:
{ email: "john@example.com" }
{ }


email: string | null

Possible:
{ email: "john@example.com" }
{ email: null }

Not valid:
{ }
```

This distinction matters when modeling backend DTOs.

---

## 4. Optional Chaining ?.

Another useful feature when dealing with missing values is **optional chaining**.

Consider:

```ts
interface Address {
    city: string;
}

interface User {
    username: string;
    address?: Address;
}
```

This isn't safe:

```ts
console.log(user.address.city);
```

because `address` could be `undefined`.

We could write:

```ts
if (user.address) {
    console.log(user.address.city);
}
```

But TypeScript provides a shorter form:

```ts
console.log(user.address?.city);
```

If `address` exists, TypeScript accesses `city`.

If it doesn't exist, the entire expression evaluates to:

```ts
undefined
```

For example:

```ts
const user: User = {
    username: "john"
};

console.log(user.address?.city);
```

Output:

```ts
undefined
```

Optional chaining can be nested:

```ts
interface Country {
    name: string;
}

interface Address {
    city?: string;
    country?: Country;
}

interface User {
    address?: Address;
}

console.log(user.address?.country?.name);
```

This is extremely common when working with API responses.

## 5. Nullish Coalescing `??`

Suppose we have:

```ts
interface User {
    username: string;
    displayName?: string;
}
```

We want to display `displayName` if available and otherwise use `"Anonymous"`.

We could write:

```ts
let name: string;

if (user.displayName !== undefined) {
    name = user.displayName;
} else {
    name = "Anonymous";
}
```

But TypeScript/JavaScript provides the **nullish coalescing operator**:

```ts
const name = user.displayName ?? "Anonymous";
```

It means:

> Use the value on the left unless it is `null` or `undefined`.

For example:

```ts
const username: string | null = null;

const displayName = username ?? "Anonymous";

console.log(displayName);
```

Output:

```
Anonymous
```

If:

```ts
const username: string | null = "john";
```

then:

```ts
const displayName = username ?? "Anonymous";
```

produces:

```
john
```

---

## 6. ?? vs ||

There is an important difference between:

```ts
value ?? defaultValue
```

and:

```ts
value || defaultValue
```

`??` only uses the fallback for:

```ts
null
undefined
```

`||` uses the fallback for any **falsy** value, including:

```ts
false
0
""
null
undefined
NaN
```

Consider:

```ts
const count = 0;

console.log(count || 10); // 10
console.log(count ?? 10); // 0
```

The second result is often what you actually want.

Another example:

```ts
const name = "";

console.log(name || "Unknown"); // Unknown
console.log(name ?? "Unknown"); // ""
```

Therefore, when your intention is specifically:

> Use this default when the value is missing.

`??` is usually the appropriate operator.


## 7. Combining `?.` and `??`

These operators are frequently used together.

Consider:

```ts
interface Address {
    city?: string;
}

interface User {
    username: string;
    address?: Address;
}
```

We can write:

```ts
const city = user.address?.city ?? "Unknown city";
```

This can be read almost like English:

```txt
user.address?.city
        ↓
Get city if address exists.

        ??

"Unknown city"
        ↓
Otherwise use this value.
```

Example:

```ts
const user: User = {
    username: "john"
};

const city = user.address?.city ?? "Unknown city";

console.log(city);
```

Output:

```
Unknown city
```

This pattern is extremely common in TypeScript frontend code.

## 8. Spring Boot DTO Example

Suppose your Spring Boot backend has:

```java
public record UserDto(
    Long id,
    String username,
    String displayName
) {}
```

and `displayName` may be `null`.

The API could return:

```json
{
    "id": 1,
    "username": "john",
    "displayName": null
}
```

The TypeScript representation should reflect reality:

```ts
interface UserDto {
    id: number;
    username: string;
    displayName: string | null;
}
```

Not:

```ts
interface UserDto {
    id: number;
    username: string;
    displayName: string;
}
```

because that incorrectly tells TypeScript that `displayName` can never be `null`.

We can safely display it:

```ts
function printUser(user: UserDto): void {
    const name = user.displayName ?? user.username;

    console.log(name);
}
```

If:

```ts
const user: UserDto = {
    id: 1,
    username: "john",
    displayName: null
};
```

the output is:

```
john
```

## 9. Complete Example

Create `null-undefined.ts`:

```ts
interface Address {
    city: string;
    country?: string;
}

interface User {
    readonly id: number;

    username: string;

    // Required property that may explicitly be null
    displayName: string | null;

    // Optional property
    email?: string;

    // Optional nested object
    address?: Address;
}

function printUser(user: User): void {

    console.log(`ID: ${user.id}`);
    console.log(`Username: ${user.username}`);

    const name =
        user.displayName ?? user.username;

    console.log(`Display name: ${name}`);

    const email =
        user.email ?? "No email";

    console.log(`Email: ${email}`);

    const city =
        user.address?.city ?? "Unknown city";

    console.log(`City: ${city}`);

    const country =
        user.address?.country ?? "Unknown country";

    console.log(`Country: ${country}`);
}

const user1: User = {
    id: 1,
    username: "john",
    displayName: "John Smith",
    email: "john@example.com",
    address: {
        city: "Bucharest",
        country: "Romania"
    }
};

const user2: User = {
    id: 2,
    username: "mary",
    displayName: null
};

printUser(user1);

console.log("---");

printUser(user2);
```

Compile and run:

```sh
npx tsc null-undefined.ts
node null-undefined.js
```

Output:

```txt
ID: 1
Username: john
Display name: John Smith
Email: john@example.com
City: Bucharest
Country: Romania
---
ID: 2
Username: mary
Display name: mary
Email: No email
City: Unknown city
Country: Unknown country
```

The key concepts to retain are:

```
null
  ↓
Explicitly no value

undefined
  ↓
Value/property wasn't provided


name: string
  ↓
must be a string

name: string | null
  ↓
required property, but value may be null

name?: string
  ↓
property may be absent
  ↓
reading it gives string | undefined
```

And three operators you'll see constantly in TypeScript:

```ts
// Optional property
email?: string;

// Optional chaining
user.address?.city;

// Nullish coalescing
user.displayName ?? "Anonymous";
```

For Spring Boot + TypeScript applications, a particularly important rule is: **model the API's actual nullability**. If the backend can return `null`, represent that explicitly as `string | null`, `ProductDto | null`, etc. This lets TypeScript force you to handle the missing value rather than discovering it through a runtime error.