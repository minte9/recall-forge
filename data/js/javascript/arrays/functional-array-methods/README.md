## JS / Javascript / Arrays / Functional Array Methods

### 1. map, filter, reduce

`map` transforms each element into a new array of the same length;
`filter` keeps elements matching a predicate; `reduce` folds the array
into a single accumulated value.

~~~javascript
const nums = [1, 2, 3, 4];
nums.map(n => n * 2);            // [2, 4, 6, 8]
nums.filter(n => n % 2 === 0);   // [2, 4]
nums.reduce((sum, n) => sum + n, 0); // 10
~~~


### 2. Sorting Arrays

`.sort()` mutates the array in place and, without a comparator, sorts
elements as strings — `[10, 2, 1].sort()` gives `[1, 10, 2]`, not numeric
order.

~~~javascript
[10, 2, 1].sort();                // [1, 10, 2] -- string sort!
[10, 2, 1].sort((a, b) => a - b); // [1, 2, 10] -- numeric sort
~~~


### 3. Spread in Arrays

`...` expands an array's elements — used to merge arrays, copy them
shallowly, or pass elements as individual function arguments.

~~~javascript
const a = [1, 2];
const b = [3, 4];
const merged = [...a, ...b];   // [1, 2, 3, 4]
const copy = [...a];            // shallow copy, independent array
~~~
