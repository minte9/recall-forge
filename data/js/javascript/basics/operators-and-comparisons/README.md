## JS / Javascript / Basics / Operators & Comparisons

### 1. Basic Operators

Arithmetic (`+ - * / % **`), assignment (`= += -=`), and increment operators
work mostly as in other C-like languages. `%` is remainder, not true
modulo, so it can return a negative result for negative operands.

~~~javascript
2 ** 10;      // 1024  -- exponentiation
-7 % 3;       // -1    -- remainder keeps the sign of the dividend
let x = 5;
x **= 2;      // x = 25
~~~


### 2. Comparisons

`===` compares value and type with no coercion; `==` coerces operands
first and produces surprising results (`"" == 0` is `true`). Default to
`===`/`!==` unless coercion is explicitly wanted.

~~~javascript
"" == 0;              // true  -- coerced, confusing
"" === 0;              // false -- strict, no coercion
null == undefined;      // true
null === undefined;     // false
~~~


### 3. Logical Operators

`&&` and `||` short-circuit and return one of their operands, not just a
boolean — useful for defaults. `??` (nullish coalescing) only falls back
on `null`/`undefined`, unlike `||`, which also falls back on `0` or `""`.

~~~javascript
0 || "default";    // "default" -- 0 is falsy
0 ?? "default";     // 0         -- 0 is not nullish
const name = user.name ?? "Guest";
~~~
