## JS / Vue.js / Components / Props

### 1. Declaring Props

Props are declared with `defineProps()` (in `<script setup>`) or the
`props` option, making them available in the template and script as
read-only reactive values passed down from the parent.

~~~javascript
const props = defineProps({
  title: String,
  count: { type: Number, default: 0 },
});
~~~


### 2. Prop Validation

Prop definitions can specify `type`, `required`, `default`, and a custom
`validator` function — Vue warns in dev mode when a passed prop doesn't
match, catching integration bugs early.

~~~javascript
defineProps({
  status: {
    type: String,
    validator: (v) => ["active", "closed"].includes(v),
  },
});
~~~


### 3. One-Way Data Flow

Props flow down from parent to child only; a child should never mutate a
prop directly — Vue warns if it does. To derive a value from a prop, use
local state initialized from it, or emit an event upward instead.

~~~javascript
// props.count++;  // warning: mutating a prop directly
const localCount = ref(props.count);  // copy, mutate locally instead
~~~
