## Python / Python / Basics / Mutability & Identity

### 1. Mutable vs Immutable Built-ins

Lists, dicts, and sets are mutable -- their contents can change in place without creating a new object. Tuples, strings, and numbers are immutable -- any "modification" (`s += "x"`) actually builds a new object and rebinds the name. This matters most for default arguments and shared references.

~~~python
def append_item(item, bucket=[]):   # BUG: default list is shared
    bucket.append(item)             # mutates across every call
    return bucket

append_item(1)  # [1]
append_item(2)  # [1, 2] -- surprise, same list reused
~~~


### 2. Small-Int & String Interning

CPython caches small integers (-5 to 256) and some string literals, so `is` on values in that range can appear to work by accident. Outside that range, equal values are separate objects, and relying on `is` for value comparison breaks depending on interpreter internals.

~~~python
a = 100; b = 100
a is b        # True -- both from the small-int cache

c = 1000; d = 1000
c is d        # False (usually) -- outside the cache
c == d        # True -- always use == for value comparison
~~~


### 3. `copy` vs `deepcopy`

`copy.copy()` makes a shallow copy -- a new outer container whose elements still point to the original nested objects. `copy.deepcopy()` recursively copies everything, so nested mutable objects are fully independent. Mutating a nested list after a shallow copy affects both copies.

~~~python
import copy

original = [[1, 2], [3, 4]]
shallow = copy.copy(original)
shallow[0].append(99)
print(original[0])  # [1, 2, 99] -- shared inner list

deep = copy.deepcopy(original)
deep[0].append(0)   # doesn't touch original at all
~~~
