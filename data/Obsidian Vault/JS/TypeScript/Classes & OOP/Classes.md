## 1. Classes and constructors

A **class** is a blueprint for creating objects. It defines the data an object contains and the operations that can be performed on that data. TypeScript classes are based on JavaScript classes but add static typing, making them feel quite familiar if you come from Java.

A constructor is a special method executed when an object is created with `new`. Constructor parameters can be typed just like normal function parameters.

```ts
class User {
    name: string;
    age: number;

    constructor(name: string, age: number) {
        this.name = name;
        this.age = age;
    }
}

const user = new User("Alice", 30);

console.log(user.name);
console.log(user.age);
```

The constructor ensures that every `User` starts with a `name` and an `age`.

TypeScript also provides a convenient shorthand where constructor parameters can declare and initialize fields at the same time:

```ts
class Person {
    constructor(
        public name: string,
        public age: number
    ) {}
}

const person = new Person("Alice", 30);

console.log(person.name);
console.log(person.age)
```

This is roughly equivalent to explicitly declaring the fields and assigning them inside the constructor.

```sh
npx tsc constructors.ts
node constructors.js

Alice
30
```

## 2. Fields and methods

Classes usually contain both **fields** and **methods**. Fields represent the object's state, while methods represent its behavior.

```ts
class Product {
    name: string;
    price: number;

    constructor(name: string, price: number) {
        this.name = name;
        this.price = price;
    }

    getDescription(): string {
        return `${this.name} costs €${this.price}`;
    }

    applyDiscount(percent: number): void {
        this.price -= this.price * percent / 100;
    }
}

const product = new Product("Keyboard", 100);
console.log(product.getDescription());

product.applyDiscount(20);
console.log(product.getDescription());
```

Output:

```sh
npx tsc product.ts
node product.js

Keyboard costs €100
Keyboard costs €80
```

Notice that method parameters and return values are typed:

```ts
applyDiscount(percent: number): void
```

This is conceptually very similar to Java:

```java
void applyDiscount(double percent)
```

## 3. Creating objects

Objects are created from classes using `new`:

```ts
class Book {
    title: string;
    author: string;

    constructor(title: string, author: string) {
        this.title = title;
        this.author = author;
    }

    describe(): string {
        return `${this.title} by ${this.author}`;
    }
}

const book1 = new Book(
    "Effective Java",
    "Joshua Bloch"
);

const book2 = new Book(
    "Clean Code",
    "Robert C. Martin"
);

console.log(book1.describe());
console.log(book2.describe());
```

Each object has its own state:

```ts
console.log(book1.title);
// Effective Java

console.log(book2.title);
// Clean Code
```

The variable itself can also explicitly declare the class as its type:

```ts
const book: Book = new Book(
    "Effective Java",
    "Joshua Bloch"
);
```

Usually this isn't necessary because TypeScript infers it automatically:

```ts
const book = new Book(
    "Effective Java",
    "Joshua Bloch"
);
```

TypeScript already knows that `book` is a `Book`.

## 4. Comparison with Java classes

If you already know Java, TypeScript classes should look very familiar.

TypeScript:

```ts
class Customer {
    name: string;
    email: string;

    constructor(name: string, email: string) {
        this.name = name;
        this.email = email;
    }

    getInfo(): string {
        return `${this.name} <${this.email}>`;
    }
}

const customer = new Customer(
    "Alice",
    "alice@example.com"
);

console.log(customer.getInfo());
```

A similar Java class would be:

```java
public class Customer {

    private String name;
    private String email;

    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getInfo() {
        return name + " <" + email + ">";
    }
}
```

The concepts are nearly identical:

|TypeScript|Java|
|---|---|
|`class User`|`class User`|
|`constructor()`|Constructor named after class|
|`new User()`|`new User()`|
|`name: string`|`String name`|
|`age: number`|`int age` / `double age`|
|`method(): string`|`String method()`|
|`this.name`|`this.name`|

There is, however, an important difference underneath.

Java classes exist as part of Java's runtime type system. TypeScript types are primarily a **compile-time feature**. TypeScript eventually becomes JavaScript.