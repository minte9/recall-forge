# JS / Javascript / DOM & Events / DOM Basics

## 1. Selecting & Modifying Elements

`querySelector`/`querySelectorAll` find elements with CSS selectors;
`textContent`, `classList`, and `setAttribute` are the common ways to
change what's on screen.

```javascript
const btn = document.querySelector(".submit-btn");
btn.textContent = "Save";
btn.classList.add("active");
```


## 2. Creating & Removing Nodes

Elements are built with `createElement` and attached with
`appendChild`/`append`; `remove()` detaches a node directly, no parent
reference needed.

```javascript
const li = document.createElement("li");
li.textContent = "New item";
list.appendChild(li);
li.remove();   // no need for list.removeChild(li)
```


## 3. Event Listeners

`addEventListener` attaches a handler without overwriting others (unlike
`onclick =`), and `removeEventListener` needs the exact same function
reference to detach it.

```javascript
function onClick() { console.log("clicked"); }
btn.addEventListener("click", onClick);
btn.removeEventListener("click", onClick);  // same reference
```
