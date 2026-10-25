## 1. Path parameters

Path parameters allow Angular routes to contain dynamic values. For example, instead of creating a separate route for every product, you can define `/products/:id`, where `:id` represents a product identifier. Angular extracts this value from the URL and makes it available to the component.

This is similar to Spring Boot's `@PathVariable`. The main difference is that Angular uses the parameter to determine which component or data to display, while Spring Boot uses it to process an HTTP request.

Define the routes in `app.routes.ts`:

```ts
import { Routes } from '@angular/router';

import { Products } from './pages/products/products';
import { ProductDetail } from './pages/product-detail/product-detail';

export const routes: Routes = [
  { path: 'products', component: Products },
  { path: 'products/:id', component: ProductDetail },
  { path: '**', redirectTo: 'products' }
];
```

Generate the detail component:

```sh
ng generate component pages/product-detail
```

Now Angular recognizes URLs such as:

```txt
/products/1
/products/2
/products/100
```

## 2. Route information

Angular provides the `ActivatedRoute` service to access information about the currently active route, including path parameters, query parameters, and other route metadata.

There are two common ways to read parameters: `snapshot` and reactive observables. `snapshot` reads the parameter at a specific moment, while observables allow the component to react when parameters change without the component being recreated.

For simple cases, use `snapshot`:

```ts
import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  template: `
    <h2>Product Details</h2>
    <p>Product ID: {{ productId }}</p>
  `
})
export class ProductDetail {

  private route = inject(ActivatedRoute);

  productId = Number(
    this.route.snapshot.paramMap.get('id')
  );
}
```

However, consider navigating from `/products/1` to `/products/2` while Angular reuses the same component. The `snapshot` value captured during initialization will not automatically update.

For reactive parameter handling, use `paramMap` with `toSignal()`:

```ts
import { Component, computed, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  template: `
    <h2>Product Details</h2>
    <p>Product ID: {{ productId() }}</p>
  `
})
export class ProductDetail {

  private route = inject(ActivatedRoute);

  private params = toSignal(
    this.route.paramMap.pipe(
      map(params => params.get('id'))
    ),
    { initialValue: this.route.snapshot.paramMap.get('id') }
  );

  productId = computed(() => Number(this.params()));
}
```

Here, `productId` is a computed signal. Whenever the route parameter changes, the displayed value updates automatically.

## 3. Query parameters

Query parameters provide additional information without changing the route structure. They are commonly used for filtering, searching, sorting, and pagination.

For example:

```txt
/products?category=electronics
/products?category=electronics&page=2
/products?sort=price&direction=asc
```

Unlike path parameters, query parameters are not declared in `app.routes.ts`.

You can navigate with query parameters programmatically:

```ts
import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-products',
  standalone: true,
  template: `
    <button (click)="filterProducts()">
      Electronics
    </button>
  `
})
export class Products {

  private router = inject(Router);

  filterProducts(): void {
    this.router.navigate(['/products'], {
      queryParams: {
        category: 'electronics',
        page: 1
      }
    });
  }
}
```

The resulting URL is:

```txt
/products?category=electronics&page=1
```
