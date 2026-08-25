# JS / TypeScript / Functions & Generics / Typing Functions

## 1. Function Types

A function's type signature describes its parameters and return type
together, usable as a variable's type — `(a: number) => number` matches
any function fitting that shape.

```typescript
let double: (n: number) => number = (n) => n * 2;
```


## 2. Optional & Default Parameters

`param?: Type` makes a parameter optional; a default value
(`param: Type = value`) makes it optional too and supplies a fallback
when omitted or `undefined`.

```typescript
function greet(name: string, greeting: string = "Hi"): string {
  return `${greeting}, ${name}`;
}
```


## 3. Function Overloads

Multiple signature declarations above a single implementation let a
function report different, more precise return types depending on how
it was called.

```typescript
function parse(x: string): number;
function parse(x: number): string;
function parse(x: any): any { /* ... */ }
```
