# Python / Algorithms / Data Structures / Sets

## 1. Inserting Sets

A set stores unique, unordered elements and rejects duplicates automatically, so unlike a list, insertion is O(1) on average thanks to hashing -- no duplicate-checking loop needed. Reading and inserting are O(1); searching and deleting stay O(n) like a list. It's like a guest list where a name can appear only once: adding an existing name simply changes nothing.

```py
fruits = {'apples', 'bananas', 'oranges'}
fruits.add('kiwi')  # O(1) -- hashing decides the slot, no scan needed
print(fruits)
```
