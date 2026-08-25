# Python / Algorithms / Algorithms / Comparison Sorts

## 1. Bubble Sort

Bubble sort repeatedly walks the list, comparing each pair of adjacent elements and swapping them if they're out of order. Each full pass pushes the largest remaining element to its correct position at the end, so after n passes the list is sorted -- O(n^2) in the worst case.

```py
def bubble_sort(X):
    for i in range(len(X)):
        for j in range(len(X) - i - 1):
            if X[j] > X[j + 1]:
                X[j], X[j + 1] = X[j + 1], X[j]

A = [1, 3, 9, 5, 4, 0]
bubble_sort(A)
print("Sorted list =", A)

"""
    Sorted list = [0, 1, 3, 4, 5, 9]
"""
```


## 2. Selection Sort

Selection sort repeatedly scans the unsorted portion of the list to find its minimum, then swaps that minimum into the next position at the front. It does fewer swaps than bubble sort (one per pass instead of many), but is still O(n^2) because every pass rescans the remaining unsorted elements.

```py
def selection_sort(X):
    for i in range(len(X)):
        min_index = i

        for j in range(i + 1, len(X)):
            if X[j] < X[min_index]:
                min_index = j

        X[i], X[min_index] = X[min_index], X[i]

A = [1, 3, 9, 5, 4, 0]
selection_sort(A)
print("Sorted list =", A)

"""
    Sorted list = [0, 1, 3, 4, 5, 9]
"""
```
