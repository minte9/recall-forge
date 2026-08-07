## Python / Python / OOP / Inheritance & MRO

### 1. Single Inheritance & `super()`

A subclass inherits its parent's attributes and methods, and can override any of them. `super().__init__(...)` calls the parent's implementation from inside an override, so the subclass extends rather than fully replaces the parent's setup logic.

~~~python
class Animal:
    def __init__(self, name):
        self.name = name

class Dog(Animal):
    def __init__(self, name, breed):
        super().__init__(name)   # reuse parent's setup
        self.breed = breed
~~~


### 2. Multiple Inheritance & MRO

A class can inherit from several parents at once; Python resolves attribute lookups using the Method Resolution Order (MRO), computed via C3 linearization, which keeps each parent's own base-to-derived order consistent. `ClassName.__mro__` shows the lookup sequence.

~~~python
class A:
    def greet(self): return "A"

class B(A):
    def greet(self): return "B"

class C(A):
    def greet(self): return "C"

class D(B, C):
    pass

D().greet()   # "B" -- B comes before C in the MRO
D.__mro__      # (D, B, C, A, object)
~~~


### 3. Abstract Base Classes

`abc.ABC` with `@abstractmethod` defines a contract subclasses must fulfill -- instantiating a class that hasn't implemented every abstract method raises `TypeError` immediately, rather than failing later with an `AttributeError` when the missing method is called.

~~~python
from abc import ABC, abstractmethod

class Shape(ABC):
    @abstractmethod
    def area(self): ...

class Square(Shape):
    def __init__(self, side): self.side = side
    def area(self): return self.side ** 2

Shape()   # TypeError: Can't instantiate abstract class
~~~
