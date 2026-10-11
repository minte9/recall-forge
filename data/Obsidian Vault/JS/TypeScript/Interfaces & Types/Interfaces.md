## 1. Declaring Interfaces

An interface defines the shape of an object. It specifies which properties an object should contain and what types those properties must have. If you're coming from Java, it is important not to think of a TypeScript interface only as the equivalent of a Java `interface`. TypeScript interfaces are commonly used simply to describe structured data.

For example, a Spring Boot REST API might return a `UserDto`. On the TypeScript frontend, you can create an interface describing the JSON representation of that DTO. TypeScript then checks your code at compile time, but the interface itself does not exist at runtime in the generated JavaScript.

```ts
interface User {
    id: number;
    username: string;
    email: string;
    active: boolean;
}

const user: User = {
    id: 1,
    username: "john",
    email: "john@example.com",
    active: true
};

console.log(user.username);
```

If the object doesn't match the interface, TypeScript reports an error:

```ts
interface User {
    id: number;
    username: string;
}

const user: User = {
    id: 1
    // Error: property 'username' is missing
};
```

You can also use an interface as the type of a function parameter:

```ts
interface User {
    id: number;
    username: string;
}

function printUser(user: User): void {
    console.log(`${user.id}: ${user.username}`);
}

printUser({
    id: 1,
    username: "john"
});
```

This becomes especially useful when applications contain many objects coming from backend APIs.

## 2. Required

Properties are required by default. The `?` modifier makes a property optional.

```ts
interface User {
    id: number;
    username: string;
    email?: string;
}

const user1: User = {
    id: 1,
    username: "john",
    email: "john@example.com"
};

const user2: User = {
    id: 2,
    username: "mary"
};
```

Both objects are valid because `email` is optional.

Because an optional property might not exist, you should handle that possibility:

```ts
function printEmail(user: User): void {
    if (user.email) {
        console.log(user.email);
    } else {
        console.log("No email available");
    }
}
```

The `readonly` modifier prevents a property from being reassigned after the object has been created:

```ts
interface User {
    readonly id: number;
    username: string;
    email?: string;
}

const user: User = {
    id: 1,
    username: "john"
};

user.username = "john.smith"; // OK

user.id = 10;
// Error:
// Cannot assign to 'id' because it is a read-only property.
```

This is useful for properties such as database IDs that normally shouldn't change on the frontend.

You can combine `readonly` and optional properties:

```ts
interface Product {
    readonly id: number;
    name: string;
    description?: string;
    price: number;
}

const product: Product = {
    id: 101,
    name: "Keyboard",
    price: 79.99
};
```

One important detail is that `readonly` is primarily a TypeScript compile-time restriction. It does not make the JavaScript object inherently immutable at runtime.

## 3. Modeling DTO

Interfaces become particularly useful when your TypeScript application communicates with a Spring Boot backend.

Suppose Spring Boot exposes this DTO:

```java
public record ProductDto(
    Long id,
    String name,
    double price,
    boolean available
) {}
```

and an endpoint:

```java
@GetMapping("/api/products/{id}")
public ProductDto getProduct(@PathVariable Long id) {
    return new ProductDto(
        id,
        "Mechanical Keyboard",
        89.99,
        true
    );
}
```

The JSON response might be:

```json
{
    "id": 1,
    "name": "Mechanical Keyboard",
    "price": 89.99,
    "available": true
}
```

On the TypeScript side, you can model the response:

```ts
interface ProductDto {
    id: number;
    name: string;
    price: number;
    available: boolean;
}
```

Then use it with `fetch`:

```ts
async function getProduct(id: number): Promise<ProductDto> {
    const response = await fetch(
        `http://localhost:8080/api/products/${id}`
    );

    const product: ProductDto = await response.json();

    return product;
}

async function main(): Promise<void> {
    const product = await getProduct(1);

    console.log(product.name);
    console.log(product.price);
}

main();
```

This gives you useful type checking while working with the response:

```ts
const product = await getProduct(1);

console.log(product.name);     // OK
console.log(product.price);    // OK

console.log(product.category);
// Error: Property 'category' does not exist on type 'ProductDto'.
```

A larger application will usually have many such interfaces:

```ts
interface UserDto {
    id: number;
    username: string;
    email: string;
}

interface ProductDto {
    id: number;
    name: string;
    price: number;
}

interface OrderDto {
    id: number;
    user: UserDto;
    products: ProductDto[];
    total: number;
}
```

For example:

```ts
const order: OrderDto = {
    id: 1001,

    user: {
        id: 1,
        username: "john",
        email: "john@example.com"
    },

    products: [
        {
            id: 101,
            name: "Keyboard",
            price: 80
        },
        {
            id: 102,
            name: "Mouse",
            price: 40
        }
    ],

    total: 120
};

console.log(order.user.username);

for (const product of order.products) {
    console.log(`${product.name}: ${product.price}`);
}
```

This maps naturally to the kind of DTO hierarchy you would commonly have in a Spring Boot application.

## 4. Complete example

Create `interfaces.ts`:

```ts
interface UserDto {
    readonly id: number;
    username: string;
    email?: string;
}

interface ProductDto {
    readonly id: number;
    name: string;
    price: number;
}

interface OrderDto {
    readonly id: number;
    user: UserDto;
    products: ProductDto[];
    total: number;
}

function printUser(user: UserDto): void {
    console.log(`User: ${user.username}`);

    if (user.email) {
        console.log(`Email: ${user.email}`);
    }
}

function printOrder(order: OrderDto): void {
    console.log(`Order #${order.id}`);
    console.log(`Customer: ${order.user.username}`);

    console.log("Products:");

    for (const product of order.products) {
        console.log(`- ${product.name}: ${product.price}`);
    }

    console.log(`Total: ${order.total}`);
}

const user: UserDto = {
    id: 1,
    username: "john",
    email: "john@example.com"
};

const order: OrderDto = {
    id: 1001,
    user: user,

    products: [
        {
            id: 101,
            name: "Keyboard",
            price: 80
        },
        {
            id: 102,
            name: "Mouse",
            price: 40
        }
    ],

    total: 120
};

printUser(user);
printOrder(order);
```

Compile and run it:

```sh
npx tsc interfaces.ts
node interfaces.js
```

Output:

```txt
User: john
Email: john@example.com

Order #1001
Customer: john
Products:
- Keyboard: 80
- Mouse: 40
Total: 120
```

The main idea to retain is:

```txt
Spring Boot                    TypeScript
-----------                    ----------

ProductDto record/class   →    interface ProductDto

Long id                   →    id: number
String name               →    name: string
boolean available         →    available: boolean
List<ProductDto>          →    ProductDto[]
nullable/optional field   →    field?: type
```

So for a Spring Boot + TypeScript application, interfaces are one of the main tools for describing the structure of data exchanged between the backend and frontend. One caveat we'll build on later: an interface validates your TypeScript code at compile time; it does not  validate that JSON received from the server actually has that structure at runtime.