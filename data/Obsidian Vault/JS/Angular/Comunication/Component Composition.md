## 1. Parent and child

Angular applications are built using a hierarchy of components. A parent component can include one or more child components through their selectors. Each component has its own TypeScript logic, HTML template, and optional CSS styles.

Generate the child component using Angular CLI:

```sh
ng generate component products/product-card
```

This creates the component files under `src/app/products/product-card/`.

## 2. Reusable components

A reusable component should focus on one responsibility. In our example, `ProductCard` displays product information, while `ProductList` manages the collection.

First, define a shared interface.

`src/app/products/product.ts`

```ts
export interface Product {
  id: number;
  name: string;
  price: number;
  description?: string;
}
```

Next, implement the child component using Angular's modern signal-based `input()` API.

`src/app/products/product-card/product-card.ts`

```ts
import { Component, input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Product } from '../product';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './product-card.html',
  styleUrl: './product-card.css'
})
export class ProductCard {
  product = input.required<Product>();
}
```

`product-card.html`

```html
<div class="product-card">
  <h3>{{ product().name }}</h3>

  <p>{{ product().description }}</p>

  <strong>
    {{ product().price | currency:'EUR' }}
  </strong>
</div>
```

`product-card.css`

```css
.product-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}
```

Notice that `product` is an input signal, so the template reads its value with `product()` instead of `product`.

Now create the parent component.

`src/app/products/product-list/product-list.ts`

```ts
import { Component, signal } from '@angular/core';
import { ProductCard } from '../product-card/product-card';
import { Product } from '../product';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [ProductCard],
  templateUrl: './product-list.html'
})
export class ProductListComponent {

  products = signal<Product[]>([
    {
      id: 1,
      name: 'Laptop',
      price: 1200,
      description: 'Business Laptop'
    },
    {
      id: 2,
      name: 'Monitor',
      price: 300
    }
  ]);
}
```

`product-list.html`

```html
<h2>Products</h2>

@for (product of products(); track product.id) {
  <app-product-card
    [product]="product"
  />
}
```

Here, `[product]="product"` passes the current product object from the parent to the child.

`app/app.ts`

```ts
import { Component } from '@angular/core';
import { ProductListComponent } from './products/product-list/product-list';

@Component({
  selector: 'app-root',
  imports: [ProductListComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {}
```

Expected browser result:

![[products_components.png]]

The same `ProductCard` can be used in different places, such as a shopping cart, search results, or a product recommendation section.

## 3. Component responsibilities

A useful Angular design pattern separates components into two categories:

|Smart components|Presentational components|
|---|---|
|Load data from services|Receive data through inputs|
|Manage application state|Display information|
|Handle business interactions|Emit user events|
|Coordinate child components|Focus on reusable UI|

In our example:

- `ProductList` is the smart component. It manages the collection of products and will eventually retrieve them from Spring Boot.
- 
- `ProductCard` is the presentational component. It receives a product and displays it without knowing where the data came from.


Let's extend `ProductList` to load products from the Spring Boot API instead of using hardcoded data.

`products/product.service.ts`

```ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from './product';

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

Updated `product-list.ts`

```ts
import {
  Component,
  inject,
  OnInit,
  signal
} from '@angular/core';

import { ProductCard } from '../product-card/product-card';
import { ProductService } from '../product.service';
import { Product } from '../product';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [ProductCard],
  templateUrl: './product-list.html'
})
export class ProductList implements OnInit {

  private productService = inject(ProductService);

  products = signal<Product[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
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

Updated `product-list.html`

```html
<h2>Products</h2>

@if (loading()) {
  <p>Loading products...</p>
} @else if (error()) {
  <p>{{ error() }}</p>
} @else {
  @for (product of products(); track product.id) {
    <app-product-card [product]="product" />
  } @empty {
    <p>No products found.</p>
  }
}
```

This assumes `HttpClient` is already configured through `provideHttpClient()` and `/api/products` is forwarded to Spring Boot through your Angular development proxy.

```json
{
  "/api/**": {
    "target": "http://localhost:9091",
    "secure": false,
    "changeOrigin": true
  }
}
```

The resulting architecture looks like this:

![[components_architecture.png]]

In Java/Spring Boot terms, `ProductService` is conceptually similar to an API client service, while `ProductList` coordinates presentation state. `ProductCard` is a reusable view component, not a controller or backend service.

The smart/presentational distinction is a design guideline rather than a strict Angular rule. Small applications can combine responsibilities, but separating them becomes useful as the application grows.

### Key takeaways

- Angular components form a parent-child hierarchy.
- Parent components compose child components using their selectors.
- `input.required<T>()` defines a required, signal-based input.
- Smart components coordinate data and state; presentational components focus on displaying information.
- Reusable components reduce duplication and make applications easier to maintain.