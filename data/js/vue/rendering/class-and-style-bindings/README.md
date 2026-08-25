# JS / Vue.js / Rendering / Class & Style Bindings

## 1. Binding HTML Classes

`:class` accepts a string, but also an object (`{ active: isActive }`)
or array, applying a class conditionally based on reactive state without
manual string concatenation.

```vue
<div :class="{ active: isActive, error: hasError }"></div>
```


## 2. Binding Inline Styles

`:style` accepts a camelCased object (`{ color: 'red' }`) or an array of
style objects merged together, avoiding manual CSS string building.

```vue
<div :style="{ color: textColor, fontSize: size + 'px' }"></div>
```


## 3. Array & Object Syntax

Both `:class` and `:style` accept arrays to combine multiple bindings —
objects for conditional classes, plain strings/variables for static or
computed ones, all mixable in one array.

```vue
<div :class="[baseClass, { active: isActive }]"></div>
```
