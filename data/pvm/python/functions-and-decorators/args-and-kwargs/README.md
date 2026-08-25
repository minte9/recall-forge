# Python / Python / Functions & Decorators / Args & Kwargs

## 1. `*args` & `**kwargs`

`*args` collects extra positional arguments into a tuple; `**kwargs` collects extra keyword arguments into a dict. They let a function accept a variable number of arguments, and are commonly used together in wrapper functions that forward everything to another callable.

```python
def log_call(*args, **kwargs):
    print("args:", args)
    print("kwargs:", kwargs)

log_call(1, 2, name="Alice")
# args: (1, 2)
# kwargs: {'name': 'Alice'}
```


## 2. Unpacking Into Calls

The same `*`/`**` syntax works in reverse at a call site -- `*seq` spreads a list/tuple into separate positional arguments, `**mapping` spreads a dict into keyword arguments. This is how a wrapper forwards its captured `*args, **kwargs` untouched.

```python
def greet(name, greeting="Hi"):
    print(f"{greeting}, {name}")

info = {"name": "Bob", "greeting": "Hello"}
greet(**info)   # unpacks dict into keyword args
```
