# JS / Javascript / Objects / Object Destructuring

## 1. Basic Destructuring

Pulls named properties out of an object into standalone variables in one
statement, optionally renaming them.

```javascript
const user = { name: "Ana", age: 30 };
const { name, age: years } = user;
// name = "Ana", years = 30
```


## 2. Nested & Default Values

Destructuring can reach into nested objects directly and supply fallback
values for properties that might be missing.

```javascript
const { address: { city = "Unknown" } = {} } = user;
// safe even if `address` itself is missing, thanks to `= {}`
```


## 3. Destructuring in Function Parameters

Destructuring directly in a parameter list turns an options object into
named, self-documenting arguments.

```javascript
function createUser({ name, age = 18 }) {
  return `${name} (${age})`;
}
createUser({ name: "Ana" });  // "Ana (18)"
```
