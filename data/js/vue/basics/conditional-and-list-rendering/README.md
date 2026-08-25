# JS / Vue.js / Basics / Conditional & List Rendering

## 1. v-if, v-else, v-show

`v-if` adds/removes elements from the DOM entirely; `v-show` always
renders the element but toggles CSS `display`. Use `v-show` for elements
that toggle often, `v-if` when the condition rarely changes.

```vue
<p v-if="loggedIn">Welcome back</p>
<p v-else>Please log in</p>
<p v-show="loading">Loading...</p>
```


## 2. v-for & :key

`v-for` renders a list from an array or object; a unique `:key` is
required so Vue can track each item's identity across re-renders instead
of patching by index.

```vue
<li v-for="item in items" :key="item.id">
  {{ item.name }}
</li>
```


## 3. List Rendering Pitfalls

Using the array index as `:key` breaks identity tracking when items are
reordered or removed, causing stale component state. Also avoid `v-if`
and `v-for` on the same element — the precedence is easy to get wrong.

```vue
<!-- Bad: index as key breaks on reorder -->
<li v-for="(item, i) in items" :key="i">
<!-- Good -->
<li v-for="item in items" :key="item.id">
```
