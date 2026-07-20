## PVM / Python / Data Structures / Hash Tables

### 1. Hashing Basics

Hashing converts a key into a number that determines where its value is stored. Real hash functions are one-way and produce fixed-size output; the example here is simplified purely to show the idea.

~~~py
letter_values = {'A': 1, 'B': 2, 'C': 3, 'D': 4, 'E': 5}

def hash_sum(word):
    return sum(letter_values[c] for c in word)

print(hash_sum('BAD'))  # 7 -- sum of letter values, used as a slot index
~~~


### 2. Hash Table

A hash table maps a key to a slot index via a hash function, then stores the value there -- giving O(1) average-case get/set. This tiny version only supports lowercase strings and ignores collisions; a real implementation (Python's `dict`) handles both.

~~~py
class HashTable:
    def __init__(self):
        self.capacity = 16
        self.table = [None] * self.capacity

    def _hash(self, key):
        h = 0
        for ch in key:
            h = (h * 257 + ord(ch)) % self.capacity
        return h

    def __setitem__(self, key, val):
        self.table[self._hash(key)] = val  # average O(1)

    def __getitem__(self, key):
        return self.table[self._hash(key)]  # average O(1)

thesaurus = HashTable()
thesaurus['bad'] = 'evil'
print(thesaurus['bad'])  # evil
~~~


### 3. Dictionary

Python's built-in `dict` is a highly optimized hash table (Java's equivalent is `HashMap`), giving O(1) average-case lookups, inserts, and deletes -- versus O(n) for an unordered list scan or O(log n) for binary search on a sorted one.

~~~py
prices = {"apple": 0.50, "banana": 0.30, "orange": 0.80}

item = prices['banana']  # O(1) read
prices["kiwi"] = 0.90    # O(1) insert
del prices["banana"]     # O(1) delete

print(prices)  # {'apple': 0.5, 'orange': 0.8, 'kiwi': 0.9}
~~~
