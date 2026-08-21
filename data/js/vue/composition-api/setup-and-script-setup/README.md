## JS / Vue / Composition API / setup() & script setup

### 1. The setup() Function

`setup()` runs before the component is created, before `this` exists;
anything it returns becomes available to the template, replacing
`data`/`methods`/`computed` from the Options API.

~~~javascript
export default {
  setup() {
    const count = ref(0);
    return { count };   // exposed to the template
  },
};
~~~


### 2. script setup

`<script setup>` is compile-time sugar that removes the need for an
explicit `setup()` return — every top-level binding is automatically
exposed to the template.

~~~vue
<script setup>
const count = ref(0);   // no return needed, template sees it
</script>
~~~


### 3. Composition vs Options API

The Options API groups code by type (`data`, `methods`, `computed`); the
Composition API groups code by logical feature. Composition API scales
better for complex components and enables composables.

~~~javascript
// Options: split by type
export default { data() {}, methods: {}, computed: {} };
// Composition: split by feature, colocated
function useSearch() { const q = ref(""); return { q }; }
~~~
