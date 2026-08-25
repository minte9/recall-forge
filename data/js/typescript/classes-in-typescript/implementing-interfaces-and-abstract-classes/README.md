# JS / TypeScript / Classes in TypeScript / Implementing Interfaces & Abstract Classes

## 1. implements

A class using `implements Interface` must provide every member the
interface declares; TypeScript checks this at compile time, though
`implements` has no effect at runtime.

```typescript
interface Shape { area(): number; }
class Circle implements Shape {
  constructor(private radius: number) {}
  area() { return Math.PI * this.radius ** 2; }
}
```


## 2. Abstract Classes

`abstract class` can't be instantiated directly and may declare
`abstract` methods with no body — subclasses must implement them,
providing shared state plus an enforced contract.

```typescript
abstract class Shape {
  abstract area(): number;
  describe() { return `Area: ${this.area()}`; }  // shared logic
}
```


## 3. Interfaces vs Abstract Classes

Interfaces describe shape only, with zero runtime footprint; abstract
classes can hold shared implementation and state, but a class can only
extend one abstract class versus implementing many interfaces.

```typescript
class Circle extends Shape implements Comparable {}
// one abstract base, multiple interfaces
```
