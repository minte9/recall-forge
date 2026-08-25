# JS / TypeScript / Modules & Declarations / TypeScript Modules

## 1. import/export in TS

TypeScript compiles standard ES module `import`/`export` syntax, adding
compile-time type checking across module boundaries on top of the same
runtime behavior as JavaScript.

```typescript
// math.ts
export function add(a: number, b: number): number { return a + b; }
// app.ts
import { add } from "./math";
```


## 2. Type-Only Imports

`import type { X }` imports only for type-checking purposes and is
fully erased at compile time — makes the intent explicit and avoids
accidental runtime side effects from a type-only module.

```typescript
import type { User } from "./types";
function greet(user: User) { /* ... */ }
```


## 3. Module Resolution

TypeScript resolves `import` paths using the same Node-style algorithm
as JavaScript by default, but `tsconfig.json`'s `paths` can remap
import specifiers to custom locations.

```javascript
// tsconfig.json
{ "compilerOptions": { "paths": { "@utils/*": ["src/utils/*"] } } }
```
