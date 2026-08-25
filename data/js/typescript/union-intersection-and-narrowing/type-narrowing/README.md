# JS / TypeScript / Union, Intersection & Narrowing / Type Narrowing

## 1. typeof Narrowing

A `typeof` check inside an `if` narrows a union to the matching
primitive type for the rest of that branch — TypeScript tracks this
automatically through the control flow.

```typescript
function format(value: string | number) {
  if (typeof value === "string") return value.trim();
  return value.toFixed(2);
}
```


## 2. instanceof Narrowing

`instanceof` narrows a union of class types to the specific class
matched, letting you safely call methods unique to that class inside
the checked branch.

```typescript
if (error instanceof ValidationError) {
  console.log(error.field);   // field only exists on ValidationError
}
```


## 3. Custom Type Guards

A function returning `value is Type` acts as a reusable narrowing check
— calling it in an `if` narrows the type wherever TypeScript can see
the call.

```typescript
function isString(value: unknown): value is string {
  return typeof value === "string";
}
```
