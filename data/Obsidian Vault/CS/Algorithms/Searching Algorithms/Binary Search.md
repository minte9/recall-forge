## 1. What is binary search?

Binary search is an efficient searching algorithm that works on sorted data.

Instead of checking every element sequentially, binary search repeatedly divides the search interval in half.

For example, consider:

```
Index:  0   1   2   3   4   5   6
Value: [3,  7, 11, 15, 19, 23, 27]
```

To find `19`:

1. Examine the middle element `15`.
2. Since `19 > 15`, discard the left half, including `15`.
3. Examine the middle of the remaining interval: `23`.
4. Since `19 < 23`, search the left part.
5. Find `19`.
    
Only 3 comparisons, instead of up to 7 with linear search.  

At each step, the search interval becomes approximately half its previous size.  

## 2. Iterative Binary Search

This is the implementation to understand first.

```java
package example.searching;

public class BinarySearch {
    public static void main(String[] args) {
        int[] numbers = {
            3, 7, 11, 15, 19, 23, 27  // sorted
        };

        System.out.println(search(numbers, 19)); // 4
        System.out.println(search(numbers, 7));  // 1
        System.out.println(search(numbers, 30)); // -1
    }    

    public static int search(int[] arr, int target) {

        int left = 0;
        int right = arr.length - 1;

        while (left <= right) { // iterative
			
			// Avoid potential integer overflow
            int mid = left + (right - left) / 2;

            if (arr[mid] == target) {
                return mid;
            }

            if (arr[mid] < target) {
                // Search the right half
                left = mid + 1;
            } else {
                // Search the left half
                right = mid - 1;
            }
        }

        return -1;
    }
}
```

### Understanding the variables

|Variable|Purpose|
|---|---|
|`left`|First index in the current search interval|
|`right`|Last index in the current search interval|
|`mid`|Middle index|
|`target`|Value being searched|

The condition `left <= right` is important because the interval can contain exactly one remaining element.

### Why calculate `mid` this way?

```java
int mid = left + (right - left) / 2;
```

You might see:

```java
int mid = (left + right) / 2;
```

However, adding two large positive integers could overflow Java's `int` range.

In Java, `int` is a 32-bit signed integer, with a range of:

| Property  | Value                                    |
| --------- | ---------------------------------------- |
| Minimum   | `-2,147,483,648`                         |
| Maximum   | `2,147,483,647`                          |
| Size      | 32 bits (4 bytes)                        |
| Constants | `Integer.MIN_VALUE`, `Integer.MAX_VALUE` |

The first version avoids this problem when `left` and `right` are valid nonnegative array indices.

### Step-by-step execution

Consider searching for `19` in:

```
[3, 7, 11, 15, 19, 23, 27]
```

|Iteration|left|right|mid|arr[mid]|Action|
|---|---|---|---|---|---|
|1|0|6|3|15|Move right|
|2|4|6|5|23|Move left|
|3|4|4|4|19|Found|

Notice that the algorithm never examines elements `3`, `7`, `11`, or `27`.

It eliminates them using the sorted ordering.

## 3. Recursive Binary Search

Binary search can also be implemented recursively.

```java
public static int binarySearch(
	int[] arr,
	int target,
	int left,
	int right) {

	// Base case: empty search interval
	if (left > right) {
		return -1;
	}    

	int mid = left + (right - left) / 2;

	if (arr[mid] == target) {
		return mid;
	}

	if (arr[mid] < target) {
		return binarySearch(arr, target, mid + 1, right);
	}

	return binarySearch(arr, target, left, right - 1);
	
}
```

Usage:

```java
int[] numbers = {3, 7, 11, 15, 19, 23, 27};

int index = binarySearch(
    numbers,
    19,
    0,
    numbers.length - 1
);

System.out.println(index); // 4
```

The recursive implementation has the same time complexity but consumes additional call-stack memory.

### Complexity analysis

|   |   |
|---|---|
|Best-case time|O(1)|
|Average time|O(log n)|
|Worst-case time|O(log n)|
|Iterative space|O(1)|
|Recursive space|O(log n)|

Worst-case comparison growth:

![[Pasted image 20261110150242.png|640]]

Approximate worst-case comparisons for successful or unsuccessful searches.

For 1,000,000 elements, binary search needs at most about 20 comparisons in the worst case, assuming a sorted array.

The key trade-off: binary search requires sorted data and efficient indexed access.


## 4. Boundary conditions and duplicates

A standard binary search returns an index where the target exists, but not necessarily the first or last occurrence.

Consider:

```
Index:  0  1  2  3  4  5  6
Value: [2, 4, 4, 4, 4, 7, 9]
```

Searching for `4` might return index `3`, even though its first occurrence is at index `1`.

We can solve this using lower and upper bounds.

### Lower bound: first position with value >= target

```java
public static int lowerBound(int[] arr, int target) {

    int left = 0;
    int right = arr.length;

    while (left < right) {
        int mid = left + (right - left) / 2;

        if (arr[mid] < target) {
            left = mid + 1;
        } else {
            right = mid;
        }
    }

    return left;
}
```

### Upper bound: first position with value > target

```java
public static int upperBound(int[] arr, int target) {

    int left = 0;
    int right = arr.length;

    while (left < right) {
        int mid = left + (right - left) / 2;

        if (arr[mid] <= target) {
            left = mid + 1;
        } else {
            right = mid;
        }
    }

    return left;
}
```

Example:

```java
package example.searching;

public class BinarySearch {
    public static void main(String[] args) {
        int[] numbers = {2, 4, 4, 4, 4, 7, 9};

        System.out.println(lowerBound(numbers, 4)); // 1
        System.out.println(upperBound(numbers, 4)); // 5

        // Number of occurrences of 4
        int count = upperBound(numbers, 4)
                - lowerBound(numbers, 4);

        System.out.println(count); // 4
    }    

    public static int lowerBound(int[] arr, int target) {

        int left = 0;
        int right = arr.length;

        while (left < right) {
            int mid = left + (right - left) / 2;

            if (arr[mid] < target) { // Look here
                left = mid + 1;
            } else {
                right = mid;
            }
        }

        return left;
    }

    public static int upperBound(int[] arr, int target) {

        int left = 0;
        int right = arr.length;

        while (left < right) {
            int mid = left + (right - left) / 2;

            if (arr[mid] <= target) { // Look here
                left = mid + 1;
            } else {
                right = mid;
            }
        }

        return left;
    }
}
```

Both methods return insertion boundaries between `0` and `arr.length`, even if the target is absent.

## 5. Practical application: finding the first eligible price

Imagine a backend service that stores sorted shipping prices and needs to find the cheapest option meeting a minimum price threshold.

```java
public class ShippingPriceSearch {

    public static int findFirstEligible(
            int[] prices,
            int minimum) {

        int index = lowerBound(prices, minimum);

        if (index == prices.length) {
            return -1;
        }

        return prices[index];
    }

    private static int lowerBound(
            int[] arr,
            int target) {

        int left = 0;
        int right = arr.length;

        while (left < right) {
            int mid = left + (right - left) / 2;

            if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }

        return left;
    }

    public static void main(String[] args) {

        int[] prices = {10, 15, 20, 35, 50, 75};

        System.out.println(
            findFirstEligible(prices, 22)
        ); // 35
    }
}
```

This finds the smallest price greater than or equal to the threshold in O(log n) time.

Other software engineering applications include searching sorted timestamps, finding the first log event after a given time, looking up version boundaries, and finding insertion positions in sorted collections.

## 6. Java standard library

Java already provides binary search: `Arrays.binarySearch()`

```java
package example.searching;

import java.util.Arrays;

public class BinarySearchExample {
    public static void main(String[] args) {
        int[] numbers = {3, 7, 11, 15, 19, 23};

        int index = Arrays.binarySearch(numbers, 19);
        System.out.println(index);  // 4

        int missing = Arrays.binarySearch(numbers, 20);
        System.out.println(missing);  // -6
    }
}
```

When the value is absent, Java returns:

```
-insertionPoint - 1
```

Here, `20` would be inserted at index `5`, so the result is `-6`.

Important: `Arrays.binarySearch()` assumes the array is sorted. With duplicate values, it does not guarantee which matching index is returned.

## 7. Common mistakes

| Mistake                                    | Consequence                   |
| ------------------------------------------ | ----------------------------- |
| Searching an unsorted array                | Incorrect results             |
| Using `left < right` in standard search    | May skip the final element    |
| Updating `left = mid`                      | Possible infinite loop        |
| Updating `right = mid` in standard search  | Possible infinite loop        |
| Assuming duplicates return the first index | Incorrect boundary logic      |
| Using binary search on a `LinkedList`      | Indexed access is inefficient |

Notice that `right = mid` is correct in our lower/upper-bound implementations because they use a different interval convention: `[left, right)`.

## 8. Key takeaways

- Binary search reduces the search interval by approximately half at each step.
- Its time complexity is O(log n), compared with O(n) for linear search.
- The input must be sorted according to the search comparison.
- Iterative binary search uses O(1) extra memory.
- Lower and upper bounds handle duplicates, ranges, and insertion positions.
- Binary search is useful beyond exact-value lookup: it can locate boundaries in any monotonic condition.
