## 1. Abstract classes

An abstract class is a class that cannot be instantiated directly. It serves as a base class for other classes, providing shared properties and methods while allowing subclasses to implement specialized behavior. TypeScript uses the `abstract` keyword, just like Java.

Abstract classes are useful when several related classes share common state and behavior but should not be created independently. For example, a generic `Payment` class might define common payment properties, while concrete classes such as `CreditCardPayment` and `BankTransferPayment` implement the actual payment processing.

```ts
abstract class Payment {
    constructor(
        public amount: number,
        public currency: string
    ) {}

    getDescription(): string {
        return `Payment of ${this.amount} ${this.currency}`;
    }
}

class CreditCardPayment extends Payment {
    process(): void {
        console.log("Processing credit card payment");
    }
}

const payment = new CreditCardPayment(100, "EUR");

console.log(payment.getDescription());
payment.process();

// Compilation error:
// const genericPayment = new Payment(100, "EUR");
```

Output:

```
Payment of 100 EUR
Processing credit card payment
```

The `Payment` class provides reusable functionality, but TypeScript prevents it from being instantiated directly.

Unlike interfaces, abstract classes exist in the generated JavaScript as actual classes. However, the `abstract` restriction itself is enforced only at compile time.

## 2. Abstract methods

An abstract method declares a method that subclasses must implement, without providing an implementation in the base class. Abstract methods can only exist inside abstract classes.

This allows a base class to define a common workflow while delegating specific operations to derived classes. A concrete subclass must implement every inherited abstract method, or it must also be declared abstract.

```ts
abstract class Employee {
    constructor(
        public name: string,
        protected salary: number
    ) {}

    abstract calculateBonus(): number;

    getTotalCompensation(): number {
        return this.salary + this.calculateBonus();
    }
}

class Developer extends Employee {
    override calculateBonus(): number {
        return this.salary * 0.15;
    }
}

class Manager extends Employee {
    override calculateBonus(): number {
        return this.salary * 0.25;
    }
}

const employees: Employee[] = [
    new Developer("Alice", 5000),
    new Manager("Bob", 6000)
];

for (const employee of employees) {
    console.log(
        `${employee.name}: €${employee.getTotalCompensation()}`
    );
}
```

Output:

```
Alice: €5750
Bob: €7500
```

Notice that `getTotalCompensation()` is implemented only once in the abstract base class. It calls `calculateBonus()`, whose implementation depends on the actual subclass.

This is an example of the Template Method pattern, where a base class defines part of an algorithm and subclasses provide the variable steps.

## 3. Abstract vs Interface

Both abstract classes and interfaces define contracts, but they serve different purposes. An interface describes what an object must provide, while an abstract class can also provide shared implementation and maintain state.

In TypeScript, an interface cannot contain implemented methods or constructors. An abstract class, however, can define constructors, fields, concrete methods, and abstract methods.

Consider the following interface:

```ts
interface NotificationService {
    send(message: string): void;
}
```

Any class implementing this interface must provide the `send()` method:

```ts
class EmailNotification implements NotificationService {
    send(message: string): void {
        console.log(`Email: ${message}`);
    }
}
```

Now consider an abstract class:

```ts
abstract class BaseNotification {
    constructor(
        protected recipient: string
    ) {}

    abstract send(message: string): void;

    log(message: string): void {
        console.log(
            `Notification sent to ${this.recipient}: ${message}`
        );
    }
}

class SmsNotification extends BaseNotification {
    override send(message: string): void {
        console.log(`SMS: ${message}`);
        this.log(message);
    }
}

const notification = new SmsNotification("+40123456789");

notification.send("Your order is ready");
```

Output:

```
SMS: Your order is ready
Notification sent to +40123456789: Your order is ready
```

The abstract class provides a shared `recipient` field and a reusable `log()` method.

An interface alone cannot provide these implementations.

#### Comparison

|Feature|Interface|Abstract class|
|---|---|---|
|Defines method contracts|Yes|Yes|
|Contains implemented methods|No|Yes|
|Contains initialized fields|No|Yes|
|Constructor|No|Yes|
|Abstract methods|Implicit contracts|Explicit `abstract`|
|Multiple implementation|Yes|Only one base class|
|Exists at runtime|No|Yes|
|Can instantiate directly|No|No|
|Used with|`implements`|`extends`|

A class can also extend an abstract class and implement multiple interfaces:

```ts
interface Auditable {
    audit(): void;
}

interface Exportable {
    export(): string;
}

abstract class Document {
    constructor(public title: string) {}

    abstract render(): string;
}

class PdfDocument
    extends Document
    implements Auditable, Exportable {

    override render(): string {
        return `Rendering PDF: ${this.title}`;
    }

    audit(): void {
        console.log(`Auditing ${this.title}`);
    }

    export(): string {
        return `${this.title}.pdf`;
    }
}

const document = new PdfDocument("Report");

console.log(document.render());
document.audit();
console.log(document.export());
```

This combination is common in object-oriented applications.

## 4. When to use which

Use an interface when you need to define a contract without forcing classes into a particular inheritance hierarchy. Interfaces are ideal for repositories, API clients, service contracts, DTOs, and other structures where multiple implementations may exist.

Use an abstract class when related classes share state or implementation, and you want to enforce certain methods while reusing common functionality. Abstract classes are useful for processing pipelines, payment workflows, parsers, and other situations where subclasses follow a shared algorithm.

For example, suppose you are building a data import system that supports CSV and JSON files. Both importers must parse input, validate records, and save them, but the parsing logic differs.

An abstract class can define the common workflow while requiring subclasses to implement only the parsing step.

## 5. Complete example: Data import system

This example demonstrates an abstract class implementing a reusable import workflow, similar to what you might implement in a Java/Spring Boot backend.

Create `src/abstract-classes.ts`:

```ts
interface ImportResult {
    imported: number;
    errors: string[];
}

interface Importer {
    import(content: string): ImportResult;
}

abstract class BaseImporter implements Importer {
    constructor(
        protected readonly sourceName: string
    ) {}

    protected abstract parse(
        content: string
    ): string[];

    protected validate(record: string): boolean {
        return record.trim().length > 0;
    }

    protected save(record: string): void {
        console.log(`Saved: ${record}`);
    }

    public import(content: string): ImportResult {
        console.log(`Importing from ${this.sourceName}`);

        const records = this.parse(content);
        const errors: string[] = [];
        let imported = 0;

        for (const record of records) {
            if (!this.validate(record)) {
                errors.push("Invalid empty record");
                continue;
            }

            this.save(record);
            imported++;
        }

        return { imported, errors };
    }
}

class CsvImporter extends BaseImporter {
    protected override parse(content: string): string[] {
        return content.split("\n");
    }
}

class JsonImporter extends BaseImporter {
    protected override parse(content: string): string[] {
        const data: unknown = JSON.parse(content);

        if (
            !Array.isArray(data) ||
            !data.every(item => typeof item === "string")
        ) {
            throw new Error("Expected an array of strings");
        }

        return data;
    }
}

const csvImporter: Importer = new CsvImporter("products.csv");

const csvResult = csvImporter.import(
    "Laptop\nKeyboard\n\nMouse"
);

console.log("CSV result:", csvResult);

const jsonImporter: Importer = new JsonImporter("products.json");

const jsonResult = jsonImporter.import(
    '["Monitor", "Printer", "Scanner"]'
);

console.log("JSON result:", jsonResult);
```

Compile on Ubuntu:

```sh
npx tsc src/abstract-classes.ts --target ES2022 --strict
```

Run:

```st
node src/abstract-classes.js
```

Output:

```
Importing from products.csv
Saved: Laptop
Saved: Keyboard
Saved: Mouse
CSV result: { imported: 3, errors: [ 'Invalid empty record' ] }

Importing from products.json
Saved: Monitor
Saved: Printer
Saved: Scanner
JSON result: { imported: 3, errors: [] }
```

BaseImporter implements the Importer contract; both concrete importers inherit its workflow and supply their own parsing logic.

The key design decision is that the import workflow is implemented once in `BaseImporter`. Each concrete importer only needs to implement `parse()`.

For a production application, the CSV parser would need to handle quoted fields and delimiters, while persistence would normally be delegated to a repository or API client.

### Comparison with Java

The same design can be implemented in Java:

```java
public abstract class BaseImporter {

    protected final String sourceName;

    public BaseImporter(String sourceName) {
        this.sourceName = sourceName;
    }

    protected abstract List<String> parse(String content);

    protected boolean validate(String record) {
        return !record.trim().isEmpty();
    }

    public void importData(String content) {
        List<String> records = parse(content);

        for (String record : records) {
            if (validate(record)) {
                System.out.println("Saved: " + record);
            }
        }
    }
}
```

The concepts are nearly identical: both languages support abstract classes, abstract methods, inheritance, and shared implementations.

One difference is that TypeScript's abstract class restriction disappears after compilation. JavaScript does not have native abstract classes, so TypeScript cannot enforce the restriction at runtime.