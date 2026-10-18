## 1. What is Angular?

Angular is an open-source, TypeScript-based frontend framework developed and maintained by Google. It is used to build modern web applications, especially Single Page Applications (SPAs). Unlike traditional websites, where the browser loads a new HTML page for every request, Angular can update parts of the interface dynamically without reloading the entire page.

Angular provides a complete development framework, including components, routing, dependency injection, forms, HTTP communication, and state management capabilities. This makes it particularly suitable for enterprise applications with complex user interfaces, multiple modules, authentication, and business workflows.

Angular runs in the browser, while backend technologies such as Java and Spring Boot handle business logic, database operations, and security. The two communicate through APIs, usually REST or GraphQL.

### Example: A simple Angular component

```ts
import { Component } from '@angular/core';

@Component({
  selector: 'app-welcome',
  standalone: true,
  template: `
    <h1>Welcome to Angular</h1>
    <p>Hello, {{ username }}!</p>

    <button (click)="changeName()">
      Change Name
    </button>
  `
})
export class WelcomeComponent {
  username: string = 'Developer';

  changeName(): void {
    this.username = 'Angular Developer';
  }
}
```

This component demonstrates several Angular concepts:

- `@Component` defines an Angular component.
- `selector` identifies the component in HTML.
- `template` defines the user interface.
- `{{ username }}` displays a TypeScript property.
- `(click)` handles a browser event.
- Changing `username` automatically updates the displayed text.
    
Unlike traditional JavaScript, you do not need to manually locate and modify HTML elements using `document.getElementById()`.

## 2. Angular vs React vs Vue

Angular, React, and Vue solve similar frontend problems, but they differ in architecture, complexity, and development philosophy.

Angular: 

- A complete, opinionated framework. Provides routing, dependency injection, HTTP integration, forms, and an established application structure.

React:

- A flexible UI library focused on components. Developers typically choose additional libraries for routing, data management, and other application features.

Vue.js:

- A progressive framework with a relatively gentle learning curve. Suitable for both small interfaces and complex applications.

### Comparison

|Feature|Angular|React|Vue|
|---|---|---|---|
|Type|Full framework|UI library|Progressive framework|
|Main language|TypeScript|JS / TS|JS / TS|
|Learning curve|Higher|Medium|Lower|
|Routing|Official|Additional library|Official|
|Dependency injection|Built-in|Not central|Provide/inject|
|Enterprise structure|Strong conventions|Flexible|Flexible|
|Small applications|More setup|Suitable|Very suitable|

For example, an application uses Vue because it provides a lightweight way to build an interactive interface. Angular become particularly interesting when the application grew into a larger system with many independent modules, complex forms, administration panels, and multiple frontend developers.

Angular's main advantage is not that it can build interfaces React or Vue cannot. It is that it provides a consistent architecture and conventions for organizing large applications.

## 3. Single Page Application (SPA) Architecture

A Single Page Application (SPA) loads an initial HTML document and then dynamically updates the interface as users navigate or interact with the application. Instead of requesting a completely new HTML page from the server for every navigation, the frontend JavaScript framework handles much of the navigation and rendering.

For example, when a user navigates from `/products` to `/products/10`, Angular Router determines which component should appear. That component may request product information from a backend API, and Angular updates the interface with the returned data.

### Example: Angular service calling a REST API

Suppose Spring Boot exposes:

```
GET http://localhost:8080/api/products/10
```

Response:

```json
{
  "id": 10,
  "name": "Laptop",
  "price": 1200
}
```

Angular can retrieve the product through an injectable service:

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

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(
      `http://localhost:8080/api/products/${id}`
    );
  }
}
```

`HttpClient` is Angular's built-in HTTP client. It returns an RxJS `Observable`, which represents a stream of asynchronous values. We will study observables in a later lesson.

To use this service, the application must configure `provideHttpClient()` in its application providers.

Important: Angular does not replace the backend. The frontend handles presentation and user interactions, while Spring Boot remains responsible for business rules, authorization, persistence, and other server-side operations.

## 4. Angular + Spring Boot

Angular and Spring Boot are a common combination for enterprise applications because they provide a clear separation between frontend and backend responsibilities.

Angular handles UI components, navigation, forms, client-side validation, and HTTP requests. Spring Boot provides REST controllers, services, security, database integration, and business logic. This separation allows both applications to be developed, tested, and deployed independently.

### Typical project structure

```
product-management/
│
├── backend/
│   ├── src/main/java/com/example/
│   │   ├── controller/
│   │   │   └── ProductController.java
│   │   ├── service/
│   │   │   └── ProductService.java
│   │   ├── repository/
│   │   │   └── ProductRepository.java
│   │   └── model/
│   │       └── Product.java
│   └── pom.xml
│
└── frontend/
    ├── src/
    │   └── app/
    │       ├── products/
    │       │   ├── product-list.ts
    │       │   └── product-list.html
    │       ├── services/
    │       │   └── product.service.ts
    │       ├── app.config.ts
    │       └── app.routes.ts
    ├── angular.json
    └── package.json
```

### Spring Boot REST controller

```java
package com.example.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public List<Product> getProducts() {
        return List.of(
            new Product(1L, "Laptop", 1200.0),
            new Product(2L, "Monitor", 300.0)
        );
    }

    public record Product(
        Long id,
        String name,
        Double price
    ) {}
}
```

This controller exposes:

```
GET http://localhost:8080/api/products
```

### Angular component consuming the API

```ts
import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductService, Product } from './product.service';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2>Products</h2>

    @for (product of products; track product.id) {
      <p>
        {{ product.name }} - ${{ product.price }}
      </p>
    }
  `
})
export class ProductListComponent implements OnInit {

  private productService = inject(ProductService);

  products: Product[] = [];

  ngOnInit(): void {
    this.productService.getProducts()
      .subscribe(data => {
        this.products = data;
      });
  }
}
```

The corresponding service method would be:

```ts
getProducts(): Observable<Product[]> {
  return this.http.get<Product[]>(
    'http://localhost:8080/api/products'
  );
}
```

This example introduces dependency injection, lifecycle hooks, HTTP services, and the `@for` control-flow syntax. Each will be covered in detail in later lessons.

### Local development

Typically, you run both applications separately:

|Application|URL|Responsibility|
|---|---|---|
|Angular|`http://localhost:4200`|Frontend|
|Spring Boot|`http://localhost:8080`|Backend|
|PostgreSQL|`localhost:5432`|Database|

Because the frontend and backend use different origins, you will need to configure CORS in Spring Boot or use an Angular development proxy. For production, you can deploy the compiled Angular application through a web server such as Nginx or Apache, while Spring Boot serves the API.

## 5. Key Concepts to Remember

- Angular is a frontend framework, not a replacement for Spring Boot.
- TypeScript is Angular's primary programming language, with Java-like concepts such as classes, interfaces, and access modifiers.
- Components are the building blocks of Angular interfaces.
- Services provide reusable logic and access to backend APIs.
- Dependency injection connects components and services without manually constructing every dependency.
- SPA routing allows navigation without full-page reloads in typical client-side navigation.
- Angular + Spring Boot provides a clear separation between the UI and business logic.