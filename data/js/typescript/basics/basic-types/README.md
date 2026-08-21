## JS / TypeScript / Basics / Basic Types

### 1. Primitives & Arrays

`string`, `number`, `boolean` are the core primitive types; arrays are
typed as `Type[]` or `Array<Type>`, both mean the same thing.

~~~typescript
let names: string[] = ["Ana", "Bob"];
let scores: Array<number> = [90, 85];
~~~


### 2. Tuples

A tuple is a fixed-length array where each position has its own
specific type — useful for representing a pair or fixed structure like
`[string, number]`.

~~~typescript
let point: [number, number] = [10, 20];
let entry: [string, number] = ["age", 30];
~~~


### 3. Object Types

An inline object type describes the shape a value must have — every
listed property is required unless marked with `?`, and extra
properties are usually flagged.

~~~typescript
let user: { name: string; age?: number } = { name: "Ana" };
~~~
