# Python / Python / Functions & Decorators / First-Class Functions

## 1. Functions as Values

Functions in Python are objects like any other -- they can be assigned to variables, stored in lists, passed as arguments, and returned from other functions. This is what makes decorators, callbacks, and higher-order functions like `sorted(key=...)` possible.

```python
def shout(text):
    return text.upper()

greeting = shout          # function stored in a variable
greeting("hi")             # "HI" -- called through the new name

sorted(["bb", "a", "ccc"], key=len)   # function passed as arg
```


## 2. Lambda Expressions

`lambda` creates a small anonymous function limited to a single expression (no statements, no assignments). It's meant for short, throwaway callbacks like a `sorted()` key -- anything more complex should be a regular `def` function for readability.

```python
pairs = [(1, "b"), (2, "a")]
pairs.sort(key=lambda p: p[1])   # sort by second element
# [(2, 'a'), (1, 'b')]
```
