## 1. Inheritance with extends

Inheritance allows a class to reuse properties and methods from another class. In TypeScript, the `extends` keyword establishes a parent-child relationship between classes, just like in Java. The parent is called the base class, while the child is called the derived class.

Inheritance is useful when several classes share common behavior but also require specialized functionality. For example, an application might have different employee types that share a name and salary but calculate bonuses differently.

```ts
class Employee {
    constructor(
        public name: string,
        public salary: number
    ) {}

    getInfo(): string {
        return `${this.name}: €${this.salary}`;
    }
}

class Developer extends Employee {
    programmingLanguage: string;

    constructor(
        name: string,
        salary: number,
        programmingLanguage: string
    ) {
        super(name, salary);
        this.programmingLanguage = programmingLanguage;
    }

    writeCode(): string {
        return `${this.name} writes ${this.programmingLanguage}`;
    }
}

const developer = new Developer(
    "Alice",
    5000,
    "Java"
);

console.log(developer.getInfo());
console.log(developer.writeCode());
```

Output:

```txt
Alice: €5000
Alice writes Java
```

The `Developer` class automatically inherits `name`, `salary`, and `getInfo()` from `Employee`.

## 2. Method overriding

Method overriding allows a derived class to provide its own implementation of a method inherited from the base class. TypeScript supports the `override` keyword, which tells the compiler that the method must exist in the parent class.

This is particularly useful when different subclasses implement the same operation differently. For example, developers and managers may have different bonus calculations.

```ts
class Employee {
    constructor(
        public name: string,
        protected salary: number
    ) {}

    calculateBonus(): number {
        return this.salary * 0.10;
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
    new Employee("Alice", 5000),
    new Developer("Bob", 5000),
    new Manager("Charlie", 5000)
];

for (const employee of employees) {
    console.log(
        `${employee.name}: €${employee.calculateBonus()}`
    );
}
```

Output:

```txt
Alice: €500
Bob: €750
Charlie: €1250
```

Even though the array is declared as `Employee[]`, TypeScript executes the overridden method based on the actual object type. This behavior is called runtime polymorphism, and it works similarly to Java.

The `override` keyword is optional by default, but recommended. You can enforce it in `tsconfig.json`:

```json
{
  "compilerOptions": {
    "strict": true,
    "noImplicitOverride": true
  }
}
```

With `noImplicitOverride`, the compiler requires `override` when a derived class overrides an inherited method.

## 3. The `super` keyword

The `super` keyword provides access to the parent class's constructor and methods. It is commonly used in two situations: initializing inherited properties and extending existing parent behavior.

When a derived class declares its own constructor, it must call `super()` before accessing `this`. This ensures that the parent class is initialized first.

```ts
class Product {
    constructor(
        public name: string,
        protected price: number
    ) {}

    getDescription(): string {
        return `${this.name}: €${this.price}`;
    }
}

class DigitalProduct extends Product {
    constructor(
        name: string,
        price: number,
        public downloadUrl: string
    ) {
        super(name, price);
    }

    override getDescription(): string {
        const baseDescription = super.getDescription();

        return `${baseDescription} (Digital download)`;
    }
}

const ebook = new DigitalProduct(
    "TypeScript Guide",
    30,
    "/downloads/typescript.pdf"
);

console.log(ebook.getDescription());
console.log(ebook.downloadUrl);
```

Output:

```txt
TypeScript Guide: €30 (Digital download)
/downloads/typescript.pdf
```

Notice the two uses of `super`:

- `super(name, price)` invokes the parent constructor.
- `super.getDescription()` invokes the parent's implementation of an overridden method.

## 4. Base and derived classes

A base class defines shared properties and behavior, while derived classes extend or customize that behavior. A derived class can be assigned to a variable typed as its base class because it satisfies the base class's contract.

However, TypeScript differs from Java in an important way: TypeScript uses structural typing, whereas Java primarily uses nominal typing for classes. This means TypeScript checks whether an object has the required structure rather than always requiring an explicit inheritance relationship.

```ts
class Animal {
    constructor(public name: string) {}

    speak(): string {
        return `${this.name} makes a sound`;
    }
}

class Dog extends Animal {
    override speak(): string {
        return `${this.name} barks`;
    }

    fetch(): string {
        return `${this.name} fetches the ball`;
    }
}

const dog = new Dog("Rex");

const animal: Animal = dog; // Look Here

console.log(animal.speak()); // Rex barks

// Compilation error:
// animal.fetch();
```

Although the underlying object is a `Dog`, the variable `animal` is typed as `Animal`. Therefore, only members available through the `Animal` type can be accessed directly.

You can check the runtime type using `instanceof`:

```ts
if (animal instanceof Dog) {
    console.log(animal.fetch());
}
```

Output:

```txt
Rex fetches the ball
```

TypeScript also allows a class to extend only one class at a time, just like Java. Multiple inheritance of classes is not supported, although classes can implement multiple interfaces.

## 5. Complete example: Product hierarchy

This example models different product types in an e-commerce application. The same concept could be used in an Angular frontend consuming product data from a Spring Boot REST API.

Create `src/inheritance.ts`:

```ts
class Product {
    constructor(
        public readonly id: number,
        public name: string,
        protected price: number
    ) {}

    getPrice(): number {
        return this.price;
    }

    getDescription(): string {
        return `${this.name}: €${this.price}`;
    }

    calculateShipping(): number {
        return 10;
    }

    getTotalPrice(): number {
        return this.price + this.calculateShipping();
    }
}

class PhysicalProduct extends Product {
    constructor(
        id: number,
        name: string,
        price: number,
        public weight: number
    ) {
        super(id, name, price);
    }

    override calculateShipping(): number {
        return this.weight * 5;
    }

    override getDescription(): string {
        return `${super.getDescription()} (${this.weight} kg)`;
    }
}

class DigitalProduct extends Product {
    constructor(
        id: number,
        name: string,
        price: number,
        public downloadUrl: string
    ) {
        super(id, name, price);
    }

    override calculateShipping(): number {
        return 0;
    }

    override getDescription(): string {
        return `${super.getDescription()} (Digital)`;
    }
}

const products: Product[] = [
    new PhysicalProduct(1, "Laptop", 1200, 2),
    new PhysicalProduct(2, "Keyboard", 100, 0.5),
    new DigitalProduct(
        3,
        "TypeScript Guide",
        30,
        "/downloads/typescript.pdf"
    )
];

for (const product of products) {
    console.log(product.getDescription());
    console.log(`Shipping: €${product.calculateShipping()}`);
    console.log(`Total: €${product.getTotalPrice()}`);
    console.log("---");
}
```

Compile on Ubuntu:

```sh
npx tsc src/inheritance.ts --target ES2022 --strict
```

Run:

```sh
node src/inheritance.js
```

Output:

```txt
Laptop: €1200 (2 kg)
Shipping: €10
Total: €1210
---
Keyboard: €100 (0.5 kg)
Shipping: €2.5
Total: €102.5
---
TypeScript Guide: €30 (Digital)
Shipping: €0
Total: €30
---
```

Notice that `getTotalPrice()` is implemented only once in the base class. It automatically uses the appropriate `calculateShipping()` implementation for each product type.

This is a practical example of inheritance combined with polymorphism.

## TypeScript vs Java comparison

|Feature|TypeScript|Java|
|---|---|---|
|Inheritance|`extends`|`extends`|
|Parent constructor|`super()`|`super()`|
|Parent method|`super.method()`|`super.method()`|
|Method overriding|`override`|`@Override`|
|Multiple class inheritance|Not supported|Not supported|
|Runtime type check|`instanceof`|`instanceof`|
|Class compatibility|Primarily structural|Nominal|
|Access to parent fields|`protected`|`protected`|

One subtle difference is that Java's `@Override` is an annotation, while TypeScript's `override` is a keyword checked by the compiler. Neither changes runtime method dispatch.