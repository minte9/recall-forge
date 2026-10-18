## 1. Implementing with implements

An interface defines a contract that a class must satisfy. It specifies the properties and methods an object must provide, without necessarily defining how those methods are implemented. In TypeScript, classes use the `implements` keyword to declare that they satisfy an interface.

This concept is very similar to Java interfaces. For example, different payment providers can implement the same `PaymentProcessor` interface while providing different payment processing logic. The application can then work with the interface rather than depending on a specific implementation.

```ts
interface PaymentProcessor {
    processPayment(amount: number): boolean;
}

class CreditCardPayment implements PaymentProcessor {
    processPayment(amount: number): boolean {
        console.log(`Credit card payment: €${amount}`);
        return true;
    }
}

class PayPalPayment implements PaymentProcessor {
    processPayment(amount: number): boolean {
        console.log(`PayPal payment: €${amount}`);
        return true;
    }
}

const creditCard = new CreditCardPayment();
const paypal = new PayPalPayment();

creditCard.processPayment(100);
paypal.processPayment(200);
```

Output:

```txt
Credit card payment: €100
PayPal payment: €200
```

If a class declares `implements PaymentProcessor` but does not implement the required method, TypeScript produces a compilation error.

```ts
interface PaymentProcessor {
    processPayment(amount: number): boolean;
}

// Compilation error
class BankTransfer implements PaymentProcessor {
    // Missing processPayment()
}
```

Unlike Java, TypeScript interfaces disappear during compilation. They exist only for static type checking and do not become JavaScript objects.

## 2. Multiple interfaces

A TypeScript class can implement multiple interfaces, just like a Java class. This allows a class to satisfy several independent contracts without requiring multiple inheritance.

For example, a product can be both printable and exportable. Each interface describes a separate capability, and the class provides the corresponding implementations.

```ts
interface Printable {
    print(): void;
}

interface Exportable {
    export(format: string): string;
}

class Report implements Printable, Exportable {
    constructor(
        public title: string,
        private content: string
    ) {}

    print(): void {
        console.log(`Printing: ${this.title}`);
    }

    export(format: string): string {
        return `Exporting ${this.title} as ${format}`;
    }
}

const report = new Report(
    "Sales Report",
    "Total sales: €5000"
);

report.print();

console.log(report.export("PDF"));
```

Output:

```txt
Printing: Sales Report
Exporting Sales Report as PDF
```

You can also use the interfaces independently:

```ts
const printable: Printable = report;
const exportable: Exportable = report;

printable.print();

console.log(exportable.export("CSV"));
```

This is useful when different services only need a subset of an object's capabilities.

For example, a printing service does not need to know whether an object can also be exported.

## 3. Interface-based contracts

One of the most important OOP principles is programming to an interface rather than an implementation. Instead of depending directly on a concrete class, other components depend on a contract.

This is particularly useful in Spring Boot applications, where services often depend on interfaces and concrete implementations are injected using dependency injection. TypeScript can follow the same design principle, although its interfaces cannot serve as runtime dependency-injection tokens because they are erased during compilation.

Consider a notification system:

```ts
interface NotificationService {
    send(message: string): void;
}

class EmailNotification implements NotificationService {
    send(message: string): void {
        console.log(`Email: ${message}`);
    }
}

class SmsNotification implements NotificationService {
    send(message: string): void {
        console.log(`SMS: ${message}`);
    }
}

class OrderService {
    constructor(
        private notificationService: NotificationService
    ) {}

    placeOrder(): void {
        console.log("Order created");

        this.notificationService.send(
            "Your order has been placed"
        );
    }
}

const emailService = new EmailNotification();
const smsService = new SmsNotification();

const order1 = new OrderService(emailService);
const order2 = new OrderService(smsService);

order1.placeOrder();
order2.placeOrder();
```

Output:

```txt
Order created
Email: Your order has been placed
Order created
SMS: Your order has been placed
```

The `OrderService` class does not know whether the notification is sent by email or SMS. It only knows that the injected object provides a `send()` method.

This makes the code easier to extend and test. For example, a mock notification service can be injected during testing without modifying `OrderService`.

#### Structural typing

Unlike Java, TypeScript does not always require an explicit `implements` declaration. If a class has the required properties and methods, it can satisfy an interface automatically.

```ts
interface Logger {
    log(message: string): void;
}

class ConsoleLogger {
    log(message: string): void {
        console.log(message);
    }
}

const logger: Logger = new ConsoleLogger();

logger.log("Application started");
```

Even though `ConsoleLogger` does not explicitly implement `Logger`, TypeScript accepts it because the required structure matches.

This is called structural typing.

## 4. Comparison with Java

TypeScript and Java interfaces share many concepts, including contracts, multiple interface implementation, and polymorphism. However, they differ in their runtime behavior and type compatibility.

TypeScript:

```ts
interface ProductRepository {
    findById(id: number): string | null;
}

class DatabaseProductRepository implements ProductRepository {
    findById(id: number): string | null {
        return id === 1 ? "Laptop" : null;
    }
}
```

Equivalent Java:

```java
public interface ProductRepository {
    String findById(int id);
}

public class DatabaseProductRepository
        implements ProductRepository {

    @Override
    public String findById(int id) {
        return id == 1 ? "Laptop" : null;
    }
}
```

In both languages, the interface describes the expected behavior, while the class provides the implementation.

The main differences are:

|Feature|TypeScript|Java|
|---|---|---|
|Interface declaration|`interface`|`interface`|
|Implementation|`implements`|`implements`|
|Multiple interfaces|Supported|Supported|
|Explicit `implements` required|No, structural typing|Yes|
|Interface exists at runtime|No|Yes, as runtime type metadata|
|`instanceof` interface|Not supported|Supported|
|Interface default methods|Not supported|Supported|
|Interface properties|Supported|Fields are constants|

For example, this is invalid TypeScript:

```ts
interface Product {
    id: number;
}

const product: Product = { id: 1 };

// Compilation error:
// product instanceof Product
```

An interface cannot be used with `instanceof` because it does not exist at runtime.

For runtime validation, you can use a custom type guard:

```ts
interface Product {
    id: number;
}

function isProduct(value: unknown): value is Product {
    return (
        typeof value === "object" &&
        value !== null &&
        "id" in value &&
        typeof value.id === "number"
    );
}

const data: unknown = { id: 1 };

if (isProduct(data)) {
    console.log(data.id);
}
```

This distinction is especially important when consuming JSON responses from a Spring Boot API: TypeScript interfaces describe expected response shapes, but they do not validate incoming JSON automatically.

## 5. Complete example: Product repository

This example demonstrates interface-based design similar to a Spring Boot application using a repository and service layer.

The repository is defined through an interface, while the concrete implementation stores products in memory. The service depends only on the interface, allowing the repository implementation to be replaced later with an HTTP-based implementation.

Create `src/interfaces-classes.ts`:

```ts
interface Product {
    id: number;
    name: string;
    price: number;
}

interface ProductRepository {
    findAll(): Product[];
    findById(id: number): Product | undefined;
    save(product: Product): void;
}

class InMemoryProductRepository implements ProductRepository {
    private products: Product[] = [];

    findAll(): Product[] {
        return [...this.products];
    }

    findById(id: number): Product | undefined {
        return this.products.find(
            product => product.id === id
        );
    }

    save(product: Product): void {
        const index = this.products.findIndex(
            p => p.id === product.id
        );

        if (index >= 0) {
            this.products[index] = product;
        } else {
            this.products.push(product);
        }
    }
}

class ProductService {
    constructor(
        private repository: ProductRepository
    ) {}

    createProduct(
        id: number,
        name: string,
        price: number
    ): void {
        if (price <= 0) {
            throw new Error("Price must be positive");
        }

        this.repository.save({
            id,
            name,
            price
        });
    }

    getProducts(): Product[] {
        return this.repository.findAll();
    }

    getProduct(id: number): Product | undefined {
        return this.repository.findById(id);
    }
}

const repository = new InMemoryProductRepository();
const service = new ProductService(repository);

service.createProduct(1, "Laptop", 1200);
service.createProduct(2, "Keyboard", 100);
service.createProduct(3, "Mouse", 50);

console.log("All products:");

for (const product of service.getProducts()) {
    console.log(`${product.name}: €${product.price}`);
}

console.log("Product ID 2:");

console.log(service.getProduct(2));
```

Compile on Ubuntu:

```sh
npx tsc src/interfaces-classes.ts --target ES2022 --strict
```

Run:

```sh
node src/interfaces-classes.js
```

Output:

```txt
All products:
Laptop: €1200
Keyboard: €100
Mouse: €50
Product ID 2:
{ id: 2, name: 'Keyboard', price: 100 }
```

Later, you could replace `InMemoryProductRepository` with an `HttpProductRepository` that calls a Spring Boot REST API, without changing the business logic in `ProductService`, provided the new implementation satisfies the same contract.