## JS / Javascript / Arrays / Map & Set

### 1. Map Basics

`Map` stores key-value pairs like an object but allows any value
(including objects) as a key and reliably preserves insertion order.

~~~javascript
const map = new Map();
const objKey = {};
map.set(objKey, "value").set("str", 1);
map.get(objKey);   // "value" -- object identity as key
map.size;           // 2
~~~


### 2. Set Basics

`Set` stores unique values — adding a duplicate is a no-op — making it a
concise way to dedupe or test membership.

~~~javascript
const set = new Set([1, 2, 2, 3]);
set.size;         // 3 -- duplicates dropped
set.has(2);        // true
[...new Set([1, 1, 2])]; // [1, 2] -- dedupe an array
~~~


### 3. Map/Set vs Object/Array

Prefer `Map` over an object when keys aren't strings or when insertion
order and size matter; prefer `Set` over an array when membership checks
(`has`) matter more than order-based access.

~~~javascript
obj.hasOwnProperty("key");  // works, but keys coerce to strings
map.has(objKey);             // Map: any key type, O(1) lookup
~~~
