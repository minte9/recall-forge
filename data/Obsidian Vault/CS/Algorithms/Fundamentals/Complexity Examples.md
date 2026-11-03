## 1. Recognizing Complexity in Practice

Understanding Big O notation is important, but the practical skill is recognizing an algorithm's complexity by examining its code.

The most common patterns are:

|Complexity|Typical pattern|Example|
|---|---|---|
|O(1)|Direct access|Array indexing|
|O(log n)|Repeatedly halve input|Binary search|
|O(n)|Single traversal|Find maximum|
|O(n log n)|Divide and process|Merge sort|
|O(n²)|Nested traversal|Compare all pairs|

These patterns are useful starting points, but actual complexity depends on how many times each operation executes.

## 2. O(1) — Constant Time

An algorithm has constant time complexity when its number of operations does not grow with input size.

```java
public int getLast(int[] numbers) {
    return numbers[numbers.length - 1];
}
```

Whether the array contains 10 or 10 million elements, retrieving the last element requires one indexed access.

Time: O(1)  
Auxiliary space: O(1)

Another example is accessing a `HashMap` entry, which has expected O(1) lookup time under normal hashing assumptions, but not guaranteed O(1) worst-case time.

## 3. O(log n) — Logarithmic Time

Logarithmic algorithms repeatedly reduce the remaining problem by a constant factor.

Binary search is the classic example:

```java
public int binarySearch(int[] arr, int target) {
    int left = 0;
    int right = arr.length - 1;

    while (left <= right) {
        int mid = left + (right - left) / 2;

        if (arr[mid] == target) {
            return mid;
        }

        if (arr[mid] < target) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }

    return -1;
}
```

The input array must be sorted.

Binary search visualization:

![[Pasted image 20261103151912.png]]

For one million sorted elements, binary search requires at most about 20 comparisons.

Time: O(log n) worst case  
Auxiliary space: O(1)

## 4. O(n) — Linear Time

Linear algorithms perform work proportional to the number of input elements.

```java
public int findMaximum(int[] numbers) {
    if (numbers.length == 0) {
        throw new IllegalArgumentException("Empty array");
    }

    int max = numbers[0];

    for (int i = 1; i < numbers.length; i++) {
        if (numbers[i] > max) {
            max = numbers[i];
        }
    }

    return max;
}
```

Each element is examined once. If the input size doubles, the number of comparisons approximately doubles.

Time: Θ(n)  
Auxiliary space: O(1)

A common mistake is assuming two sequential loops mean O(n²):

```java
for (int i = 0; i < n; i++) {
    processA(i);
}

for (int i = 0; i < n; i++) {
    processB(i);
}
```

Assuming both processing functions are constant-time, the total work is 2n, which simplifies to O(n).

## 5. O(n log n) — Linearithmic Time

Many efficient comparison-based sorting algorithms have O(n log n) time complexity.

Merge sort divides an array into smaller parts, sorts them, and combines the results.

![[Pasted image 20261103152049.png]]

At each level of recursion, merging all the elements takes O(n) work. There are approximately log₂(n) levels.

```java
T(n) = 2T(n/2) + O(n) = O(n log n)
```

Time: Θ(n log n)  
Auxiliary space: O(n) for a typical array-based implementation.

We will implement merge sort in the Sorting Algorithms subcategory.

## 6. O(n²) — Quadratic Time

Quadratic complexity commonly appears when comparing every element with every other element.

```java
public boolean hasDuplicate(int[] numbers) {
    for (int i = 0; i < numbers.length; i++) {
        for (int j = i + 1; j < numbers.length; j++) {
            if (numbers[i] == numbers[j]) {
                return true;
            }
        }
    }

    return false;
}
```

In the worst case, the number of comparisons is:

```java
(n-1) + (n-2) + ... + 1 = n(n-1)/2
```

Therefore, the worst-case time complexity is O(n²).  
The best case is O(1) if the first pair contains duplicates.

### Improving the algorithm with a HashSet

```java
public boolean hasDuplicate(int[] numbers) {
    Set<Integer> seen = new HashSet<>();

    for (int number : numbers) {
        if (!seen.add(number)) {
            return true;
        }
    }

    return false;
}
```

Using a hash set reduces the expected time complexity to O(n), at the cost of O(n) additional memory.

This demonstrates an important engineering trade-off: using more memory can significantly reduce execution time.

## 7. Comparing Growth Rates

Estimated operations by input size

| Complexity | Elements  |
| ---------- | --------- |
| O(1)       | 1         |
| O(log n)   | 10        |
| O(n)       | 1,000     |
| O(n log n) | 9,966     |
| O(n²)      | 1,000,000 |

At 10,000 elements, a quadratic algorithm may require around 100 million operations, compared with approximately 133,000 for an n log n algorithm.

However, Big O alone does not determine actual runtime. JVM overhead, memory access patterns, and constant factors also matter.

## 8. Practical Application — Matching Packages to Boxes

Suppose a warehouse has `n` packages and `m` available box types.

A simple algorithm checks every package against every box type:

```java
package example.complexity;

import java.util.List;

public class FindCompatibleBoxes {
    
    public static void main(String[] args) {
        List<Package> packages = List.of(
            new Package(10, 10, 1),
            new Package(20, 20, 2)
        );

        List<Box> boxes = List.of(
            new Box(15, 15, 3),
            new Box(25, 25, 4)
        );

        findCompatibleBoxes(packages, boxes);
    }

    public static void findCompatibleBoxes(
            List<Package> packages,
            List<Box> boxes) {

        for (Package p : packages) {
            for (Box b : boxes) {
                if (p.width() <= b.width() &&
                    p.height() <= b.height() && 
                    p.depth() <= b.depth()) {
                    
                    System.out.println(
                        "Compatible box found: " + p + " " + b
                    );
                }
            }
        }
        
    }
}

record Package(
    int width,
    int height,
    int depth
) {}

record Box (
    int width,
    int height,
    int depth
) {}

// Package[width=10, height=10, depth=1] Box[width=15, height=15, depth=3]
// Package[width=10, height=10, depth=1] Box[width=25, height=25, depth=4]
// Package[width=20, height=20, depth=2] Box[width=25, height=25, depth=4]
```

This has O(n × m) time complexity, assuming each dimension comparison is constant-time.

For 10,000 packages and 100 box types, the algorithm performs up to one million compatibility checks.

This example checks whether individual packages fit inside individual boxes in a fixed orientation. It does not yet solve the more difficult problem of packing multiple packages into a single box.

Later, we can improve the matching algorithm using sorting, indexing, and spatial data structures.

## 9. Key Takeaways

- O(1) algorithms perform a constant amount of work.
- O(log n) algorithms often reduce the problem size repeatedly.
- O(n) algorithms typically traverse the input once.
- O(n log n) algorithms often combine divide-and-conquer with linear processing.
- O(n²) algorithms frequently involve nested comparisons.
- Choosing a suitable data structure can improve time complexity, sometimes at the cost of additional memory.