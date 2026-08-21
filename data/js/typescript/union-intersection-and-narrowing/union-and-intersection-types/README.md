## JS / TypeScript / Union, Intersection & Narrowing / Union & Intersection Types

### 1. Union Types

A union (`A | B`) means a value could be either type; TypeScript only
allows operations valid on every member of the union until the type is
narrowed.

~~~typescript
function printId(id: string | number) {
  console.log(id.toString());  // toString exists on both
}
~~~


### 2. Intersection Types

An intersection (`A & B`) combines multiple types into one that must
satisfy all of them simultaneously — commonly used to merge separate
trait-like object types.

~~~typescript
type Named = { name: string };
type Aged = { age: number };
type Person = Named & Aged;   // must have both name and age
~~~


### 3. Discriminated Unions

A shared literal property (like `kind`) across union members lets
TypeScript narrow to the exact variant after checking that property,
enabling exhaustive, type-safe branching.

~~~typescript
type Shape =
  | { kind: "circle"; radius: number }
  | { kind: "square"; side: number };

function area(s: Shape) {
  if (s.kind === "circle") return Math.PI * s.radius ** 2;
  return s.side ** 2;
}
~~~
