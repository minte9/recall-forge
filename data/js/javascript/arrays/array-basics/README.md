# JS / Javascript / Arrays / Array Basics

## 1. Common Array Methods

`push`/`pop` and `shift`/`unshift` add/remove from the end/start; `slice`
returns a copy of a range, while `splice` mutates the array in place — a
frequent source of bugs when a copy was expected.

```javascript
const arr = [1, 2, 3];
arr.slice(1);       // [2, 3] -- new array, arr unchanged
arr.splice(1, 1);   // removes index 1; arr is now [1, 3]
```


## 2. Iteration

`for...of` and `.forEach` both visit each element; `forEach` can't be
stopped early with `break`, so use a `for...of` loop when early exit is
needed.

```javascript
for (const n of [1, 2, 3]) {
  if (n === 2) break;   // works: for...of supports break
}
[1, 2, 3].forEach(n => { /* no break allowed here */ });
```


## 3. Array Destructuring

Positional unpacking of array elements into variables, with `,` used to
skip entries and `...rest` to gather the remainder.

```javascript
const [first, , third, ...rest] = [1, 2, 3, 4, 5];
// first = 1, third = 3, rest = [4, 5]
```
