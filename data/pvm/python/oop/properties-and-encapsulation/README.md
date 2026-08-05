## Python / Python / OOP / Properties & Encapsulation

### 1. `@property`

`@property` turns a method into an attribute-like accessor -- callers write `obj.value` instead of `obj.value()`, while the class keeps control to validate or compute on access. Pairing it with `@value.setter` lets assignment (`obj.value = x`) run validation too.

~~~python
class Temperature:
    def __init__(self, celsius):
        self._celsius = celsius

    @property
    def fahrenheit(self):
        return self._celsius * 9 / 5 + 32   # computed, read-only

    @fahrenheit.setter
    def fahrenheit(self, value):
        self._celsius = (value - 32) * 5 / 9
~~~


### 2. Name Mangling (`_` vs `__`)

A single leading underscore (`_attr`) is just a convention signaling "internal, don't touch" -- Python doesn't enforce it. A double leading underscore (`__attr`) triggers name mangling to `_ClassName__attr`, mainly to avoid accidental collisions in subclasses, not true privacy.

~~~python
class Base:
    def __init__(self):
        self.__secret = 42   # stored as _Base__secret

b = Base()
b.__secret          # AttributeError
b._Base__secret      # 42 -- still reachable, just discouraged
~~~
