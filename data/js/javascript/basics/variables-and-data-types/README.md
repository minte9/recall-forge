# JS / Javascript / Basics / Variables & Data Types

## 1. Variables

`let` and `const` are block-scoped; `const` prevents reassignment but not
mutation of what it points to. `var` is function-scoped and hoisted as
`undefined`, which is why modern code avoids it. Prefer `const`, falling
back to `let` only when a binding must change.

```javascript
const user = { name: "Ana" };
user.name = "Bob";     // OK: mutating, not reassigning
// user = {};           // TypeError: reassignment
let count = 0;
count += 1;              // fine: let allows reassignment
```


## 2. Data Types

JS has 7 primitive types (string, number, boolean, null, undefined, symbol,
bigint) plus objects. Primitives are copied by value; objects are copied by
reference. `typeof null` famously returns `"object"` — a long-standing
language quirk worth remembering.

```javascript
typeof "hi";        // "string"
typeof 42;            // "number"
typeof undefined;     // "undefined"
typeof null;          // "object"  -- historic bug, not a real object
typeof {};              // "object"
```


## 3. Type Conversion

JS coerces types implicitly in many operators (`+`, `==`, template
literals) and explicitly via `Number()`, `String()`, `Boolean()`. `+`
concatenates when either operand is a string, but subtracts numerically
otherwise — a frequent source of bugs.

```javascript
"5" + 3;        // "53"  -- string concatenation
"5" - 3;        // 2     -- numeric coercion
Number("5px");  // NaN   -- explicit conversion, fails to parse
Boolean("");     // false -- empty string is falsy
```
