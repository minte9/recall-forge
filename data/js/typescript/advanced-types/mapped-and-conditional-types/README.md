## JS / TypeScript / Advanced Types / Mapped & Conditional Types

### 1. Mapped Types

A mapped type transforms every property of an existing type using
`[K in keyof T]`, the mechanism `Partial`/`Readonly`/etc. are built
from internally.

~~~typescript
type ReadonlyUser = { readonly [K in keyof User]: User[K] };
~~~


### 2. Conditional Types

`T extends U ? X : Y` at the type level picks between two types based
on whether one is assignable to another — type-level branching,
evaluated at compile time.

~~~typescript
type IsString<T> = T extends string ? true : false;
type A = IsString<"hi">;   // true
type B = IsString<42>;      // false
~~~


### 3. infer

`infer` inside a conditional type extracts and names a nested type for
reuse — how built-in utility types like `ReturnType<T>` pull out a
function's return type.

~~~typescript
type ReturnOf<T> = T extends (...a: any[]) => infer R ? R : never;
type R = ReturnOf<() => string>;   // string
~~~
