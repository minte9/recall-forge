## 1. Types and Type Inference

Angular uses TypeScript as its primary programming language. TypeScript extends JavaScript with static typing, interfaces, generics, access modifiers, and other features familiar to Java developers. Its type system helps detect errors during development, before the application runs in the browser.

Unlike Java, TypeScript often determines variable types automatically through type inference. You can explicitly declare a type or allow the compiler to infer it from the assigned value. TypeScript types are checked at compile time and are generally removed when the code is compiled into JavaScript.

### Example: Explicit types

```ts
let username: string = 'Developer';
let age: number = 30;
let active: boolean = true;

let skills: string[] = [
  'Java',
  'Spring Boot',
  'Angular'
];

function calculatePrice(
  price: number,
  quantity: number
): number {
  return price * quantity;
}
```

### Example: Type inference

```ts
let username = 'Developer'; // inferred string
let age = 30;               // inferred number
let active = true;          // inferred boolean

username = 'Angular Developer'; // valid

// username = 100; // compilation error
```

The compiler infers `username` as `string`, so assigning a number produces a type error.

### Java comparison

Java:

```java
String username = "Developer";
int age = 30;

var active = true;
```

TypeScript:

```ts
let username: string = 'Developer';
let age: number = 30;

let active = true;
```

An important difference is that TypeScript uses the same `number` type for integers and floating-point numbers.

### Angular example

```ts
import { Component } from '@angular/core';

@Component({
  selector: 'app-user',
  standalone: true,
  template: `
    <h2>{{ username }}</h2>
    <p>Age: {{ age }}</p>
    <p>Active: {{ active }}</p>
  `
})
export class UserComponent {
  username: string = 'John';
  age = 30;
  active = true;
}
```

Angular templates access the properties declared in the component class. TypeScript helps ensure that the component's properties and methods are used correctly.

## 2. Interfaces and Type Aliases

Interfaces define the structure of objects. They describe which properties and methods an object must contain without providing their implementation. This makes them particularly useful for modeling data transferred between Angular and Spring Boot through REST APIs.

A type alias also defines reusable types, but it can represent more than object structures, including unions, primitives, and combinations of types. In Angular projects, interfaces are commonly used for DTOs and domain models, while type aliases are useful for restricted values and more complex type definitions.

### Example: Interface

```ts
export interface Product {
  id: number;
  name: string;
  price: number;
  available: boolean;
}
```

Creating an object:

```ts
const product: Product = {
  id: 1,
  name: 'Laptop',
  price: 1200,
  available: true
};
```

The compiler checks whether the object matches the interface.

```ts
const product: Product = {
  id: 1,
  name: 'Laptop'
  // Error: price and available are missing
};
```

### Example: Type alias

```ts
type ProductStatus =
  | 'AVAILABLE'
  | 'OUT_OF_STOCK'
  | 'DISCONTINUED';

interface Product {
  id: number;
  name: string;
  status: ProductStatus;
}
```

Only the three declared values are accepted.

```ts
const product: Product = {
  id: 1,
  name: 'Laptop',
  status: 'AVAILABLE'
};

// product.status = 'UNKNOWN'; // Error
```

### Java comparison

In Java, you might define a DTO using a record:

```java
public record ProductDto(
    Long id,
    String name,
    Double price,
    boolean available
) {}
```

In Angular, the corresponding interface would be:

```ts
export interface ProductDto {
  id: number;
  name: string;
  price: number;
  available: boolean;
}
```

However, there is an important difference: TypeScript interfaces do not exist at runtime.

For example:

```ts
const product = response as ProductDto;
```

This tells the compiler to treat `response` as `ProductDto`, but it does not validate the actual JSON data. Runtime validation requires additional checks or a validation library.

### Angular service example

```ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Product {
  id: number;
  name: string;
  price: number;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private http = inject(HttpClient);

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>('/api/products');
  }
}
```

Here, `Observable<Product[]>` indicates that the HTTP request will asynchronously produce an array of products.

The `Product` interface provides compile-time type checking when working with the response.

## 3. Classes and Access Modifiers

TypeScript supports classes, constructors, methods, inheritance, and access modifiers. These concepts are similar to Java, although TypeScript has a different runtime model because its classes compile to JavaScript.

Angular uses classes extensively for components and services. A component class stores the state and behavior of a user interface, while a service class typically contains reusable business or API communication logic.

### Example: TypeScript class

```ts
class Product {

  constructor(
    public id: number,
    public name: string,
    private price: number
  ) {}

  getPrice(): number {
    return this.price;
  }

  applyDiscount(percent: number): void {
    this.price -= this.price * percent / 100;
  }
}

const product = new Product(1, 'Laptop', 1200);

product.applyDiscount(10);

console.log(product.getPrice()); // 1080
```

TypeScript allows constructor parameter properties, which automatically declare and initialize fields.

### Access modifiers

|Modifier|Meaning|Java equivalent|
|---|---|---|
|`public`|Accessible everywhere|`public`|
|`private`|Accessible within the class|`private`|
|`protected`|Accessible within class and subclasses|`protected`|
|`readonly`|Cannot be reassigned after initialization|Similar to `final` for references|

TypeScript's `private` and `protected` are primarily compile-time restrictions. JavaScript `#private` fields provide runtime privacy.

### Angular example: Dependency injection

```ts
import { Component, inject } from '@angular/core';
import { ProductService } from './product.service';

@Component({
  selector: 'app-products',
  standalone: true,
  template: `
    <h2>Product Management</h2>
    <button (click)="loadProducts()">
      Load Products
    </button>
  `
})
export class ProductsComponent {

  private productService = inject(ProductService);

  loadProducts(): void {
    this.productService.getProducts()
      .subscribe(products => {
        console.log(products);
      });
  }
}
```

Angular creates and injects the `ProductService` dependency through its dependency injection system.

This is conceptually similar to constructor injection in Spring Boot:

```java
@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }
}
```

Both frameworks manage dependencies rather than requiring developers to instantiate every service manually.

## 4. Generics and Optional Properties

Generics allow classes, interfaces, and functions to operate with different types while preserving type safety. In TypeScript, generics use syntax similar to Java, such as `T`, `Array<T>`, and `ApiResponse<T>`.

Optional properties are declared using `?`. They indicate that an object property may be absent, which is useful when working with API responses, partial updates, or optional form fields.

### Example: Generic interface

```ts
interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}
```

Using it with a product:

```ts
interface Product {
  id: number;
  name: string;
  price: number;
}

const response: ApiResponse<Product> = {
  success: true,
  data: {
    id: 1,
    name: 'Laptop',
    price: 1200
  }
};
```

Using it with an array:

```ts
const response: ApiResponse<Product[]> = {
  success: true,
  data: [
    { id: 1, name: 'Laptop', price: 1200 },
    { id: 2, name: 'Monitor', price: 300 }
  ]
};
```

### Java comparison

```java
public record ApiResponse<T>(
    boolean success,
    T data,
    String message
) {}
```

Both languages use `T` as a placeholder for a concrete type.

### Optional properties

```ts
interface User {
  id: number;
  username: string;
  email?: string;
}

const user: User = {
  id: 1,
  username: 'john'
};
```

The `email` property is optional. Reading it normally produces the type `string | undefined`.

```ts
console.log(user.email); // undefined
```

You can safely handle missing values using optional chaining and nullish coalescing:

```ts
const email = user.email ?? 'No email provided';

console.log(email); // No email provided
```

### Angular example: Generic API response

Suppose Spring Boot returns:

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Laptop",
      "price": 1200
    }
  ],
  "message": "Products retrieved"
}
```

Define the TypeScript models:

```ts
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

export interface Product {
  id: number;
  name: string;
  price: number;
}
```

Then create the Angular service:

```ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApiResponse, Product } from './models';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private http = inject(HttpClient);

  getProducts(): Observable<ApiResponse<Product[]>> {
    return this.http.get<ApiResponse<Product[]>>(
      '/api/products'
    );
  }
}
```

The service now expects a structured API response rather than a plain array.

## 5. Complete Example: Typed Product Component

Let's combine interfaces, classes, generics, optional properties, and Angular dependency injection into one small example.

### Spring Boot REST Application

Assume the Spring Boot backend exposes:

```
GET /api/products
```

Rest controller:

```java
@RestController
public class ProductController {

    @GetMapping("/api/products")
    public List<Product> products() {

        List<Product> products = Arrays.asList(
            new Product(1, "Laptop", 1200, "Business Laptop"),
            new Product(2, "Monitor", 300, "Dell Monitor")
        );

        return products;
    }
}
```

Record:

```java
package com.minte9.rest_service;

public record Product(
    int id,
    String name,
    double price,
    String description
) {
    // Overloaded constructor allowing creation without a description
    public Product(int id, String name, double price) {
        this(id, name, price, null); 
    }
}
```

Request:

```sh
curl http://localhost:9091/api/products | jq
```

Result:

```ts
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Laptop",
      "price": 1200,
      "description": "Business laptop"
    },
    {
      "id": 2,
      "name": "Monitor",
      "price": 300
    }
  ]
}
```

### Angular Application

Create `proxy.conf.json` in the Angular project root:

```json
{
  "/api/**": {
    "target": "http://localhost:9091",
    "secure": false,
    "changeOrigin": true
  }
}
```

src/app/product/`product.model.ts`

```ts
export interface Product {
  id: number;
  name: string;
  price: number;
  description: string | null;
}
```

src/app/product/`product.service.ts`

```ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Product } from './product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private readonly http = inject(HttpClient);

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>('/api/products');
  }
}
```

The `@Injectable({ providedIn: 'root' })` annotation tells Angular to register this service in its dependency injection system.

The service calls `/api/products` using Angular's `HttpClient`. Later, our development proxy will forward this request to Spring Boot on port `9091`.

src/app/product/`product-list.ts`

```ts
import {
  Component,
  inject,
  OnInit,
  signal
} from '@angular/core';

import { ProductService } from './product.service';
import { Product } from './product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  template: `
    <h2>Products</h2>

    @for (product of products(); track product.id) {
      <div>
        <h3>{{ product.name }}</h3>
        <p>Price: {{ product.price }} EUR</p>
        <p>
          {{ product.description ?? 'No description' }}
        </p>
      </div>
    } @empty {
      <p>No products available.</p>
    }
  `
})
export class ProductListComponent implements OnInit {

  private readonly productService = inject(ProductService);

  products = signal<Product[]>([]);

  ngOnInit(): void {
    this.productService.getProducts().subscribe({
      next: products => {
        console.log('Received products:', products);

        this.products.set(products);
      },
      error: error => {
        console.error('Failed to load products:', error);
      }
    });
  }
}
```

When Angular creates this component, it calls `ngOnInit()`. The component requests products through `ProductService`, subscribes to the HTTP response, and assigns the returned products to the `products` array.

Angular then renders each product using `@for`.

The example assumes `provideHttpClient()` has been registered in `app.config.ts`, the component has been included in the application, and `/api` requests are forwarded to Spring Boot.

src/app/`app.config.ts`

```ts
import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient()
  ]
};
```

If your generated configuration already contains additional providers, retain them and add `provideHttpClient()`.

This is necessary because `ProductService` injects `HttpClient`.

src/app/`app.ts`

```ts
import { Component } from '@angular/core';

import { ProductListComponent } from './products/product-list';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ProductListComponent],
  template: `
    <h1>Angular + Spring Boot Demo</h1>

    <app-product-list />
  `
})
export class App {}
```

Now Angular requests:

```
http://localhost:4200/api/products
```

The Angular development server forwards them to:

```
http://localhost:9091/api/products
```

This avoids browser CORS problems during development.

Stop Angular using `Ctrl+C`, then run:

```sh
ng serve --port 4201 --proxy-config proxy.conf.json
```

On another terminal check:

```sh
curl -i http://localhost:4201/api/products

[{"id":1,"name":"Laptop","price":1200.0,"description":"Business Laptop"},{"id":2,"name":"Monitor","price":300.0,"description":null}]
```

Expected browser interface:

```txt
<h2>Products</h2>

Laptop
Price: $1200
Business laptop

Monitor
Price: $300

No description
```

The important concept is that the same `Product` interface is reused throughout the frontend. The HTTP service returns typed data, the component stores a `Product[]`, and the template accesses the properties defined by that interface.