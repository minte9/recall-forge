## PVM / Python / Algorithms / Divide & Conquer Sorts

### 1. Merge Sort

Merge sort recursively splits the list in half down to single elements (the base case), then merges pairs of already-sorted halves back together in order as the recursion unwinds. Splitting is O(log n) deep, and merging each level takes O(n), giving O(n log n) overall -- better than the O(n^2) comparison sorts.

~~~py
def merge_sort(items):
    if len(items) <= 1:  # base case
        return items

    m = len(items) // 2
    L = merge_sort(items[:m])  # recursive
    R = merge_sort(items[m:])

    i = j = 0
    sorted_items = []
    while len(sorted_items) < len(items):
        if L[i] < R[j]:
            sorted_items.append(L[i])
            i += 1
        else:
            sorted_items.append(R[j])
            j += 1

        if i == len(L):
            sorted_items.extend(R[j:])
            break
        if j == len(R):
            sorted_items.extend(L[i:])
            break

    return sorted_items

data = [2, 9, 8, 5, 3, 4, 7, 6]
print(merge_sort(data))

"""
    [2, 3, 4, 5, 6, 7, 8, 9]
"""
~~~


### 2. Quicksort

Quicksort picks a pivot (here, the last element) and partitions the list around it in one pass: everything smaller ends up to its left, everything larger to its right, and the pivot lands in its final sorted position. It then recursively sorts the left and right partitions -- O(n log n) on average, but O(n^2) worst case if the data is already sorted (a bad pivot choice every time).

~~~py
def quicksort(items, i=0, j=None):
    if j is None:
        j = len(items) - 1

    if i > j:  # base case
        return

    pivot = items[j]
    for k in range(i, j + 1):
        if items[k] < pivot:
            items[i], items[k] = items[k], items[i]  # swap items
            i += 1
        if items[k] == pivot:
            items[i], items[k] = items[k], items[i]  # move pivot

    quicksort(items, 0, i - 1)  # sort left partition
    quicksort(items, i + 1, j)  # sort right partition

data = [8, 18, 4, 2, 10]
quicksort(data)
print("Sorted:", data)

"""
    Sorted: [2, 4, 8, 10, 18]
"""
~~~
