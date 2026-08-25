# JS / Javascript / Functions / this Keyword

## 1. this in Different Contexts

`this` is determined by how a function is called, not where it's defined.
As a method, `this` is the object before the dot; as a plain function
call, it's `undefined` in strict mode.

```javascript
const obj = {
  name: "Ana",
  greet() { return this.name; },
};
const fn = obj.greet;
obj.greet();  // "Ana"  -- called as a method
fn();          // TypeError: this is undefined (strict mode)
```


## 2. call, apply & bind

`call` and `apply` invoke a function immediately with an explicit `this`
(differing only in argument form); `bind` returns a new function
permanently bound to that `this`.

```javascript
function greet(greeting) { return `${greeting}, ${this.name}`; }
greet.call({ name: "Ana" }, "Hi");     // "Hi, Ana"
greet.apply({ name: "Ana" }, ["Hi"]);  // same, args as array
const bound = greet.bind({ name: "Ana" });
bound("Hi");                            // "Hi, Ana"
```


## 3. Arrow Functions and this

Arrow functions capture `this` from their enclosing lexical scope at
definition time and never rebind it, even with `call`/`apply`/`bind`.
This makes them ideal for callbacks inside methods.

```javascript
const obj = {
  name: "Ana",
  greetLater() {
    setTimeout(() => console.log(this.name), 0); // this = obj
  },
};
obj.greetLater();  // "Ana"
```
