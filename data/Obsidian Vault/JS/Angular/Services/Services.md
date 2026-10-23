## 1. What is an Angular service?

An Angular service is a TypeScript class that contains logic shared between components or logic that does not belong directly in the UI. Typical responsibilities include fetching data from APIs, managing application state, authentication, and implementing reusable business operations.

In Angular, components should primarily handle user interaction and rendering, while services handle data access and application logic. This is similar to the separation between `@RestController` and `@Service` in Spring Boot, although Angular services run in the browser rather than on the backend.

|Angular|Spring Boot|
|---|---|
|`@Component`|Controller / UI adapter|
|`@Injectable`|`@Service`|
|`inject()`|Constructor injection|
|`providedIn: 'root'`|Singleton-scoped Spring bean|
|`HttpClient`|`RestClient` / `WebClient`|

## 2. Creating a service

We will continue with the Product application, using the Spring Boot endpoint `/api/products`.

Generate a service using Angular CLI:

```
ng generate service services/product
```

This creates:

```
src/app/
├── services/
│   ├── product.ts
│   └── product.spec.ts
├── models/
│   └── product.ts
├── product-list/
│   ├── product-list.ts
│   └── product-list.html
└── app.config.ts
```

First, define the Product interface.

`src/app/models/product.ts`

```ts
export interface Product {
  id: number;
  name: string;
  price: number;
  description?: string;
}
```

Next, implement the service.

`src/app/services/product.ts`

```ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Product } from '../models/product';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = '/api/products';

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl);
  }

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(
      `${this.apiUrl}/${id}`
    );
  }
}
```

The `@Injectable()` decorator tells Angular that the class can participate in dependency injection. With `providedIn: 'root'`, Angular registers the service with the root injector, making it available throughout the application.

The `HttpClient` dependency is obtained using `inject()`, Angular's modern dependency injection API. The `getProducts()` method returns an `Observable<Product[]>`, allowing components to subscribe to asynchronous HTTP responses.

Make sure `HttpClient` is configured in `app.config.ts`:

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

## 3. Using a service in a component

Instead of making HTTP requests directly from the component, inject `ProductService` and delegate data access to it.

`src/app/product-list/product-list.ts`

```ts
import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';

import { Product } from '../models/product';
import { ProductService } from '../services/product';

@Component({
  selector: 'app-product-list',
  standalone: true,
  templateUrl: './product-list.html'
})
export class ProductList implements OnInit {

  private readonly productService = inject(ProductService);

  products = signal<Product[]>([]);
  loading = signal(false);
  error = signal('');

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading.set(true);
    this.error.set('');

    this.productService.getProducts().subscribe({
      next: (data) => {
        this.products.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error(err);
        this.error.set('Failed to load products');
        this.loading.set(false);
      }
    });
  }
}
```

`src/app/product-list/product-list.html`

```html
<h2>Products</h2>

@if (loading()) {
  <p>Loading products...</p>
}

@if (error()) {
  <p>{{ error() }}</p>
}

@for (product of products(); track product.id) {
  <div>
    <h3>{{ product.name }}</h3>
    <p>Price: {{ product.price }}</p>
    <p>{{ product.description }}</p>
  </div>
} @empty {
  @if (!loading() && !error()) {
    <p>No products available.</p>
  }
}
```

Notice that the component no longer knows the API URL or how HTTP requests are constructed. It only asks the service for products and updates the signals used by the template.

For this simple example, `HttpClient` observables normally complete after returning a response, so the subscription does not require manual cleanup.

## 4. Singleton services

By default, a service declared with `providedIn: 'root'` has one instance per Angular application root injector.

Consider a simple counter service:

`src/app/services/counter.ts`

```ts
import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CounterService {

  count = signal(0);

  increment(): void {
    this.count.update(value => value + 1);
  }
}
```

Two components can inject the same service:

```ts
private counterService = inject(CounterService);

count = this.counterService.count;
```

When one component calls:

```ts
this.counterService.increment();
```

Both components observe the updated value because they reference the same signal on the same service instance.

This is useful for application-wide state such as the authenticated user, shopping cart, or selected theme.

Important: A root-provided singleton is not shared between different browser tabs, different users, or separate Angular applications.

## 5. Angular vs Spring Boot

Although both frameworks use dependency injection, their services typically have different responsibilities.

Spring Boot:

```java
@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> findAll() {
        return repository.findAll();
    }
}
```

Angular:

```ts
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

The Spring Boot service implements server-side business logic and accesses persistence. The Angular service communicates with the backend and may coordinate client-side state or UI-related operations.

Business rules that must be enforced for security or data integrity should remain on the backend. Angular services cannot be trusted as an enforcement boundary because browser-side code can be modified or bypassed.