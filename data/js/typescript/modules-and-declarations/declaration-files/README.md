# JS / TypeScript / Modules & Declarations / Declaration Files

## 1. .d.ts Files

A `.d.ts` file contains only type information, no implementation — it
describes the shape of existing JS code so TypeScript can type-check
code that uses it.

```typescript
// math.d.ts
export function add(a: number, b: number): number;
```


## 2. declare

`declare` tells TypeScript a value exists at runtime (defined
elsewhere, like a global script tag) without requiring an
implementation in that file.

```typescript
declare const VERSION: string;   // provided by a build tool, e.g.
console.log(VERSION);              // typed, no import needed
```


## 3. Typing an Untyped Package

For a JS package with no types, a hand-written `.d.ts` (or an
`@types/package-name` package from DefinitelyTyped) supplies the
missing type information without touching the package itself.

```javascript
// npm install --save-dev @types/lodash
import _ from "lodash";   // now typed
```
