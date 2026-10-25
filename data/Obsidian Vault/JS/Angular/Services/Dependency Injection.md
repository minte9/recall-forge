## 1. What is DI?

Dependency Injection (DI) is a design pattern where a class receives its dependencies from an external system instead of creating them itself. Angular has a built-in dependency injection system that creates services, manages their lifecycle, and supplies them to components or other services.

This is very similar to Spring Boot, where the Spring IoC container manages beans annotated with `@Service`, `@Component`, or `@Repository`. Angular uses injectors to manage dependencies, while Spring uses the `ApplicationContext`.

Without dependency injection:

```ts
export class ProductList {
  private productService = new ProductService();
}
```

This approach creates tight coupling and may not work when `ProductService` has dependencies of its own, such as `HttpClient`.

With dependency injection:

```ts
import { Component, inject } from '@angular/core';
import { ProductService } from '../services/product';

@Component({
  selector: 'app-product-list',
  standalone: true,
  template: `<h2>Products</h2>`
})
export class ProductList {
  private productService = inject(ProductService);
}
```

Angular resolves `ProductService` automatically using its injector.

## 2. Using inject()

Modern Angular supports the `inject()` function, which is commonly preferred for declaring dependencies in components and services.

Consider our existing `ProductService`:

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

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>('/api/products');
  }
}
```

Angular resolves two dependencies in sequence:

1. `ProductList` requests `ProductService`.
2. `ProductService` requests `HttpClient`.
    

The injector creates or retrieves the required instances.

The equivalent Spring Boot approach uses constructor injection:

```java
@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }
}
```

## 3. Providers

A provider tells Angular how to obtain a dependency. Angular can create an instance of a class, supply an existing instance, or return a configured value.

The most common approach is:

```ts
@Injectable({
  providedIn: 'root'
})
export class ProductService {}
```

This registers the service with the root environment injector, so it can be injected throughout the application.

Alternatively, you can register providers explicitly in `app.config.ts`:

```ts
import { ApplicationConfig } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';

import { ProductService } from './services/product';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(),
    ProductService
  ]
};
```

Explicitly registering `ProductService` at the root is unnecessary when it already uses `providedIn: 'root'`.

Providers can also be scoped to individual components:

```ts
import { Component } from '@angular/core';
import { CounterService } from '../services/counter';

@Component({
  selector: 'app-counter',
  standalone: true,
  providers: [CounterService],
  template: `<p>Counter component</p>`
})
export class CounterComponent {}
```

In this case, each instance of `CounterComponent` receives its own `CounterService` instance. Its descendants can share that instance unless they override the provider.

## 4. Injection hierarchy

Unlike a single global service registry, Angular has a hierarchical dependency injection system.

When a component requests a dependency, Angular searches for a suitable provider, starting from the nearest applicable injector and continuing through the injector hierarchy.

Simplified provider hierarchy showing root and component-scoped dependencies.

Consider a service defined at the root:

```ts
@Injectable({
  providedIn: 'root'
})
export class CounterService {
  count = 0;
}
```

If both `ComponentA` and `ComponentB` inject it without overriding its provider, they share the same instance.

However, if `ComponentB` declares:

```ts
@Component({
  selector: 'app-component-b',
  standalone: true,
  providers: [CounterService],
  template: `<p>Component B</p>`
})
export class ComponentB {
  counter = inject(CounterService);
}
```

Angular creates a separate `CounterService` instance for each instance of `ComponentB`. This is called provider shadowing: the nearer provider takes precedence over the root provider.

## 5. Provider types

Angular supports several ways of configuring providers.

|Provider|Purpose|
|---|---|
|`useClass`|Create an instance of a specified class|
|`useValue`|Supply an existing object or constant|
|`useFactory`|Create a dependency using a factory function|
|`useExisting`|Reuse an already registered dependency|

For example, suppose we want to configure the backend API URL.

`src/app/config/api.config.ts`

```ts
import { InjectionToken } from '@angular/core';

export const API_URL = new InjectionToken<string>('API_URL');
```

Register the value in `app.config.ts`:

```ts
import { ApplicationConfig } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';

import { API_URL } from './config/api.config';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(),
    {
      provide: API_URL,
      useValue: '/api'
    }
  ]
};
```

Now inject it into `ProductService`:

```ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from '../config/api.config';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_URL);

  getProducts() {
    return this.http.get(`${this.apiUrl}/products`);
  }
}
```

An `InjectionToken` allows Angular to inject values that do not have a runtime class token, such as strings or configuration objects.

This is useful when switching between development and production API configurations.

## 6. Practical example — service instance scope

We can demonstrate the difference between root and component-level providers using a counter.

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

`src/app/counter/counter.ts`

```ts
import { Component, inject } from '@angular/core';
import { CounterService } from '../services/counter';

@Component({
  selector: 'app-counter',
  standalone: true,
  template: `
    <p>Count: {{ counter.count() }}</p>
    <button (click)="counter.increment()">
      Increment
    </button>
  `
})
export class CounterComponent {

  readonly counter = inject(CounterService);
}
```

Render two instances in the parent:

```html
<app-counter />
<app-counter />
```

Because `CounterService` is provided at root, both counters display the same value. Incrementing one updates the other.

Both components share one service instance. Incrementing either updates both counters.

To reproduce the second behavior in Angular, add:

```ts
@Component({
  selector: 'app-counter',
  standalone: true,
  providers: [CounterService],  // Look Here
  template: `
    <p>Count: {{ counter.count() }}</p>
    <button (click)="counter.increment()">
      Increment
    </button>
  `
})
export class CounterComponent {

  readonly counter = inject(CounterService);
}
```

Now each counter has its own state.

## 7. Key takeaways

- Angular dependency injection is conceptually similar to Spring Boot's IoC container.
- `inject()` is a modern way to request dependencies.
- `providedIn: 'root'` creates an application-wide service instance within the root injector.
- `providers` on a component creates component-scoped service instances.
- Angular uses a hierarchical injector system, allowing providers to be overridden.
- `InjectionToken` is useful for configuration values and interface-like dependency contracts.