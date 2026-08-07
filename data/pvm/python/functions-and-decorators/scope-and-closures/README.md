## Python / Python / Functions & Decorators / Scope & Closures

### 1. The LEGB Rule

Python looks up a name using the LEGB order -- Local (current function), Enclosing (any outer function), Global (module level), Built-in (Python's own names) -- stopping at the first match. Assigning to a name inside a function makes it local by default, even if a global of that name exists.

~~~python
x = "global"

def outer():
    x = "enclosing"
    def inner():
        x = "local"
        print(x)   # "local" -- LEGB stops at Local
    inner()
~~~


### 2. Closures & `nonlocal`

A nested function that references a variable from its enclosing scope "closes over" it, keeping it alive even after the outer function returns. Reassigning (not just reading) that variable from inside the nested function requires declaring it `nonlocal` first.

~~~python
def make_counter():
    count = 0
    def increment():
        nonlocal count   # without this, count += 1 raises
        count += 1
        return count
    return increment

counter = make_counter()
counter()   # 1
counter()   # 2 -- count persisted between calls
~~~
