## 1. Creating Angular Components

A component is the fundamental building block of an Angular application. Each component represents a reusable part of the user interface, such as a product list, navigation bar, login form, or dashboard. A component combines a TypeScript class containing its logic, an HTML template defining its appearance, and optional CSS styles.

Angular applications are organized as a tree of components. The root component (`App`) contains other components, which can contain additional child components. This architecture makes it easier to maintain large applications because each component has a specific responsibility.

For example, our Angular + Spring Boot application could be organized as follows:

![[product_card.png]]

### Generate a component with Angular CLI

From the root of your `angular-demo` project:

```sh
ng generate component products/product-card
```

Or use the shorthand:

```sh
ng g c products/product-card
```

Angular CLI generates files similar to:

```txt
src/app/products/product-card/
├── product-card.ts
├── product-card.html
├── product-card.css
└── product-card.spec.ts
```

The `.spec.ts` file contains unit tests. You can skip generating it for a learning example:

```sh
ng g c products/product-card --skip-tests
```

Modern Angular components are standalone by default. They can directly import other components, directives, and pipes without requiring an `NgModule`.

### Example: Product Card

product-card.ts

```ts
import { Component } from '@angular/core';

@Component({
  selector: 'app-product-card',
  templateUrl: './product-card.html',
  styleUrl: './product-card.css'
})
export class ProductCard {
  name = 'Laptop';
  price = 1200;
}
```

product-card.html

```html
<div class="product-card">
  <h3>{{ name }}</h3>
  <p>Price: {{ price }} EUR</p>
</div>
```

product-card.css

```css
.product-card {
  padding: 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  margin-bottom: 12px;
}
```

To display this component inside another standalone component, import it:

app.ts

```ts
import { Component } from '@angular/core';
import { ProductCard } from './products/product-card/product-card';

@Component({
  selector: 'app-root',
  imports: [ProductCard],
  template: `
    <h1>Product Management</h1>
    <app-product-card />
  `
})
export class App {}
```

Angular renders the `ProductCard` component wherever it encounters `<app-product-card />`.

```sh
ng serve --port 4201 --proxy-config proxy.conf.json 
```

Result (in browser):

![[product_card_browser.png]]

HTML (view source):

```html
<!doctype html>
<html lang="en">
<head>
  <script type="module" src="/@vite/client"></script>

  <meta charset="utf-8">
  <title>AngularDemo</title>
  <base href="/">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="icon" type="image/x-icon" href="favicon.ico">
<link rel="stylesheet" href="styles.css"></head>
<body>
  <app-root></app-root>
<script src="main.js" type="module"></script></body>
</html>
```

## 2. Component Metadata

The `@Component` decorator tells Angular how a TypeScript class should be used as a UI component. The object passed to the decorator contains metadata, including the component's selector, template, styles, and imported dependencies.

This is conceptually similar to annotations in Spring Boot, such as `@RestController`, `@Service`, or `@Component`. However, Angular's `@Component` configures a frontend UI element, while Spring's annotations register and configure backend classes.

### Example: Component metadata

```ts
import { Component } from '@angular/core';

@Component({
  selector: 'app-product',
  standalone: true,
  templateUrl: './product.html',
  styleUrl: './product.css',
  imports: []
})
export class ProductComponent {
  name = 'Laptop';
}
```

### Important metadata properties

|Property|Purpose|
|---|---|
|`selector`|HTML element used to render the component|
|`template`|Inline HTML template|
|`templateUrl`|External HTML template file|
|`styles`|Inline CSS styles|
|`styleUrl`|External CSS stylesheet|
|`imports`|Dependencies used by the template|
|`standalone`|Indicates standalone component architecture|
|`changeDetection`|Configures component change detection|
|`providers`|Defines component-scoped dependency providers|

For modern Angular, `standalone: true` is the default, so it can usually be omitted.

### Angular vs Spring Boot annotations

Spring Boot:

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public List<Product> getProducts() {
        return List.of();
    }
}
```

Angular:

```ts
@Component({
  selector: 'app-products',
  template: `<h2>Products</h2>`
})
export class ProductsComponent {}
```

Both use metadata to describe how the framework should treat a class.

The difference is that Spring Boot maps HTTP requests to controller methods, while Angular maps UI elements to component instances.

## 3. Templates

Angular supports two ways of defining templates and styles: inline, directly inside the `@Component` decorator, or in separate HTML and CSS files. Inline templates are convenient for small components, while separate files are generally easier to maintain for complex interfaces.

Components can also communicate through inputs and outputs. Inputs allow parent components to pass data to children, while outputs allow children to notify parents about events. Modern Angular provides `input()` and `output()` APIs, which work naturally with signals and TypeScript.

Let's improve our existing product list by extracting the product display into a reusable `ProductCard` component.

src/app/products/product-card/`product-card.ts`

```ts
import {
  Component,
  input,
  output
} from '@angular/core';

import { Product } from '../product.model';

@Component({
  selector: 'app-product-card',
  templateUrl: './product-card.html',
  styleUrl: './product-card.css'
})
export class ProductCard {

  product = input.required<Product>();

  selected = output<number>();

  selectProduct(): void {
    this.selected.emit(this.product().id);
  }
}
```

Notice two important APIs:

- `input.required<Product>()` declares a required input, allowing the parent component to pass a `Product` object.
- `output<number>()` declares an event that emits a product ID to the parent component.
    
The `product` input is a signal, so we access its value using `product()`.

src/app/products/product-card/`product-card.html`

```html
<div class="product-card">
  <h3>{{ product().name }}</h3>

  <p>Price: {{ product().price }} EUR</p>

  <p>
    {{ product().description ?? 'No description' }}
  </p>

  <button (click)="selectProduct()">
    Select Product
  </button>
</div>
```

src/app/products/product-card/`product-card.css`

```css
.product-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  background: #fff;
}

.product-card h3 {
  margin-top: 0;
}

.product-card button {
  padding: 8px 12px;
  cursor: pointer;
}
```

By default, Angular uses emulated style encapsulation, which scopes these styles to the component's template.

src/app/products/`product-list.ts`

```ts
import {
  Component,
  inject,
  OnInit,
  signal
} from '@angular/core';

import { ProductService } from './product.service';
import { Product } from './product.model';
import { ProductCard } from './product-card/product-card';

@Component({
  selector: 'app-product-list',
  imports: [ProductCard],
  template: `
    <h2>Products</h2>

    @for (product of products(); track product.id) {
      <app-product-card
        [product]="product"
        (selected)="onProductSelected($event)"
      />
    } @empty {
      <p>No products available.</p>
    }

    @if (selectedProductId() !== null) {
      <p>
        Selected product ID: {{ selectedProductId() }}
      </p>
    }
  `
})
export class ProductListComponent implements OnInit {

  private readonly productService = inject(ProductService);

  products = signal<Product[]>([]);

  selectedProductId = signal<number | null>(null);

  ngOnInit(): void {
    this.productService.getProducts().subscribe({
      next: products => {
        this.products.set(products);
      },
      error: error => {
        console.error('Failed to load products', error);
      }
    });
  }

  onProductSelected(id: number): void {
    this.selectedProductId.set(id);
  }
}
```

The parent component passes each product to the child using property binding:

```html
<app-product-card [product]="product" />
```

The child notifies the parent using event binding:

```html
<app-product-card
  (selected)="onProductSelected($event)"
/>
```

This is the behavior you should see after connecting the components.

app/`app.ts`

```ts
import { Component } from '@angular/core';
import { ProductListComponent } from './products/product-list';

@Component({
  selector: 'app-root',
  imports: [ProductListComponent],
  template: `
    <h1>Product Management</h1>
    <app-product-list />
  `
})
export class App {}
```

Result:

![[product_card_search.png]]


## 4. Component Lifecycle Basics

Angular components have a lifecycle that begins when Angular creates the component and ends when it destroys it. During this lifecycle, Angular invokes specific methods called lifecycle hooks, allowing developers to execute logic at particular stages.

Lifecycle hooks are useful for initializing component data, reacting to input changes, interacting with rendered elements, and cleaning up resources. The most commonly encountered hooks are `ngOnInit()`, `ngOnChanges()`, and `ngOnDestroy()`.

### Main lifecycle hooks

|Hook|When it runs|Common use|
|---|---|---|
|`constructor`|Class instance creation|Basic initialization|
|`ngOnChanges`|When input values change|React to input updates|
|`ngOnInit`|Once after initial inputs are set|Load initial data|
|`ngAfterViewInit`|After the component view initializes|Access view elements|
|`ngOnDestroy`|Before the component is destroyed|Cleanup|

The constructor is a TypeScript class feature, not an Angular lifecycle hook.

### Example: `ngOnInit()`

In our product list:

```ts
import { Component, inject, OnInit } from '@angular/core';

@Component({
  selector: 'app-products',
  template: `<h2>Products</h2>`
})
export class ProductsComponent implements OnInit {

  constructor() {
    console.log('Constructor called');
  }

  ngOnInit(): void {
    console.log('Component initialized');
  }
}
```

When Angular creates this component, the console displays:

```txt
Constructor called
Component initialized
```

`ngOnInit()` is appropriate for loading initial data, although Angular also provides other patterns for managing asynchronous data.

### Example: `ngOnChanges()`

Suppose a child component receives a product ID from its parent.

```ts
import {
  Component,
  Input,
  OnChanges,
  SimpleChanges
} from '@angular/core';

@Component({
  selector: 'app-product-details',
  template: `<p>Product ID: {{ productId }}</p>`
})
export class ProductDetailsComponent implements OnChanges {

  @Input() productId = 0;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['productId']) {
      console.log('Product ID changed:', this.productId);
    }
  }
}
```

Angular calls `ngOnChanges()` when a data-bound input changes, including the initial input assignment.

This example uses the traditional `@Input()` API to demonstrate the hook. Modern signal inputs can instead be observed with `effect()` or `computed()` when appropriate.

### Example: `ngOnDestroy()`

Suppose a component starts a timer:

```ts
import {
  Component,
  OnInit,
  OnDestroy,
  signal
} from '@angular/core';

@Component({
  selector: 'app-timer',
  template: `
    <p>Seconds: {{ seconds() }}</p>
  `
})
export class TimerComponent implements OnInit, OnDestroy {

  seconds = signal(0);

  private timerId?: ReturnType<typeof setInterval>;

  ngOnInit(): void {
    this.timerId = setInterval(() => {
      this.seconds.update(value => value + 1);
    }, 1000);
  }

  ngOnDestroy(): void {
    if (this.timerId !== undefined) {
      clearInterval(this.timerId);
    }
  }
}
```

`ngOnDestroy()` prevents the timer from continuing after the component is removed.

### Modern alternative: `DestroyRef`

Angular also provides `DestroyRef`, which allows cleanup logic to be registered without implementing `OnDestroy`.

```ts
import {
  Component,
  DestroyRef,
  inject,
  signal
} from '@angular/core';

@Component({
  selector: 'app-timer',
  template: `<p>Seconds: {{ seconds() }}</p>`
})
export class TimerComponent {

  private readonly destroyRef = inject(DestroyRef);

  seconds = signal(0);

  constructor() {
    const timerId = setInterval(() => {
      this.seconds.update(value => value + 1);
    }, 1000);

    this.destroyRef.onDestroy(() => {
      clearInterval(timerId);
    });
  }
}
```

This is especially useful when cleanup needs to be registered close to the code that creates the resource.

## 5. Angular Components vs Java Classes

It helps to compare Angular components with Java classes.

| Concept              | Angular                | Java / Spring Boot               |
| -------------------- | ---------------------- | -------------------------------- |
| Class definition     | `class ProductList`    | `class ProductService`           |
| Metadata             | `@Component`           | `@Component`, `@Service`         |
| Dependency injection | `inject()`             | Constructor injection            |
| Initialization       | `ngOnInit()`           | `@PostConstruct` (rough analogy) |
| Cleanup              | `ngOnDestroy()`        | `@PreDestroy`                    |
| State                | Properties and signals | Fields                           |
| User interface       | HTML template          | Usually separate frontend        |

One important distinction is that Angular components are created and destroyed as the UI changes. Spring services are typically singleton beans that remain active throughout the application's lifetime.

This makes lifecycle management particularly important in Angular.

## 6. Complete Styled Example

We'll separate the HTML, CSS, and TypeScript for both components.

```txt
src/
├── styles.css
└── app/
    ├── app.ts
    ├── app.html
    ├── app.css
    │
    └── products/
        ├── product.model.ts
        ├── product.service.ts
        ├── product-list.ts
        ├── product-list.html
        ├── product-list.css
        ├── product-card.ts
        ├── product-card.html
        └── product-card.css
```

Your existing `product.model.ts` and `product.service.ts` don't need any changes.

`src/app/app.ts`

```ts
import { Component } from '@angular/core';
import { ProductListComponent } from './products/product-list';

@Component({
  selector: 'app-root',
  imports: [ProductListComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {}
```

`src/app/app.html

```html
<main class="app-container">
  <header class="app-header">
    <h1>Product Management</h1>
    <p>Angular + Spring Boot Demo</p>
  </header>

  <app-product-list />
</main>
```

`src/app/app.css`

```css
.app-container {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;
}

.app-header {
  margin-bottom: 32px;
}

.app-header h1 {
  font-size: 30px;
  font-weight: 700;
  margin: 0 0 8px;
}

.app-header p {
  color: #64748b;
  font-size: 15px;
  margin: 0;
}
```

The card will receive a `Product` from its parent, display its properties, and emit a selection event. We'll also introduce a `selected` input so the button can change appearance when its product is selected.

`src/app/products/product-card.ts`

```ts
import { Component, input, output } from '@angular/core';
import { Product } from './product.model';

@Component({
  selector: 'app-product-card',
  templateUrl: './product-card.html',
  styleUrl: './product-card.css'
})
export class ProductCard {

  product = input.required<Product>();

  isSelected = input<boolean>(false);

  selected = output<number>();

  selectProduct(): void {
    this.selected.emit(this.product().id);
  }
}
```

`src/app/products/product-card.html

```html
<div class="product-card" [class.is-selected]="isSelected()">

  <h3>{{ product().name }}</h3>

  <p class="price">
    Price: {{ product().price }} EUR
  </p>

  <p class="description">
    {{ product().description ?? 'No description' }}
  </p>

  <button
    type="button"
    [class.selected-button]="isSelected()"
    (click)="selectProduct()"
  >
    {{ isSelected() ? 'Selected' : 'Select Product' }}
  </button>

</div>
```

`src/app/products/product-card.css`

```css
.product-card {
  padding: 20px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #ffffff;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.product-card:hover {
  border-color: #94a3b8;
}

.product-card.is-selected {
  border-color: #2563eb;
  box-shadow: 0 0 0 1px #2563eb;
}

.product-card h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 14px;
  color: #0f172a;
}

.price {
  font-size: 14px;
  color: #334155;
  margin: 0 0 8px;
}

.description {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 18px;
}

button {
  padding: 9px 16px;
  border: 1px solid #2563eb;
  border-radius: 7px;
  background: #ffffff;
  color: #2563eb;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

button:hover {
  background: #eff6ff;
}

button.selected-button {
  background: #2563eb;
  color: #ffffff;
}

button.selected-button:hover {
  background: #1d4ed8;
}
```

The parent continues to retrieve products from Spring Boot and manage the selected product ID using Angular signals.

`src/app/products/product-list.ts`

```ts
import {
  Component,
  inject,
  OnInit,
  signal
} from '@angular/core';

import { ProductService } from './product.service';
import { Product } from './product.model';
import { ProductCard } from './product-card';

@Component({
  selector: 'app-product-list',
  imports: [ProductCard],
  templateUrl: './product-list.html',
  styleUrl: './product-list.css'
})
export class ProductListComponent implements OnInit {

  private readonly productService = inject(ProductService);

  products = signal<Product[]>([]);

  selectedProductId = signal<number | null>(null);

  ngOnInit(): void {
    this.productService.getProducts().subscribe({
      next: products => {
        this.products.set(products);
      },
      error: error => {
        console.error('Failed to load products:', error);
      }
    });
  }

  onProductSelected(id: number): void {
    this.selectedProductId.set(id);
  }
}
```

`src/app/products/product-list.html`

```html
<section class="products-section">

  <h2>Products</h2>

  <div class="product-list">
    @for (product of products(); track product.id) {
      <app-product-card
        [product]="product"
        [isSelected]="selectedProductId() === product.id"
        (selected)="onProductSelected($event)"
      />
    } @empty {
      <p>No products available.</p>
    }
  </div>

  @if (selectedProductId() !== null) {
    <p class="selection-message">
      Selected product ID: {{ selectedProductId() }}
    </p>
  }

</section>
```

`src/app/products/product-list.css`

```css
.products-section h2 {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 20px;
  color: #0f172a;
}

.product-list {
  display: grid;
  gap: 14px;
}

.selection-message {
  margin-top: 20px;
  padding: 12px 16px;
  background: #eff6ff;
  border-radius: 8px;
  color: #1d4ed8;
  font-size: 14px;
  font-weight: 500;
}
```

Angular CLI may generate some global styles in `src/styles.css`. For consistency, replace them with:

```css
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  background: #f8fafc;
  color: #0f172a;
  font-family: Arial, Helvetica, sans-serif;
  line-height: 1.5;
}

button {
  font-family: inherit;
}
```

The global stylesheet controls the page's overall appearance, while each component's stylesheet controls its own elements.

### Expected result

After saving your files, Angular should automatically refresh the application.

![[product_management_styled.png]]