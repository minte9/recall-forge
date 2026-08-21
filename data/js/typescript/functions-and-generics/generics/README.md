## JS / TypeScript / Functions & Generics / Generics

### 1. Generic Functions

A generic function captures a type parameter (`<T>`) so the same
function works across many types while preserving the relationship
between its input and output types.

~~~typescript
function identity<T>(value: T): T {
  return value;
}
identity<string>("hi");   // T inferred as string
~~~


### 2. Generic Constraints

`extends` on a type parameter restricts which types can be used,
letting the function safely access properties that only constrained
types are guaranteed to have.

~~~typescript
function getLength<T extends { length: number }>(v: T): number {
  return v.length;
}
~~~


### 3. Default Generic Parameters

A generic parameter can declare a default type, used whenever the
caller doesn't explicitly specify one — mirrors default function
parameters, but for types.

~~~typescript
interface Box<T = string> { value: T; }
let b: Box = { value: "hi" };   // T defaults to string
~~~
