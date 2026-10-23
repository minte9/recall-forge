Angular provides view queries to access child components, directives, and DOM elements declared in a component's template. This is useful when a parent needs to call a child's public method, focus an input element, or interact with a UI element that cannot be controlled through ordinary data binding.

Modern Angular provides the signal-based `viewChild()` and `viewChildren()` APIs. The traditional `@ViewChild` decorator is also supported, but we'll focus on the newer approach.

## 1. What is ViewChild

Normally, Angular components communicate through `input()` and `output()`. However, sometimes the parent needs direct access to a child component's methods.

For example, imagine a `ProductList` containing a search component. The parent might need to clear the search field when the user clicks Reset.

A view query allows the parent to obtain a reference to the child component instance and invoke its public methods.

`product-search.ts`

```ts
import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-product-search',
  standalone: true,
  template: `
    <input
      type="text"
      [value]="searchText()"
      (input)="onInput($event)"
      placeholder="Search products"
    />
  `
})
export class ProductSearch {

  searchText = signal('');

  onInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchText.set(input.value);
  }

  clear(): void {
    this.searchText.set('');
  }
}
```

The child exposes a public `clear()` method that resets its internal signal.

Now the parent can access it.

`product-list.ts`

```ts
import { Component, viewChild } from '@angular/core';
import { ProductSearch } from '../product-search/product-search';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [ProductSearch],
  template: `
    <app-product-search />

    <button (click)="resetSearch()">
      Reset Search
    </button>
  `
})
export class ProductList {

  search = viewChild(ProductSearch);

  resetSearch(): void {
    this.search()?.clear();
  }
}
```

The important expression is:

```ts
search = viewChild(ProductSearch);
```

Angular searches the parent's view for the first matching `ProductSearch` component and returns a signal containing its instance.

Because the child might not exist, the result can be `undefined`. Therefore, we use optional chaining:

```ts
this.search()?.clear();
```

If the child is guaranteed to exist, Angular also supports:

```ts
search = viewChild.required(ProductSearch);
```

This produces a required query. However, accessing it before Angular has resolved the query will cause an error.

## 2. Accessing DOM

View queries can also access native DOM elements using template reference variables and `ElementRef`.

For example, suppose we want to focus the product search field automatically when the user clicks a button.

`product-search.ts`

```ts
import {
  Component,
  ElementRef,
  viewChild
} from '@angular/core';

@Component({
  selector: 'app-product-search',
  standalone: true,
  template: `
    <input
      #searchInput
      type="text"
      placeholder="Search products"
    />

    <button (click)="focusSearch()">
      Focus Search
    </button>
  `
})
export class ProductSearch {

  searchInput =
    viewChild.required<ElementRef<HTMLInputElement>>(
      'searchInput'
    );

  focusSearch(): void {
    this.searchInput().nativeElement.focus();
  }
}
```

Here, `#searchInput` creates a template reference variable that Angular can use to identify the element.

The query returns an `ElementRef`, whose `nativeElement` property points to the underlying HTML input element.

In a real Angular application, the `focus()` method moves keyboard focus to the input. The interactive illustration above represents the behavior without directly manipulating the browser DOM.

### Important:

Direct DOM access should be used sparingly. For ordinary changes to text, classes, attributes, and visibility, prefer Angular's template bindings and signals.

## 3. Accessing childs

Angular also provides `viewChildren()` to retrieve multiple matching components from the parent's template.

Suppose our ProductList contains several ProductCard components and we want to access all their instances.

`product-card.ts`

```ts
import { Component, input } from '@angular/core';
import { Product } from '../product';

@Component({
  selector: 'app-product-card',
  standalone: true,
  template: `
    <h3>{{ product().name }}</h3>
  `
})
export class ProductCard {

  product = input.required<Product>();

  highlight(): void {
    console.log('Highlight:', this.product().name);
  }
}
```

`product-list.ts`

```ts
import {
  Component,
  viewChildren,
  signal
} from '@angular/core';

import { ProductCard } from '../product-card/product-card';
import { Product } from '../product';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [ProductCard],
  template: `
    @for (product of products(); track product.id) {
      <app-product-card [product]="product" />
    }

    <button (click)="highlightAll()">
      Highlight All
    </button>
  `
})
export class ProductList {

  products = signal<Product[]>([
    { id: 1, name: 'Laptop', price: 1200 },
    { id: 2, name: 'Monitor', price: 300 }
  ]);

  cards = viewChildren(ProductCard);

  highlightAll(): void {
    this.cards().forEach(card => card.highlight());
  }
}
```

Unlike `viewChild()`, which returns one matching component, `viewChildren()` returns a signal containing an array of matching components.

|API|Returns|
|---|---|
|`viewChild()`|First matching element or component, possibly `undefined`|
|`viewChild.required()`|One required matching element or component|
|`viewChildren()`|Array of matching elements or components|

Angular automatically updates the query results when components are added or removed from the view.

In this example, `highlight()` only logs a message. A real highlighting implementation would need to update a signal or CSS class in `ProductCard`.