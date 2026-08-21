## JS / Vue.js / Components / Slots

### 1. Default Slots

`<slot>` in a child component marks where parent-provided content gets
inserted, letting a component wrap arbitrary markup instead of only
accepting data props.

~~~vue
<!-- Card.vue -->
<div class="card"><slot></slot></div>
<!-- usage -->
<Card><p>Any content here</p></Card>
~~~


### 2. Named Slots

Multiple slots are distinguished by name; the parent targets one with
`<template #name>` (shorthand for `v-slot:name`), letting a component
define several content regions.

~~~vue
<!-- Layout.vue -->
<slot name="header"></slot>
<slot></slot>
<!-- usage -->
<Layout>
  <template #header><h1>Title</h1></template>
  <p>Main content</p>
</Layout>
~~~


### 3. Scoped Slots

A slot can pass data back up to the parent via attributes on `<slot>`;
the parent reads it through `v-slot="props"`, letting a child expose
internal state for custom rendering.

~~~vue
<!-- List.vue -->
<slot v-for="item in items" :item="item"></slot>
<!-- usage -->
<List v-slot="{ item }">{{ item.name }}</List>
~~~
