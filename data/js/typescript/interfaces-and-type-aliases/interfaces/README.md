# JS / TypeScript / Interfaces & Type Aliases / Interfaces

## 1. Declaring an Interface

`interface` names a reusable object shape; any object assigned to that
type must satisfy every required property, checked structurally, not by
name.

```typescript
interface User {
  name: string;
  age: number;
}
const user: User = { name: "Ana", age: 30 };
```


## 2. Optional & Readonly Properties

`?` marks a property as optional; `readonly` prevents reassignment
after the object is created, catching accidental mutation of values
meant to stay fixed.

```typescript
interface Config {
  readonly id: string;
  timeout?: number;
}
```


## 3. Extending Interfaces

`extends` lets one interface build on another, inheriting its
properties — useful for modeling a hierarchy like a base `Entity`
shared across several specific shapes.

```typescript
interface Entity { id: string; }
interface User extends Entity { name: string; }
```
