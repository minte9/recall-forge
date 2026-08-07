## Python / Python / Basics / Variables & Dynamic Typing

### 1. Dynamic Typing & Name Binding

Python variables are just names bound to objects, not typed storage boxes -- the type lives on the object, not the name, so the same name can rebind to a different type at any time. Assignment (`x = 5`) creates a reference, it doesn't copy data into a slot.

~~~python
x = 5          # x binds to an int object
x = "five"     # same name, now binds to a str object
y = x          # y binds to the same str object as x
~~~


### 2. Multiple Assignment & Unpacking

Python can bind several names in one statement, either to the same object (`a = b = 1`) or by unpacking an iterable's elements positionally (`a, b = 1, 2`). The starred target (`*rest`) captures any extra items into a list, useful for splitting a header from the remaining values.

~~~python
first, *middle, last = [1, 2, 3, 4, 5]
# first=1, middle=[2, 3, 4], last=5

a, b = b, a  # swap without a temp variable
~~~


### 3. `is` vs `==`

`==` compares values via `__eq__`, while `is` compares object identity (`id(a) == id(b)`). Two equal-looking values aren't always the same object, so `is` should be reserved for identity checks like `x is None`, never as a shortcut for value equality.

~~~python
a = [1, 2]
b = [1, 2]
a == b   # True -- same values
a is b   # False -- different objects

x = None
x is None   # True -- identity check, the idiomatic form
~~~
