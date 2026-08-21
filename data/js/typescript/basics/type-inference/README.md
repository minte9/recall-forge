## JS / TypeScript / Basics / Type Inference

### 1. How Inference Works

TypeScript infers a variable's type from its initializer, so explicit
annotations aren't always needed — `let x = 5` is inferred as `number`
without writing `: number`.

~~~typescript
let x = 5;        // inferred: number
let name = "Ana";  // inferred: string
~~~


### 2. Contextual Typing

TypeScript infers a callback's parameter types from the expected
function signature it's assigned to — no annotation needed inside
`.map()`'s callback, for example.

~~~typescript
[1, 2, 3].map((n) => n * 2);
// `n` is inferred as number from Array<number>.map's signature
~~~


### 3. Best Common Type

When inferring an array's type from mixed literals, TypeScript picks the
most specific type that covers every element — a union if there's no
single common type.

~~~typescript
let arr = [1, "two", 3];
// inferred as (string | number)[]
~~~
