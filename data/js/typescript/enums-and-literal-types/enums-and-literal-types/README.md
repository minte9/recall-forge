# JS / TypeScript / Enums & Literal Types / Enums & Literal Types

## 1. Numeric Enums

A numeric `enum` auto-assigns increasing numbers starting at 0 unless
given explicit values — members are usable as both a type and runtime
values, unlike a plain union.

```typescript
enum Status { Pending, Active, Closed }
Status.Active;   // 1
```


## 2. String Enums

String enums require every member to have an explicit string value,
giving more readable debug output and serialized values than numeric
enums.

```typescript
enum Status { Pending = "PENDING", Active = "ACTIVE" }
```


## 3. Literal Types & as const

A literal type restricts a value to one exact value (`"GET"` as a type,
not just `string`); `as const` freezes an object/array so its
properties become literal types instead of widened ones.

```typescript
let method: "GET" | "POST" = "GET";
const config = { method: "GET" } as const;
// config.method is typed as "GET", not string
```
