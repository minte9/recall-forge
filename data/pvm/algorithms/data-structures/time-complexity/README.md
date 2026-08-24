## Python / Algorithms / Data Structures / Time Complexity

### 1. Big-O Notation

Big-O describes how an algorithm's number of steps grows as input size grows, not the exact step count. Constants and small differences are ignored -- only the growth trend as input approaches infinity matters. It answers one question: what happens to performance when the input becomes very large?


### 2. Linear Search - O(n)

Linear search checks each element in order until it finds the target, so it takes up to n steps for n elements. Best case is O(1) (the match is first); worst case is O(n) (the match is last, or missing entirely). It's like scanning an unsorted contact list from the top.

~~~py
contacts = ['Alice', 'Bob', 'Carol', 'Dave', 'Eve']

def find_contact(contacts, name):
    steps = 0
    for i in range(len(contacts)):
        steps += 1
        if name == contacts[i]:
            return i, steps  # found after `steps` checks
    return -1, steps

index, steps = find_contact(contacts, 'Dave')
print(f"Found at index: {index} Steps: {steps}")  # Found at index: 3 Steps: 4
~~~


### 3. Binary Search - O(log n)

Binary search repeatedly checks the middle of a sorted list and discards the half that can't contain the target, so each step halves the search space. Doubling the data only adds one extra step -- O(log n) comparisons in the worst case -- but it only works on sorted data. It's like finding a word in a dictionary by jumping to the middle each time.

~~~py
def binary_search(items, val):
    left, right, steps = 0, len(items) - 1, 0
    while left <= right:
        steps += 1
        middle = (left + right) // 2
        if val == items[middle]:
            return middle, steps
        if val > items[middle]:
            left = middle + 1
        else:
            right = middle - 1
    return -1, steps

# Doubling the list size adds only ~1 extra step
for data in ([i for i in range(10)], [i for i in range(21)], list(range(100000))):
    key, steps = binary_search(data, data[len(data) // 3])
    print(f"n={len(data)}: steps={steps}")
~~~
