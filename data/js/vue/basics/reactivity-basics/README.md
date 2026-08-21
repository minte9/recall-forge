## JS / Vue.js / Basics / Reactivity Basics

### 1. ref vs reactive

`ref()` wraps any value (primitive or object) in a reactive container
accessed via `.value`; `reactive()` makes an object itself reactive
directly, with no `.value`. Use `ref` for primitives, since `reactive`
can't wrap them.

~~~javascript
const count = ref(0);
count.value++;              // must use .value in script

const state = reactive({ count: 0 });
state.count++;               // no .value needed
~~~


### 2. Reactive State

Vue tracks reads and writes on reactive data via Proxies, so any
component using that data automatically re-renders when it changes — no
manual subscription needed.

~~~javascript
const state = reactive({ count: 0 });
setInterval(() => state.count++, 1000);  // triggers re-render each tick
~~~


### 3. Unwrapping Refs

In templates, refs are automatically unwrapped — no `.value` needed.
Inside `<script>`, `.value` is always required, and reassigning a whole
`reactive()` object breaks reactivity, since the new object isn't tracked.

~~~vue
<template>
  <p>{{ count }}</p>  <!-- auto-unwrapped, no .value -->
</template>
<script setup>
const count = ref(0);   // .value needed here
</script>
~~~
