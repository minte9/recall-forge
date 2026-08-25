# JS / Javascript / Objects / Object Basics

## 1. Object Literals & Property Access

Objects are collections of key-value pairs, created with `{}` and
accessed via dot notation or brackets — brackets are needed for dynamic
or non-identifier keys.

```javascript
const user = { "first-name": "Ana", age: 30 };
user.age;                   // 30
user["first-name"];         // "Ana" -- bracket needed, has a dash
const key = "age";
user[key];                  // 30 -- dynamic key access
```


## 2. References vs Copies

Assigning an object to a new variable copies the reference, not the data
— both variables point to the same object, so mutating one affects the
other.

```javascript
const a = { count: 1 };
const b = a;          // b references the same object
b.count = 2;
a.count;               // 2 -- a sees the mutation too
const c = { ...a };    // shallow copy: independent top-level object
```


## 3. Optional Chaining

`?.` short-circuits to `undefined` instead of throwing when accessing a
property on `null`/`undefined`, avoiding manual null checks on deeply
nested paths.

```javascript
const user = { address: null };
user.address.city;    // TypeError
user.address?.city;    // undefined -- safe, short-circuits
user.getName?.();      // safe optional call too
```
