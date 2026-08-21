## JS / TypeScript / Classes in TypeScript / Class Members & Modifiers

### 1. public, private, protected

`public` (the default) is accessible anywhere; `private` only within the
declaring class; `protected` within the class and its subclasses —
enforced at compile time, unlike JS's runtime-only `#private`.

~~~typescript
class Account {
  public id: string;
  private pin: string;
  protected balance: number;
}
~~~


### 2. readonly Properties

A `readonly` class property can only be assigned in the constructor (or
at declaration) — reassigning it anywhere else is a compile-time error.

~~~typescript
class User {
  readonly id: string;
  constructor(id: string) { this.id = id; }
}
~~~


### 3. Parameter Properties

Prefixing a constructor parameter with `public`/`private`/`readonly`
declares and assigns the class property in one step, removing separate
field declarations.

~~~typescript
class Point {
  constructor(public x: number, public y: number) {}
}
// equivalent to declaring x/y and assigning them manually
~~~
