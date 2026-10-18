
## 1. Public

Access modifiers control which parts of a class can be accessed from outside the class or from subclasses. TypeScript supports `public`, `private`, and `protected`, similar to Java. It also provides `readonly`, which prevents reassignment of a property after initialization.

An important difference is that TypeScript access modifiers are primarily enforced at compile time, whereas Java enforces access restrictions at runtime as well.

The `public` modifier allows a property or method to be accessed from anywhere. In TypeScript, class members are public by default, so explicitly writing `public` is optional.

```ts
class User {
    public name: string;

    constructor(name: string) {
        this.name = name;
    }

    public greet(): string {
        return `Hello, ${this.name}`;
    }
}

const user = new User("Alice");

console.log(user.name);    // Alice
console.log(user.greet()); // Hello, Alice

user.name = "Bob";         // Allowed
```

The following is equivalent:

```ts
class User {
    name: string;

    constructor(name: string) {
        this.name = name;
    }

    greet(): string {
        return `Hello, ${this.name}`;
    }
}
```

In Java, fields and methods without an access modifier have package-private visibility. In TypeScript, they are public.

## 2. Private

The `private` modifier restricts access to members within the class itself. This is useful for encapsulation, preventing external code from directly modifying internal state.

For example, a bank account should not allow arbitrary changes to its balance. Instead, modifications should happen through controlled methods.

```ts
class BankAccount {
    private balance: number;

    constructor(initialBalance: number) {
        this.balance = initialBalance;
    }

    deposit(amount: number): void {
        if (amount <= 0) {
            throw new Error("Invalid amount");
        }

        this.balance += amount;
    }

    getBalance(): number {
        return this.balance;
    }
}

const account = new BankAccount(1000);

account.deposit(500);

console.log(account.getBalance()); // 1500

// Compilation error:
// console.log(account.balance);

// Compilation error:
// account.balance = 999999;
```

Here, `balance` can only be modified through the class's methods.

However, TypeScript's `private` is different from Java's `private`. TypeScript performs compile-time checking, but ordinary `private` properties still exist in the resulting JavaScript.

JavaScript also supports truly private fields using `#`:

```ts
class BankAccount {
    #balance: number;

    constructor(balance: number) {
        this.#balance = balance;
    }

    getBalance(): number {
        return this.#balance;
    }
}

const account = new BankAccount(1000);

console.log(account.getBalance()); // 1000

// Error:
// console.log(account.#balance);
```

Unlike TypeScript's `private`, JavaScript's `#balance` is enforced at runtime. It is generally preferable when runtime privacy is important.

## 3. Protected

The `protected` modifier allows access within the class and its subclasses, but prevents direct access from outside.

This becomes useful when implementing inheritance.

```ts
class Employee {
    protected salary: number;

    constructor(salary: number) {
        this.salary = salary;
    }

    protected calculateBonus(): number {
        return this.salary * 0.10;
    }
}

class Manager extends Employee {

    getTotalCompensation(): number {
        return this.salary + this.calculateBonus();
    }
}

const manager = new Manager(5000);

console.log(manager.getTotalCompensation());
// 5500

// Compilation error:
// console.log(manager.salary);

// Compilation error:
// manager.calculateBonus();
```

Notice that `Manager` can access `salary` and `calculateBonus()` because it extends `Employee`.

The comparison with Java is important: Java's `protected` also permits access from other classes in the same package. TypeScript does not have this package-level access rule.

## 4. Readonly

The `readonly` modifier prevents a property from being reassigned after initialization. It is useful for values that should remain constant during an object's lifetime, such as identifiers or creation timestamps.

A `readonly` property can be initialized when declared or inside the constructor.

```ts
class Product {
    readonly id: number;
    name: string;
    price: number;

    constructor(
        id: number,
        name: string,
        price: number
    ) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    updatePrice(price: number): void {
        this.price = price;
    }
}

const product = new Product(1, "Laptop", 1200);

product.updatePrice(1100);

console.log(product.price); // 1100
console.log(product.id);    // 1

// Compilation error:
// product.id = 2;
```

The `readonly` modifier is similar to Java's `final` for fields, although Java enforces final-field assignment restrictions at runtime through its execution model, while TypeScript's `readonly` is compile-time only.

Also, `readonly` does not make an entire object immutable:

```ts
class User {
    readonly settings = {
        theme: "dark"
    };
}

const user = new User();

// Allowed: modifying the nested object
user.settings.theme = "light";

// Compilation error: reassigning the property
// user.settings = { theme: "light" };
```

## 5. Combining access modifiers

TypeScript allows access modifiers to be combined with `readonly`. A common pattern is using `private readonly` for dependencies that should not change after construction.

```ts
class ProductService {
    constructor(
        private readonly apiUrl: string
    ) {}

    getEndpoint(): string {
        return `${this.apiUrl}/products`;
    }
    
    setEndpoint(): void {
        // Compilation error:
        // this.apiUrl = null;
    }
}

const service = new ProductService(
    "http://localhost:8080/api"
);

console.log(service.getEndpoint());

// Compilation error:
// console.log(service.apiUrl);
```

This is also an example of a parameter property: TypeScript automatically creates and initializes the `apiUrl` field from the constructor parameter.

In Java, the equivalent would be:

```java
public class ProductService {

    private final String apiUrl;

    public ProductService(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getEndpoint() {
        return apiUrl + "/products";
    }
}
```

This pattern is particularly useful when building Angular services that communicate with a Spring Boot backend.

## 6. Complete example: Product management

Create `src/access-modifiers.ts`:

```ts
class Product {
    constructor(
        public readonly id: number,
        public name: string,
        private price: number,
        protected category: string
    ) {}

    public getPrice(): number {
        return this.price;
    }

    public updatePrice(newPrice: number): void {
        if (newPrice <= 0) {
            throw new Error("Price must be positive");
        }

        this.price = newPrice;
    }

    protected getCategory(): string {
        return this.category;
    }

    public getDescription(): string {
        return `${this.name}: €${this.price}`;
    }
}

class DigitalProduct extends Product {
    constructor(
        id: number,
        name: string,
        price: number,
        public readonly downloadUrl: string
    ) {
        super(id, name, price, "Digital");
    }

    public getDetails(): string {
        return `${this.getDescription()} (${this.getCategory()})`;
    }
}

const product = new Product(
    1,
    "Laptop",
    1200,
    "Electronics"
);

product.updatePrice(1100);

console.log(product.id);
console.log(product.name);
console.log(product.getPrice());
console.log(product.getDescription());

const ebook = new DigitalProduct(
    2,
    "TypeScript Guide",
    30,
    "/downloads/typescript.pdf"
);

console.log(ebook.getDetails());
console.log(ebook.downloadUrl);
```

Compile and run on Ubuntu:

```sh
npx tsc src/access-modifiers.ts --target ES2022
node src/access-modifiers.js
```

Output:

```TXT
1
Laptop
1100
Laptop: €1100
TypeScript Guide: €30 (Digital)
/downloads/typescript.pdf
```

## TypeScript vs Java comparison

|Modifier|TypeScript|Java|
|---|---|---|
|`public`|Accessible everywhere; default|Accessible everywhere|
|`private`|Accessible only within the class; compile-time|Accessible only within the class|
|`protected`|Class and subclasses|Class, subclasses, and same package|
|`readonly`|Prevents reassignment at compile time|Similar to `final` fields|
|`private readonly`|Private, non-reassignable field|`private final`|
|`#field`|Runtime-private JavaScript field|Similar in intent to `private`|
