## JS / Vue.js / Reactivity / How Reactivity Works

### 1. Proxies Under the Hood

Vue 3's reactivity wraps objects in a JS `Proxy` that intercepts property
get/set to track dependencies and trigger updates — a rewrite from Vue
2's `Object.defineProperty`, which couldn't detect new properties.

~~~javascript
const state = reactive({ count: 0 });
state.newProp = 1;   // detected! Proxy catches new properties too
~~~


### 2. Reactivity Caveats

Destructuring a `reactive()` object loses reactivity, since the
destructured variable is a plain value disconnected from the Proxy.
Replacing a `ref`'s `.value` works, but replacing the whole `ref`
variable does not.

~~~javascript
const state = reactive({ count: 0 });
let { count } = state;   // count is now a plain, disconnected number
count++;                  // does NOT update state.count
~~~


### 3. readonly

`readonly()` wraps a reactive object so mutations are blocked (and
warned in dev mode) — useful for passing state down to components that
shouldn't modify it directly.

~~~javascript
const original = reactive({ count: 0 });
const copy = readonly(original);
copy.count++;   // warning: readonly, mutation ignored
~~~
