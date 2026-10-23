## 1. Parent-Child communication

Angular components communicate primarily through inputs and outputs. Inputs allow a parent component to pass data to a child, while outputs allow the child to notify its parent when something happens.

Angular uses a unidirectional data flow: the parent passes data down through inputs, and the child sends events up through outputs.

For example, the parent passes a `Product` object to `ProductCard`. When the user clicks Add to Cart, the child emits an event containing that product. The parent receives the event and updates the cart.

This is similar to Java's event-driven programming: a component exposes an event, and another component registers a handler.

## 2. Component inputs

Angular's `input()` function declares a property that receives data from a parent component. Unlike traditional `@Input()`, it creates an input signal, meaning you read its value by calling it as a function.

`src/app/products/product.ts`

```ts
export interface Product {
  id: number;
  name: string;
  price: number;
  description?: string;
}
```

`product-card.ts`

```ts
import { Component, input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Product } from '../product';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './product-card.html'
})
export class ProductCard {

  product = input.required<Product>();

  showDescription = input(true);
}
```

Here, `product` is a required input, while `showDescription` is optional and defaults to `true`.

`product-card.html`

```html
<div class="product-card">
  <h3>{{ product().name }}</h3>

  @if (showDescription()) {
    <p>{{ product().description }}</p>
  }

  <strong>
    {{ product().price | currency:'EUR' }}
  </strong>
</div>
```

The parent can now configure the child component:

```html
<app-product-card
  [product]="product"
  [showDescription]="false"
/>
```

The square brackets indicate property binding. Angular evaluates the expression and passes the resulting value to the child.

There are two main ways to declare inputs in modern Angular:

|API|Description|
|---|---|
|`input<T>()`|Optional input, initially `undefined` unless a default is provided|
|`input.required<T>()`|Required input that the parent must provide|

Input signals are read-only from the child's perspective. The child should not call `.set()` on an input signal to modify parent-owned state.

## 3. Component outputs

Outputs allow a child component to emit custom events that its parent can handle. Angular's modern `output<T>()` API creates an event emitter with a strongly typed payload.

Let's add an Add to Cart button.

Updated `product-card.ts`

```ts
import { Component, input, output } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Product } from '../product';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './product-card.html'
})
export class ProductCard {

  product = input.required<Product>();

  addToCart = output<Product>();

  onAddToCart(): void {
    this.addToCart.emit(this.product());
  }
}
```

Updated `product-card.html`

```html
<div class="product-card">
  <h3>{{ product().name }}</h3>

  <p>{{ product().description }}</p>

  <strong>
    {{ product().price | currency:'EUR' }}
  </strong>

  <button (click)="onAddToCart()">
    Add to Cart
  </button>
</div>
```

When the button is clicked, `onAddToCart()` calls `.emit()` and sends the current product to the parent.

Notice the difference:

- `input.required<Product>()` receives a `Product`.
- `output<Product>()` emits a `Product`.
    
The output does not modify the parent's data directly. It simply notifies the parent that an action occurred.

## 4. Handling events in the parent

Now let's update `ProductList` to receive Add to Cart events and maintain the shopping cart.

`product-list.ts`

```ts
import { Component, signal, computed } from '@angular/core';
import { ProductCard } from '../product-card/product-card';
import { Product } from '../product';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [ProductCard],
  templateUrl: './product-list.html'
})
export class ProductList {

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

  cart = signal<Product[]>([]);

  cartCount = computed(() => this.cart().length);

  cartTotal = computed(() =>
    this.cart().reduce(
      (total, product) => total + product.price,
      0
    )
  );

  onAddToCart(product: Product): void {
    this.cart.update(items => [...items, product]);
  }
}
```

`product-list.html`

```html
<h2>Products</h2>

<p>Cart items: {{ cartCount() }}</p>

<p>Cart total: {{ cartTotal() }} EUR</p>

@for (product of products(); track product.id) {
  <app-product-card
    [product]="product"
    (addToCart)="onAddToCart($event)"
  />
}
```

The key expression is:

```ts
(addToCart)="onAddToCart($event)"
```

Here, `(addToCart)` listens for the custom event emitted by the child, while `$event` contains the `Product` object passed to `.emit()`.

The `cart.update()` method creates a new array containing the existing cart items plus the selected product. Because `cart` is a signal, Angular updates the cart count and total automatically.

## 5. Input/output vs two-way binding

Angular also supports two-way binding between components, but it is important to distinguish it from the input/output pattern.

|Feature|Purpose|
|---|---|
|`input()`|Parent passes data to child|
|`output()`|Child sends events to parent|
|`model()`|Parent and child share a two-way bindable value|

For example, Angular's `model()` API is useful for components such as counters, checkboxes, or custom form controls.

`quantity-selector.ts`

```ts
import { Component, model } from '@angular/core';

@Component({
  selector: 'app-quantity-selector',
  standalone: true,
  template: `
    <button (click)="decrease()">-</button>

    <span>{{ quantity() }}</span>

    <button (click)="increase()">+</button>
  `
})
export class QuantitySelector {

  quantity = model(1);

  increase(): void {
    this.quantity.update(value => value + 1);
  }

  decrease(): void {
    this.quantity.update(value =>
      Math.max(1, value - 1)
    );
  }
}
```

The parent can bind to it using:

```ts
<app-quantity-selector [(quantity)]="quantity" />
```

Assuming the parent defines:

```ts
quantity = signal(1);
```

Angular automatically synchronizes changes between the parent's `quantity` signal and the child's model input.

For a shopping cart, however, `output()` is generally more appropriate for an Add to Cart action, because it represents an event rather than a shared value.

### Key takeaways

- `input()` passes data from parent to child.
- `input.required<T>()` declares a mandatory input.
- `output<T>()` defines a custom event emitted by the child.
- `$event` contains the emitted event payload.
- `model()` supports two-way binding for shared values.
- Keeping state in the parent and presentation in the child makes components easier to reuse and test.