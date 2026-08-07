## Python / Python / Iterators, Generators & Comprehensions / Itertools Essentials

### 1. `chain` & `islice`

`itertools.chain(*iterables)` links several iterables into one continuous sequence without concatenating them into a new list first. `itertools.islice(it, start, stop)` slices a lazy iterator the way `[start:stop]` slices a list, without materializing it.

~~~python
from itertools import chain, islice

list(chain([1, 2], [3, 4]))       # [1, 2, 3, 4]
list(islice(range(100), 5, 8))     # [5, 6, 7] -- no full list built
~~~


### 2. `groupby`

`itertools.groupby` groups consecutive equal elements -- it does *not* group across the whole sequence, so input usually needs sorting by the same key first, or groups fragment wherever the value changes and reappears later.

~~~python
from itertools import groupby

data = sorted(["fig", "apple", "fox", "banana"], key=len)
for length, group in groupby(data, key=len):
    print(length, list(group))
# 3 ['fig', 'fox']
# 5 ['apple']
# 6 ['banana']
~~~
