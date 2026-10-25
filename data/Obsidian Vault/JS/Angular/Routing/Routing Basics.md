## 1. Configuring routes

Angular uses the Angular Router to navigate between views without reloading the entire page. In a Single Page Application (SPA), the browser initially loads the Angular application, and the router determines which component should be displayed based on the URL.

Routes are typically defined in `app.routes.ts`, where each URL path maps to an Angular component. For example, we'll build a small product application with three pages: Home, Products, and About.

First, create the components:

```sh
ng generate component pages/home
ng generate component pages/products
ng generate component pages/about
```

Define the routes in `src/app/app.routes.ts`:

```ts
import { Routes } from '@angular/router';

import { Home } from './pages/home/home';
import { Products } from './pages/products/products';
import { About } from './pages/about/about';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'products', component: Products },
  { path: 'about', component: About },
  { path: '**', redirectTo: '' }
];
```

The `**` wildcard handles unknown URLs. Angular evaluates routes in order, so the wildcard should come last.

Next, register the router in `app.config.ts`:

```ts
import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes)
  ]
};
```

## 2. RouterOutlet

`RouterOutlet` is a placeholder where Angular renders the component associated with the current route. It allows the application to keep shared elements, such as a navigation bar, while changing only the main content.

In `app.ts`:

```ts
import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  templateUrl: './app.html'
})
export class App {}
```

In `app.html`:

```html
<h1>Product Management</h1>

<nav>
  <a routerLink="/">Home</a> |
  <a routerLink="/products">Products</a> |
  <a routerLink="/about">About</a>
</nav>

<hr>

<router-outlet />
```

When the user navigates to `/products`, Angular renders the `Products` component inside `<router-outlet>`.

The header and navigation remain visible while the routed component changes.

## 3. RouterLink

`RouterLink` is Angular's declarative navigation directive. Instead of using standard HTML navigation with `href`, it tells Angular Router to change the active route without performing a full-page reload.

For example, the Products component can contain links to other pages:

`pages/products/products.html`:

```html
<h2>Products</h2>

<p>Available products:</p>

<ul>
  <li>Laptop - $1200</li>
  <li>Monitor - $300</li>
</ul>

<a routerLink="/">Back to Home</a>
```

Import `RouterLink` in the component:

```ts
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './products.html'
})
export class Products {}
```

## 4. Programmatic navigation

Sometimes navigation must happen from TypeScript code rather than from an HTML link. For example, after saving a product, you may want to redirect the user to the product list.

Angular provides the `Router` service, which can be injected using `inject()`.

```ts
import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  template: `
    <h2>Home</h2>

    <button (click)="goToProducts()">
      View Products
    </button>
  `
})
export class Home {

  private router = inject(Router);

  goToProducts(): void {
    this.router.navigate(['/products']);
  }
}
```

There are two common navigation methods:

```ts
// Navigate using URL segments
this.router.navigate(['/products']);

// Navigate using an absolute URL string
this.router.navigateByUrl('/products');
```

`navigate()` is especially useful when constructing URLs from parameters:

```ts
const productId = 10;

this.router.navigate(['/products', productId]);
// Navigates to /products/10
```

## 5. Angular vs Spring Boot

In an Angular + Spring Boot application, routing happens at two different levels.

Angular Router handles frontend URLs such as `/products` and `/about`. Spring Boot handles backend endpoints such as `/api/products`. The Angular component can call the Spring Boot API using `HttpClient`, but changing frontend routes does not automatically call the backend.

For example:

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public List<Product> getProducts() {
        return List.of(
            new Product(1, "Laptop", 1200),
            new Product(2, "Monitor", 300)
        );
    }
}
```

Angular calls `/api/products` through its configured HTTP client and proxy, while the router manages the visible page. In production, the web server must also be configured to serve Angular's `index.html` for frontend routes when users refresh or open a deep link directly, without intercepting `/api/**` requests.