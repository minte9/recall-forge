# Python / Python / OOP / Classes & Instances

## 1. Class Definition & `__init__`

`class` defines a blueprint; `__init__` runs once per instance right after creation to set up its starting state. `self` is the instance itself, passed automatically -- every instance method takes it as the first parameter to access that instance's own attributes.

```python
class Account:
    def __init__(self, owner, balance=0):
        self.owner = owner      # instance attribute
        self.balance = balance

a = Account("Alice", 100)
```


## 2. Instance vs Class Attributes

Attributes set on `self` inside `__init__` belong to that one instance. Attributes defined directly in the class body are shared by every instance until an instance sets its own copy of the same name, which then shadows the class-level value.

```python
class Dog:
    species = "Canis familiaris"   # shared by all instances

    def __init__(self, name):
        self.name = name            # unique per instance

Dog("Rex").species    # "Canis familiaris" -- reads class attr
```


## 3. Instance, Class, and Static Methods

An instance method takes `self` and operates on one object. A `@classmethod` takes `cls` instead and operates on the class itself, often used for alternate constructors. A `@staticmethod` takes neither -- it's a plain function namespaced inside the class.

```python
class Pizza:
    def __init__(self, toppings):
        self.toppings = toppings

    @classmethod
    def margherita(cls):
        return cls(["mozzarella", "basil"])   # alt constructor

    @staticmethod
    def is_valid_topping(name):
        return name in KNOWN_TOPPINGS
```
