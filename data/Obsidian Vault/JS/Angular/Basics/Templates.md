## 1. Interpolation— {{ }}

Interpolation allows Angular to display values from a component's TypeScript class inside an HTML template. It uses double curly braces, `{{ expression }}`, and automatically updates the displayed value when Angular detects a change in the underlying state.

Interpolation supports property access, arithmetic, conditional expressions, method calls, and signal reads. It is primarily intended for displaying text content, not for setting DOM properties or handling user events.

### Example: Product information

```ts
import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-product-info',
  template: `
    <h2>{{ name() }}</h2>
    <p>Price: {{ price() }} EUR</p>
    <p>Discounted: {{ price() * 0.9 }} EUR</p>
  `
})
export class ProductInfo {
  name = signal('Laptop');
  price = signal(1200);
}
```

Here, `name()` and `price()` read the current signal values. Angular tracks these signal reads and updates the template when the values change.

In our existing `product-card.html`, we already use interpolation:

```html
<h3>{{ product().name }}</h3>
<p>Price: {{ product().price }} EUR</p>
<p>{{ product().description ?? 'No description' }}</p>
```

## 2. Property Binding— [property]

Property binding allows a component expression to control a property of an HTML element or another Angular component. It uses square brackets, such as `[disabled]`, `[src]`, or `[product]`. Unlike interpolation, which displays text, property binding assigns values directly to DOM or component properties.

This is useful when an element's behavior or appearance depends on component state. For example, a button can be disabled when a product is unavailable, or a CSS class can change when a product is selected.

### Example: Dynamic button state

```ts
import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-product-action',
  template: `
    <button [disabled]="!available()">
      Buy Product
    </button>

    <p>Available: {{ available() }}</p>
  `
})
export class ProductAction {
  available = signal(false);
}
```

The button is disabled because `available()` returns `false`.

### Common property bindings

|Binding|Purpose|
|---|---|
|`[disabled]="condition"`|Disable a button or input|
|`[value]="expression"`|Set an input value|
|`[src]="imageUrl"`|Set an image source|
|`[class.selected]="condition"`|Add or remove a CSS class|
|`[style.color]="color"`|Set an inline CSS property|
|`[product]="product"`|Pass data to a child component|

### Example: Selected product styling

In our existing `ProductCard`, we can introduce a new input:

```ts
selected = input(false);
```

Then update the template:

```html
<div class="product-card"
     [class.selected]="selected()">

  <h3>{{ product().name }}</h3>

  <button [disabled]="selected()">
    {{ selected() ? 'Selected' : 'Select Product' }}
  </button>
</div>
```

And the CSS:

```css
.product-card.selected {
  border-color: #2563eb;
  background: #eff6ff;
}
```

The parent passes the selection state using:

```html
<app-product-card
  [product]="product"
  [selected]="selectedProductId() === product.id"
/>
```

This is the same principle as passing props to a Vue component.

## 3. Event Binding — (event)

Event binding allows Angular components to respond to user interactions, such as clicks, typing, form submissions, and keyboard events. It uses parentheses around the event name, for example `(click)`, `(input)`, or `(submit)`.

When the event occurs, Angular evaluates the associated expression. The special variable `$event` provides access to the event object or, for component outputs, the emitted value.

### Example: Button click

```ts
import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-counter',
  template: `
    <p>Count: {{ count() }}</p>

    <button (click)="increment()">
      Increment
    </button>
  `
})
export class Counter {
  count = signal(0);

  increment(): void {
    this.count.update(value => value + 1);
  }
}
```

Notice the difference between `.set()` and `.update()`:

```ts
this.count.set(10);                 // Replace value
this.count.update(value => value + 1); // Derive new value
```

### Example: Child-to-parent events

```ts
selected = output<number>();

selectProduct(): void {
  this.selected.emit(this.product().id);
}
```

The parent listens for the event:

```html
<app-product-card
  [product]="product"
  (selected)="onProductSelected($event)"
/>
```

Here, `$event` contains the product ID emitted by the child component, not a browser `MouseEvent`.

## 4. Two-Way Binding — [(ngModel)]

Two-way binding synchronizes a value between an input element and the component's state. When the component value changes, the input updates. When the user types into the input, the component value changes too.

Angular uses the syntax `[(ngModel)]`, sometimes called banana-in-a-box, because it combines property binding `[ ]` and event binding `( )`.

To use `ngModel`, the component must import `FormsModule` from `@angular/forms`.

### Example: Product search input

```ts
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-product-search',
  imports: [FormsModule],
  template: `
    <input
      type="text"
      [(ngModel)]="searchText"
      placeholder="Search products"
    />

    <p>Searching for: {{ searchText }}</p>
  `
})
export class ProductSearch {
  searchText = '';
}
```

Typing into the input automatically updates `searchText`, and changes to `searchText` from the component update the input.

### Two-way binding with signals

Angular also supports writable signals with `ngModel`:

```ts
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-product-search',
  imports: [FormsModule],
  template: `
    <input
      [(ngModel)]="searchText"
      placeholder="Search products"
    />

    <p>Search: {{ searchText() }}</p>
  `
})
export class ProductSearch {
  searchText = signal('');
}
```

	Notice that `[(ngModel)]` receives the writable signal `searchText` without parentheses, while interpolation reads it using `searchText()`.

## 5. Complete Example — Product Search and Selection

Now let's apply all four binding concepts to your existing application.

We'll add a search field to filter the products returned by Spring Boot and visually highlight the selected product.

The backend remains unchanged. Filtering happens in Angular after the products are retrieved.

### Project structure

```
src/app/
├── products/
│   ├── product.model.ts
│   ├── product.service.ts
│   ├── product-list.ts
│   ├── product-list.html
│   ├── product-list.css
│   ├── product-card.ts
│   ├── product-card.html
│   └── product-card.css
├── app.ts
└── app.config.ts
```

### Step 1: Update `product-list.ts`

We'll use `signal()` for mutable state and `computed()` for derived state.

`computed()` is useful because we don't need to manually update the filtered list every time the search text changes.

```ts
import {
  Component,
  inject,
  OnInit,
  signal,
  computed
} from '@angular/core';

import { FormsModule } from '@angular/forms';

import { ProductService } from './product.service';
import { Product } from './product.model';
import { ProductCard } from './product-card';

@Component({
  selector: 'app-product-list',
  imports: [FormsModule, ProductCard],
  templateUrl: './product-list.html',
  styleUrl: './product-list.css'
})
export class ProductListComponent implements OnInit {

  private readonly productService = inject(ProductService);

  products = signal<Product[]>([]);
  searchText = signal('');
  selectedProductId = signal<number | null>(null);

  filteredProducts = computed(() => {
    const search = this.searchText().toLowerCase().trim();

    return this.products().filter(product =>
      product.name.toLowerCase().includes(search)
    );
  });

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

  clearSearch(): void {
    this.searchText.set('');
  }
}
```

`filteredProducts` is a computed signal. Angular recalculates it whenever `products()` or `searchText()` changes.

### Step 2: Create `product-list.html`

```html
<h2>Products</h2>

<div class="search-bar">
  <input
    type="text"
    [(ngModel)]="searchText"
    placeholder="Search products..."
  />

  <button (click)="clearSearch()">
    Clear
  </button>
</div>

<p>
  Showing {{ filteredProducts().length }}
  of {{ products().length }} products
</p>

<div class="product-grid">
  @for (product of filteredProducts(); track product.id) {
    <app-product-card
      [product]="product"
      [selected]="selectedProductId() === product.id"
      (selectedProduct)="onProductSelected($event)"
    />
  } @empty {
    <p>No matching products found.</p>
  }
</div>

@if (selectedProductId() !== null) {
  <p class="selection">
    Selected product ID: {{ selectedProductId() }}
  </p>
}
```

Here we use all four binding techniques:

- Interpolation: `{{ filteredProducts().length }}`
- Property binding: `[product]` and `[selected]`
- Event binding: `(click)` and `(selectedProduct)`
- Two-way binding: `[(ngModel)]="searchText"`
    
### Step 3: Create `product-list.css`

```css
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.search-bar input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
}

.search-bar button {
  padding: 10px 16px;
  cursor: pointer;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(
    auto-fit,
    minmax(220px, 1fr)
  );
  gap: 16px;
  margin-top: 16px;
}

.selection {
  margin-top: 20px;
  font-weight: 600;
  color: #2563eb;
}
```

### Step 4: Update `product-card.ts`

We'll use the output name `selectedProduct` to make it distinct from the `selected` input.

```ts
import {
  Component,
  input,
  output
} from '@angular/core';

import { Product } from './product.model';

@Component({
  selector: 'app-product-card',
  templateUrl: './product-card.html',
  styleUrl: './product-card.css'
})
export class ProductCard {

  product = input.required<Product>();

  selected = input(false);

  selectedProduct = output<number>();

  selectProduct(): void {
    this.selectedProduct.emit(this.product().id);
  }
}
```

### Step 5: Update `product-card.html`

```html
<div class="product-card"
     [class.selected]="selected()">

  <h3>{{ product().name }}</h3>

  <p>Price: {{ product().price }} EUR</p>

  <p>
    {{ product().description ?? 'No description' }}
  </p>

  <button
    [disabled]="selected()"
    (click)="selectProduct()">

    {{ selected() ? 'Selected' : 'Select Product' }}
  </button>
</div>
```

### Step 6: Update `product-card.css`

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

### Interactive preview

Try searching for a product and selecting it. This is the behavior your Angular application should implement.

![[product_management_search.png]]


## 6. Key Concepts to Remember

|Concept|Syntax|Purpose|
|---|---|---|
|Interpolation|`{{ value }}`|Display values|
|Property binding|`[disabled]="condition"`|Set element or component properties|
|Event binding|`(click)="method()"`|Respond to events|
|Two-way binding|`[(ngModel)]="value"`|Synchronize input and state|

There are also three important Angular concepts introduced in the complete example.

`signal()` stores reactive state:

```ts
searchText = signal('');
```

`computed()` derives a value from other signals:

```ts
filteredProducts = computed(() =>
  this.products().filter(product =>
    product.name.includes(this.searchText())
  )
);
```

`FormsModule` provides template-driven form directives, including `ngModel`.