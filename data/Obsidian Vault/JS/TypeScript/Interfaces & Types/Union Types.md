## 1. Union Types with `|`

A **union type** means that a value can have **one of several specified types**. TypeScript uses the `|` operator to define a union.

For example:

```ts
let id: string | number;

id = 10;        // OK
id = "USR-10";  // OK

id = true;      // Error
```

Without a union, you would have to choose only one type:

```ts
let id: number;

id = 10;       // OK
id = "USR-10"; // Error
```

Union types are especially useful when data can legitimately have several representations. For example, an API identifier might sometimes be numeric and sometimes a string:

```ts
type Id = number | string;

function printId(id: Id): void {
    console.log(`ID: ${id}`);
}

printId(100);
printId("USR-100");
```

You can combine more than two types:

```ts
type Value = string | number | boolean;

let value: Value;

value = "hello";
value = 100;
value = true;
```

## 2. Literal Types

A particularly powerful use of unions is combining them with **literal types**.

Normally:

```ts
let status: string;
```

allows any string:

```ts
status = "ACTIVE";
status = "INACTIVE";
status = "banana";   // also valid
status = "whatever"; // also valid
```

But perhaps your application only accepts two values.

You can define:

```ts
type UserStatus = "ACTIVE" | "INACTIVE";

let status: UserStatus;

status = "ACTIVE";   // OK
status = "INACTIVE"; // OK

status = "DELETED";  // Error
```

This is very useful for values that would often be represented by enums in Java.

For example, your Spring Boot application might have:

```java
public enum OrderStatus {
    CREATED,
    PROCESSING,
    SHIPPED,
    DELIVERED
}
```

The TypeScript equivalent can be modeled as:

```ts
type OrderStatus =
    | "CREATED"
    | "PROCESSING"
    | "SHIPPED"
    | "DELIVERED";
```

Then:

```ts
type Order = {
    id: number;
    status: OrderStatus;
};

const order: Order = {
    id: 1001,
    status: "PROCESSING"
};
```

TypeScript prevents invalid values:

```ts
const order: Order = {
    id: 1001,
    status: "UNKNOWN"
    // Error
};
```

## 3. Narrowing with typeof

Union types introduce an important problem.

Consider:

```ts
type Id = string | number;

function printId(id: Id): void {
    console.log(id.toUpperCase());
}
```

TypeScript reports an error. Why?

Because `toUpperCase()` exists on `string`, but not on `number`.

We therefore need **type narrowing**.

```ts
function printId(id: string | number): void {

    if (typeof id === "string") {
        console.log(id.toUpperCase());
    } else {
        console.log(id);
    }
}
```

## 4. Union Types with Objects

Union types can also combine entire object structures.

Consider two possible API results:

```ts
type SuccessResponse = {
    success: true;
    data: string;
};

type ErrorResponse = {
    success: false;
    error: string;
};
```

We can create:

```ts
type ApiResponse =
    | SuccessResponse
    | ErrorResponse;
```

A function can now return either:

```ts
function getResult(success: boolean): ApiResponse {

    if (success) {
        return {
            success: true,
            data: "Product loaded"
        };
    }

    return {
        success: false,
        error: "Product not found"
    };
}
```

Then:

```ts
const response = getResult(true);

if (response.success) {
    console.log(response.data);
} else {
    console.log(response.error);
}
```

This is a very useful TypeScript pattern called a **discriminated union**.

The `success` property tells TypeScript which object type it's dealing with:

```
ApiResponse
     │
     ├── success: true
     │       ↓
     │   SuccessResponse
     │       ↓
     │   data available
     │
     └── success: false
             ↓
         ErrorResponse
             ↓
         error available
```

You'll encounter this pattern frequently in frontend applications when modeling API responses and application state.

## 5. Practical Spring Boot Example

Suppose your Spring Boot backend returns products:

```java
public enum ProductStatus {
    AVAILABLE,
    OUT_OF_STOCK,
    DISCONTINUED
}
```

with:

```java
public record ProductDto(
    Long id,
    String name,
    double price,
    ProductStatus status
) {}
```

On the TypeScript side:

```ts
type ProductStatus =
    | "AVAILABLE"
    | "OUT_OF_STOCK"
    | "DISCONTINUED";

type ProductDto = {
    id: number;
    name: string;
    price: number;
    status: ProductStatus;
};
```

We can now write:

```ts
function printProduct(product: ProductDto): void {
    console.log(`${product.name}: ${product.price}`);

    if (product.status === "AVAILABLE") {
        console.log("Product can be ordered.");
    } else if (product.status === "OUT_OF_STOCK") {
        console.log("Product is temporarily unavailable.");
    } else {
        console.log("Product has been discontinued.");
    }
}
```

Usage:

```ts
const keyboard: ProductDto = {
    id: 101,
    name: "Mechanical Keyboard",
    price: 89.99,
    status: "AVAILABLE"
};

printProduct(keyboard);
```

## 6.  Complete Example

Create `union-types.ts`:

```ts
type UserId = number | string;

type Role =
    | "ADMIN"
    | "EDITOR"
    | "VIEWER";

type UserStatus =
    | "ACTIVE"
    | "INACTIVE"
    | "SUSPENDED";

type User = {
    id: UserId;
    username: string;
    role: Role;
    status: UserStatus;
};

function printId(id: UserId): void {

    if (typeof id === "string") {
        console.log(`String ID: ${id.toUpperCase()}`);
    } else {
        console.log(`Numeric ID: ${id}`);
    }
}

function printUser(user: User): void {
    console.log(`User: ${user.username}`);

    printId(user.id);

    console.log(`Role: ${user.role}`);
    console.log(`Status: ${user.status}`);

    if (user.role === "ADMIN") {
        console.log("Administrator access");
    } else if (user.role === "EDITOR") {
        console.log("Editor access");
    } else {
        console.log("Read-only access");
    }
}

const admin: User = {
    id: 1,
    username: "john",
    role: "ADMIN",
    status: "ACTIVE"
};

const viewer: User = {
    id: "USR-200",
    username: "mary",
    role: "VIEWER",
    status: "ACTIVE"
};

printUser(admin);

console.log("---");

printUser(viewer);
```

Compile and run:

```sh
npx tsc union-types.ts
node union-types.js
```

Output:

```txt
User: john
Numeric ID: 1
Role: ADMIN
Status: ACTIVE
Administrator access
---
User: mary
String ID: USR-200
Role: VIEWER
Status: ACTIVE
Read-only access
```

For a **Spring Boot + TypeScript** application, literal unions are particularly useful for mirroring Java enums and other restricted API values, while general unions are useful whenever a value or response can legitimately take multiple forms.