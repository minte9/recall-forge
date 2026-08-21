## JS / Vue / Rendering / Template Refs & Dynamic Components

### 1. Template Refs

`ref="name"` on an element exposes the actual DOM node (or component
instance) via a matching `ref()` in script — an escape hatch for direct
DOM access Vue's declarative model doesn't cover.

~~~vue
<input ref="inputEl">
<script setup>
const inputEl = ref(null);
onMounted(() => inputEl.value.focus());
</script>
~~~


### 2. Dynamic Components

`<component :is="currentView">` renders whichever component the bound
value points to, enabling tabbed interfaces or plugin-like views without
a chain of `v-if`s.

~~~vue
<component :is="currentTab"></component>
~~~


### 3. keep-alive

Wrapping dynamic components in `<KeepAlive>` caches their instance
instead of destroying it on switch, preserving component state (like
scroll position or form input) across tab changes.

~~~vue
<KeepAlive>
  <component :is="currentTab"></component>
</KeepAlive>
~~~
