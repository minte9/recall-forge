## JS / TypeScript / Basics / Type Annotations

### 1. Annotating Variables

A `: Type` annotation after a variable name tells TypeScript what values
it can hold; TypeScript then rejects assignments that don't match at
compile time, before the code ever runs.

~~~typescript
let age: number = 30;
age = "old";   // Error: string is not assignable to number
~~~


### 2. Annotating Function Parameters

Parameters have no inferred type from usage, so TypeScript defaults
untyped parameters to `any` unless `noImplicitAny` is on — always
annotate parameters explicitly to catch call-site mistakes.

~~~typescript
function greet(name: string): void {
  console.log(`Hi, ${name}`);
}
greet(42);   // Error: number is not assignable to string
~~~


### 3. Return Type Annotations

Explicitly annotating a return type (`: number`) catches a function body
that doesn't actually return what it claims, even though TypeScript can
often infer it automatically.

~~~typescript
function add(a: number, b: number): number {
  return a + b;
}
~~~
