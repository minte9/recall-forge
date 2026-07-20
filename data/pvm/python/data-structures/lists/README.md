## PVM / Python / Data Structures / Lists

### 1. Reading lists

Accessing an item by index takes one step: the computer jumps directly to that memory address. There's no scanning involved, so time complexity is O(1) regardless of list size. It's like opening a book to a specific page using a bookmark.

~~~py
data = ["apples", "bananas", "oranges"]
found_item = data[1]  # O(1) -- direct address lookup
print(found_item)  # bananas
~~~


### 2. Searching lists

Linear search checks items one by one until it finds the target or reaches the end, so it takes up to n steps for n items. Best case is O(1) (first item matches); worst case is O(n) (last item matches, or nothing does). Use it on unsorted or small lists, where sorting first isn't worth the overhead.

~~~py
fruits = ["apples", "bananas", "oranges"]

def search_value(items, value):
    for i in range(len(items)):
        if value == items[i]:
            return i
    return -1

print(search_value(fruits, "oranges"))  # 2 -- worst case, O(n)
print(search_value(fruits, "apples"))   # 0 -- best case, O(1)
~~~


### 3. Inserting lists

Inserting at a given index means shifting every element to its right one position to make room, then writing the new value. That's up to n shifts plus one write, so time complexity is O(n) -- proportional to how many elements sit to the right of the insertion point. It's like seating someone in a full row of theater seats: everyone to the right has to move over one seat.

~~~py
def add(data, k, value):
    items = data[:]        # avoid mutating the caller's list
    items.append("")       # make room at the end
    for i in range(len(items) - 1, k, -1):
        items[i] = items[i - 1]  # shift right
    items[k] = value
    return items

fruits = ["apples", "bananas", "oranges"]
print(add(fruits, 1, "kiwi"))  # ['apples', 'kiwi', 'bananas', 'oranges']
~~~


### 4. Deleting lists

Deleting at a given index means shifting every element to its right one position left to close the gap, then popping the now-duplicate trailing element. That's up to n-1 shifts plus one pop, so time complexity is O(n) -- proportional to how many elements sit to the right of the removed index. It's like pulling a book from a tight shelf: everything to the right slides left to close the gap.

~~~py
def delete_item(data, k):
    for i in range(k, len(data) - 1):
        data[i] = data[i + 1]  # shift left
    data.pop()                 # remove trailing duplicate

fruits = ["apples", "bananas", "oranges"]
delete_item(fruits, 1)
print(fruits)  # ['apples', 'oranges']
~~~
