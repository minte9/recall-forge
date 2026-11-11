## 1. Simple Search

Linear search is the simplest searching algorithm. It examines elements sequentially until the target is found or the collection is exhausted.

```java
package example.searching;

public class LinearSearch {

    public static int search(int[] numbers, int target) {
        for (int i = 0; i < numbers.length; i++) {
            if (numbers[i] == target) {
                return i;
            }
        }

        return -1;
    }

    public static void main(String[] args) {
        int[] numbers = {12, 7, 25, 9, 18, 31, 4};

        System.out.println(search(numbers, 18)); // 4
        System.out.println(search(numbers, 50)); // -1
    }
}
```

### Complexity analysis

|Case|Time|Explanation|
|---|---|---|
|Best|O(1)|First element matches|
|Average|O(n)|Examine a linear number of elements|
|Worst|O(n)|Last element or not found|
|Extra space|O(1)|No additional data structure|

## 2. Practical applications

In backend development, linear search is useful when searching small collections, filtering in-memory objects, or checking elements where no index exists.

For large datasets, database indexes, hash-based lookup, or binary search on sorted data are often more efficient.