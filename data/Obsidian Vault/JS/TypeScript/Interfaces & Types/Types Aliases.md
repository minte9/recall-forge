
## 1. Tyep Declarations

A type alias lets you give a name to a TypeScript type. The basic syntax uses the `type` keyword followed by the name and the type definition.

For simple values:

```ts
type UserId = number;
type Username = string;

const id: UserId = 10;
const username: Username = "john";
```

Here, `UserId` is still a `number` and `Username` is still a `string`. The alias doesn't create a new runtime type; it simply gives an existing type a meaningful name.

Type aliases can also describe objects:

```ts
type User = {
    id: number;
    username: string;
    email: string;
};

const user: User = {
    id: 1,
    username: "john",
    email: "john@example.com"
};

console.log(user.username);
```

This looks very similar to the interface from the previous lesson. For simple object structures, the two are often interchangeable.

```ts
interface User {
    id: number;
    username: string;
    email: string;
}
```

## 2. Interface vs Type Alias

Both `interface` and `type` can describe objects:

```ts
interface ProductInterface {
    id: number;
    name: string;
    price: number;
}

type ProductType = {
    id: number;
    name: string;
    price: number;
};
```

Both can then be used in exactly the same way:

```ts
const product1: ProductInterface = {
    id: 1,
    name: "Keyboard",
    price: 80
};

const product2: ProductType = {
    id: 2,
    name: "Mouse",
    price: 40
};
```

The main difference is that a `type` alias can represent **more than just object structures**.

For example:

```ts
type UserId = number;

type Status = "ACTIVE" | "INACTIVE";

type StringOrNumber = string | number;

type Coordinates = [number, number];
```

An interface cannot directly represent these:

```ts
// Not possible:

interface Status = "ACTIVE" | "INACTIVE";
```

Interfaces are particularly natural when describing object contracts:

```ts
interface UserDto {
    id: number;
    username: string;
    email: string;
}
```

while `type` becomes especially useful when composing or combining types:

```ts
type Status = "ACTIVE" | "INACTIVE";

type UserId = number;

type UserDto = {
    id: UserId;
    username: string;
    status: Status;
};
```

There is another important difference: interfaces support **declaration merging**.

```ts
interface User {
    id: number;
}

interface User {
    username: string;
}
```

TypeScript combines them, so `User` effectively becomes:

```ts
interface User {
    id: number;
    username: string;
}
```

Therefore this is valid:

```ts
const user: User = {
    id: 1,
    username: "john"
};
```

You cannot do the same with `type`:

```ts
type User = {
    id: number;
};

type User = {
    username: string;
};

// Error: Duplicate identifier 'User'
```

For normal application development, a useful convention is:

```txt
interface → primarily object contracts
type      → aliases, unions, tuples and composed types
```

This isn't a strict rule. Many TypeScript projects use `type` extensively even for objects.

## 3. Object Types

Type aliases are very useful for defining reusable object structures.

Consider a product:

```ts
type Product = {
    readonly id: number;
    name: string;
    description?: string;
    price: number;
};

const product: Product = {
    id: 101,
    name: "Mechanical Keyboard",
    price: 89.99
};
```

Notice that object types support the same property modifiers we saw with interfaces:

```ts
readonly id: number;
description?: string;
```

You can also nest object types:

```ts
type Address = {
    city: string;
    country: string;
};

type User = {
    id: number;
    username: string;
    address: Address;
};

const user: User = {
    id: 1,
    username: "john",

    address: {
        city: "Bucharest",
        country: "Romania"
    }
};
```

And arrays work naturally:

```ts
type Product = {
    id: number;
    name: string;
    price: number;
};

type Order = {
    id: number;
    products: Product[];
    total: number;
};
```

For example:

```ts
const order: Order = {
    id: 1001,

    products: [
        {
            id: 1,
            name: "Keyboard",
            price: 80
        },
        {
            id: 2,
            name: "Mouse",
            price: 40
        }
    ],

    total: 120
};
```

## 4. Reusable Domain Types

One major advantage of type aliases is that they let you give **domain meaning** to otherwise generic primitive types.

Consider this:

```ts
function getUser(id: number): void {
    console.log(`Loading user ${id}`);
}
```

The `number` tells us the technical type, but not what the value represents.

We can make the intention clearer:

```ts
type UserId = number;

function getUser(id: UserId): void {
    console.log(`Loading user ${id}`);
}
```

The same idea can be used throughout an application:

```ts
type UserId = number;
type ProductId = number;
type OrderId = number;

type Email = string;
type Price = number;
```

Then our domain objects become more descriptive:

```ts
type User = {
    id: UserId;
    email: Email;
};

type Product = {
    id: ProductId;
    name: string;
    price: Price;
};

type Order = {
    id: OrderId;
    user: User;
    products: Product[];
};
```

There is an important limitation here:

```ts
type UserId = number;
type ProductId = number;
```

TypeScript still considers both to be `number`. Therefore:

```ts
const userId: UserId = 10;
const productId: ProductId = 20;

const anotherUserId: UserId = productId; // allowed
```

The aliases improve readability, but don't automatically create strongly distinct types like separate Java classes might.

## 5. Combining Types

Types can also be combined using an **intersection type**, written with `&`.

Suppose we have:

```ts
type User = {
    id: number;
    username: string;
};

type Timestamped = {
    createdAt: string;
    updatedAt: string;
};
```

We can combine them:

```ts
type StoredUser = User & Timestamped;
```

A `StoredUser` must now contain properties from **both** types:

```ts
const user: StoredUser = {
    id: 1,
    username: "john",
    createdAt: "2026-10-04",
    updatedAt: "2026-10-04"
};
```

Conceptually:

```txt
User
 ├── id
 └── username

Timestamped
 ├── createdAt
 └── updatedAt

          ↓ &

StoredUser
 ├── id
 ├── username
 ├── createdAt
 └── updatedAt
```

Interfaces have a similar mechanism using `extends`:

```ts
interface User {
    id: number;
    username: string;
}

interface StoredUser extends User {
    createdAt: string;
    updatedAt: string;
}
```

We'll encounter type composition frequently as the TypeScript examples become larger.

## 6. Spring Boot DTO

Suppose your Spring Boot backend has:

```java
public record ProductDto(
    Long id,
    String name,
    double price,
    String status
) {}
```

The corresponding TypeScript object could be:

```ts
type ProductId = number;

type ProductStatus =
    | "AVAILABLE"
    | "OUT_OF_STOCK"
    | "DISCONTINUED";

type ProductDto = {
    id: ProductId;
    name: string;
    price: number;
    status: ProductStatus;
};
```

Now TypeScript can catch invalid status values:

```ts
const product: ProductDto = {
    id: 1,
    name: "Keyboard",
    price: 89.99,
    status: "AVAILABLE"
};
```

But:

```ts
const product: ProductDto = {
    id: 1,
    name: "Keyboard",
    price: 89.99,
    status: "SOMETHING_ELSE"
    // Error
};
```

The `ProductStatus` definition:

```ts
type ProductStatus =
    | "AVAILABLE"
    | "OUT_OF_STOCK"
    | "DISCONTINUED";
```

is a **union of literal types**, which we'll study properly in `03-union-types.md`.

## 7. Complete Example

Create `type-aliases.ts`:

```ts
type UserId = number;
type ProductId = number;
type OrderId = number;

type ProductStatus =
    | "AVAILABLE"
    | "OUT_OF_STOCK"
    | "DISCONTINUED";

type User = {
    readonly id: UserId;
    username: string;
    email?: string;
};

type Product = {
    readonly id: ProductId;
    name: string;
    price: number;
    status: ProductStatus;
};

type Order = {
    readonly id: OrderId;
    user: User;
    products: Product[];
    total: number;
};

const user: User = {
    id: 1,
    username: "john",
    email: "john@example.com"
};

const keyboard: Product = {
    id: 101,
    name: "Mechanical Keyboard",
    price: 80,
    status: "AVAILABLE"
};

const mouse: Product = {
    id: 102,
    name: "Mouse",
    price: 40,
    status: "OUT_OF_STOCK"
};

const order: Order = {
    id: 1001,
    user,
    products: [keyboard, mouse],
    total: 120
};

function printOrder(order: Order): void {
    console.log(`Order #${order.id}`);
    console.log(`Customer: ${order.user.username}`);

    for (const product of order.products) {
        console.log(
            `${product.name}: ${product.price} (${product.status})`
        );
    }

    console.log(`Total: ${order.total}`);
}

printOrder(order);
```

Compile and run:

```sh
npx tsc type-aliases.ts
node type-aliases.js
```

Output:

```txt
Order #1001
Customer: john
Mechanical Keyboard: 80 (AVAILABLE)
Mouse: 40 (OUT_OF_STOCK)
Total: 120
```

The key distinction to remember at this stage is:

```txt
interface
    ↓
Best thought of as describing an object's shape/contract

interface User {
    id: number;
    name: string;
}


type
    ↓
Can give a name to almost any TypeScript type

type UserId = number;

type User = {
    id: number;
    name: string;
};

type Status = "ACTIVE" | "INACTIVE";

type Value = string | number;
```
