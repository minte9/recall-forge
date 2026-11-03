## 1. What is Complexity Analysis?

Complexity analysis measures how an algorithm's resource requirements grow as the input size increases. The two primary resources are execution time and memory.

- Time complexity describes how the number of operations grows with input size.
- Space complexity describes how much memory an algorithm requires as input size grows.
    
Rather than measuring execution time in milliseconds, complexity analysis focuses on growth rates. Actual execution time depends on hardware, JVM optimizations, and other factors.

For example, searching an unsorted array of 1,000 elements may require examining all 1,000 elements. Searching an array of 1,000,000 elements may require 1,000,000 examinations. Both operations have O(n) time complexity.

## 2. Asymptotic Notation

Three mathematical notations describe algorithm complexity.

|Notation|Meaning|Interpretation|
|---|---|---|
|Big O — O(f(n))|Asymptotic upper bound|Growth is no faster than f(n), up to a constant factor|
|Big Omega — Ω(f(n))|Asymptotic lower bound|Growth is at least f(n), up to a constant factor|
|Big Theta — Θ(f(n))|Asymptotically tight bound|Growth is bounded above and below by f(n)|

For example, consider a loop that processes every element of an array:

```java
for (int number : numbers) {
    System.out.println(number);
}
```

For an array of size `n`, the loop performs `n` iterations. Its iteration count is O(n), Ω(n), and Θ(n).

Big O is commonly used to describe worst-case complexity, but Big O does not inherently mean worst case. It is a mathematical upper bound that can describe worst-case, average-case, or best-case behavior.

## 3. Common Complexity Classes

How operation counts grow with input size

![[Pasted image 20261103135934.png]]

Typical examples:

|Complexity|Algorithm|
|---|---|
|O(1)|Accessing an array element by index|
|O(log n)|Binary search|
|O(n)|Linear search|
|O(n log n)|Merge sort|
|O(n²)|Bubble sort|
|O(2ⁿ)|Enumerating all subsets|
|O(n!)|Enumerating all permutations|

## 4. Analyzing Java Code

### Example A — Constant time: O(1)

```java
public int first(int[] numbers) {
    return numbers[0];
}
```

The algorithm accesses one element regardless of the array size. Therefore, time complexity is O(1), assuming a nonempty array.

### Example B — Linear time: O(n)

```java
public int sum(int[] numbers) {
    int total = 0;

    for (int number : numbers) {
        total += number;
    }

    return total;
}
```

The loop processes each element exactly once. Doubling the input size approximately doubles the number of loop operations.

Time: Θ(n). Auxiliary space: O(1).

### Example C — Quadratic time: O(n²)

```java
public void printPairs(int[] numbers) {
    for (int i = 0; i < numbers.length; i++) {
        for (int j = 0; j < numbers.length; j++) {
            System.out.println(numbers[i] + ", " + numbers[j]);
        }
    }
}
```

For `n` elements, the outer loop executes `n` times, and the inner loop executes `n` times for every outer iteration.

Total iterations:  n * n = n^2

Time complexity is Θ(n²).

### Example D — Logarithmic time: O(log n)

```java
public int binarySearch(int[] arr, int target) {
    int left = 0;
    int right = arr.length - 1;

    while (left <= right) {
        int mid = left + (right - left) / 2;

        if (arr[mid] == target) return mid;

        if (arr[mid] < target) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }

    return -1;
}
```

Binary search requires a sorted array. Each iteration eliminates approximately half the remaining elements.

For one million elements, binary search requires at most about 20 comparisons in the worst case, compared with up to one million for linear search.

Time: O(log n) worst case. Auxiliary space: O(1).

## 5. Rules for Calculating Complexity

When analyzing algorithms, follow these rules:

1. Sequential operations add: O(n) + O(n) = O(n), after removing constant factors.
2. Nested loops multiply: Two nested loops of size `n` typically produce O(n²).
3. Drop constants: O(3n + 5) simplifies to O(n).
4. Keep the dominant term: O(n² + n + 1) simplifies to O(n²).
5. Consider different inputs: A loop over `n` items nested inside a loop over `m` items has O(n × m) complexity.
   
These simplifications describe asymptotic growth. Constants can still matter substantially for small inputs and real production performance.

## 6. Space Complexity

Consider two approaches to reversing an array.

Approach A — Allocate a new array

```java
public int[] reverseCopy(int[] input) {
    int n = input.length;
    int[] result = new int[n];

    for (int i = 0; i < n; i++) {
        result[i] = input[n - 1 - i];
    }

    return result;
}
```

Time: O(n). Auxiliary space: O(n).

Approach B — Reverse in place

```java
public void reverseInPlace(int[] arr) {
    int left = 0;
    int right = arr.length - 1;

    while (left < right) {
        int temp = arr[left];
        arr[left] = arr[right];
        arr[right] = temp;

        left++;
        right--;
    }
}
```

Time: O(n). Auxiliary space: O(1).

Both approaches have the same asymptotic time complexity, but the second requires no additional array. It does, however, modify the original input.

## 7. Practical Application — Finding Routes Between Cities

Suppose we represent a road network with `V` cities and `E` roads.

Different shortest-path approaches have different complexity characteristics:

|Approach|Typical time complexity|
|---|---|
|Exhaustively enumerate simple paths|Exponential or worse|
|BFS (unweighted graph)|O(V + E)|
|Dijkstra with binary heap|O((V + E) log V)|
|Floyd–Warshall (all pairs)|O(V³)|

For a large road network, an exhaustive approach is usually impractical. Dijkstra's algorithm can efficiently find shortest paths with nonnegative edge weights. A* can often improve practical search performance when an appropriate heuristic is available.

This illustrates why complexity analysis matters: it helps us choose an algorithm before investing time in implementation.