# JS / Javascript / Functions / Closures & Scope

## 1. Lexical Scope

A function's scope is determined by where it's written in the source, not
where it's called from. Inner functions can read variables from any
enclosing function, forming a chain resolved at definition time.

```javascript
function outer() {
  const x = 10;
  function inner() { return x; }  // reads outer's x lexically
  return inner();
}
```


## 2. Closures

A closure is a function that remembers variables from its enclosing scope
even after that scope has returned. This is how private state is
simulated without classes.

```javascript
function counter() {
  let count = 0;
  return () => ++count;   // closes over `count`
}
const inc = counter();
inc(); inc();   // 1, then 2 -- count persists between calls
```


## 3. The Loop-Closure Classic

`var` in a loop shares one binding across all iterations, so callbacks
capture the final value. `let` creates a fresh binding per iteration,
fixing the trap.

```javascript
for (var i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 0);  // logs 3, 3, 3
}
for (let j = 0; j < 3; j++) {
  setTimeout(() => console.log(j), 0);  // logs 0, 1, 2
}
```
