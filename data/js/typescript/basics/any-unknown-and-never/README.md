## JS / TypeScript / Basics / any, unknown & never

### 1. any

`any` opts a value out of type checking entirely — assignments, property
access, and calls on it are never flagged, which defeats TypeScript's
purpose if overused.

~~~typescript
let value: any = 5;
value.toUpperCase();   // no error, even though numbers lack this
~~~


### 2. unknown

`unknown` accepts any value like `any`, but forbids using it until its
type is narrowed first — the type-safe alternative for values whose
type truly isn't known yet.

~~~typescript
let value: unknown = fetchData();
value.trim();          // Error: must narrow first
if (typeof value === "string") value.trim();  // OK
~~~


### 3. never

`never` represents a value that can never occur — the return type of a
function that always throws or loops forever, and useful for
exhaustiveness checks in a switch.

~~~typescript
function fail(msg: string): never {
  throw new Error(msg);
}
~~~
