# JS / TypeScript / Interfaces & Type Aliases / Type Aliases

## 1. Declaring a Type Alias

`type` gives a name to any type, not just object shapes — primitives,
unions, tuples, and function signatures can all be aliased for reuse and
readability.

```typescript
type ID = string | number;
type Point = { x: number; y: number };
```


## 2. Interface vs Type Alias

Interfaces can be reopened and merged across declarations; type aliases
cannot. Type aliases can express unions and primitives directly, which
interfaces cannot.

```typescript
interface A { x: number; }
interface A { y: number; }  // merges: A has x and y
// type A = {x:number}; type A = {y:number}; // Error: duplicate
```


## 3. Aliasing Union/Function Types

Type aliases are the natural way to name a union or a function
signature that gets reused across a codebase, avoiding repetition.

```typescript
type Status = "active" | "closed" | "pending";
type Comparator = (a: number, b: number) => number;
```
