# JS / Vue.js / Testing / Vue Test Utils Basics

## 1. Mounting a Component

`mount()` renders a component in an isolated test environment,
returning a wrapper with helpers to query the DOM and inspect component
state.

```javascript
import { mount } from "@vue/test-utils";
const wrapper = mount(Counter);
```


## 2. Finding Elements & Asserting

`wrapper.find()`/`.findAll()` query rendered output with CSS selectors;
`.text()` and `.exists()` are common assertions on what actually
rendered.

```javascript
expect(wrapper.find(".count").text()).toBe("0");
expect(wrapper.find(".error").exists()).toBe(false);
```


## 3. Simulating User Input

`.trigger()` simulates DOM events like clicks or input changes, and
`await` is required afterward since Vue's DOM updates happen
asynchronously in the next tick.

```javascript
await wrapper.find("button").trigger("click");
expect(wrapper.find(".count").text()).toBe("1");
```
