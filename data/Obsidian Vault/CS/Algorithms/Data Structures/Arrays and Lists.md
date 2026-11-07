## 1. Overview

Arrays and lists are fundamental data structures used to store and access collections of elements.

They appear in almost every software application, from processing API responses to managing database records and implementing more advanced algorithms.

Understanding them requires more than knowing their Java syntax. You should understand:

- How elements are stored in memory.
- Why some operations are \(O(1)\) and others are \(O(n)\).
- The differences between arrays, `ArrayList`, and `LinkedList`.
- How to select the appropriate structure for practical problems.
    
## 2. Arrays

An array stores a fixed number of elements, accessible through numerical indexes.

```java
int[] numbers = {10, 20, 30, 40, 50};

System.out.println(numbers[0]); // 10
System.out.println(numbers[3]); // 40
```

Each element has an index. Accessing numbers[2] directly returns 30.

### Why is array access O(1)?

An array allows direct access to elements using an index.  
Conceptually, the memory address is calculated as:  

```
address(i) = baseAddress + i * elementSize
```

For primitive arrays such as `int[]`, this is a useful simplified model of contiguous storage.  
The JVM can locate the requested element without scanning preceding elements.  

Therefore:

- Accessing the first element: \(O(1)\)
- Accessing the middle element: \(O(1)\)
- Accessing the last element: \(O(1)\)
    
### Complete implementation: Array operations

```java
package example.datastructures;

import java.util.Arrays;

public class ArrayExample {

    public static void main(String[] args) {
        int[] numbers = {10, 20, 30, 40, 50};

        System.out.println("Third: " + numbers[2]);
        System.out.println("Index: " + search(numbers, 40));
        System.out.println("Maximum: " + max(numbers));

        int[] inserted = insert(numbers, 2, 25);
        System.out.println(Arrays.toString(inserted));

        int[] removed = remove(inserted, 3);
        System.out.println(Arrays.toString(removed));
    }

    // O(n) time, O(1) extra space
    public static int search(int[] arr, int target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) {
                return i;
            }
        }
        return -1;
    }

    // O(n) time, O(1) extra space
    public static int max(int[] arr) {
        if (arr.length == 0) {
            throw new IllegalArgumentException("Empty array");
        }

        int result = arr[0];

        for (int i = 1; i < arr.length; i++) {
            result = Math.max(result, arr[i]);
        }

        return result;
    }

    // O(n) time, O(n) extra space
    public static int[] insert(
            int[] arr, int index, int value) {

        if (index < 0 || index > arr.length) {
            throw new IndexOutOfBoundsException(index);
        }

        int[] result = new int[arr.length + 1];

        System.arraycopy(
	        arr, 0, 
	        result, 0, 
	        index
	    );
        
        result[index] = value;
        
        System.arraycopy(
            arr, index, 
            result, index + 1,
            arr.length - index
        );

        return result;
    }

    // O(n) time, O(n) extra space
    public static int[] remove(int[] arr, int index) {
        if (index < 0 || index >= arr.length) {
            throw new IndexOutOfBoundsException(index);
        }

        int[] result = new int[arr.length - 1];

        System.arraycopy(arr, 0, result, 0, index);
        System.arraycopy(
            arr, index + 1, result, index,
            arr.length - index - 1
        );

        return result;
    }
}
```

Output:

```
Third: 30
Index: 3
Maximum: 50
[10, 20, 25, 30, 40, 50]
[10, 20, 25, 40, 50]
```

Notice that inserting or removing elements creates a new array because Java arrays have fixed lengths.

## 3. Dynamic arrays — ArrayList

An `ArrayList` is a resizable array implementation.

Unlike a regular array, it automatically manages its internal capacity.

```java
List<Integer> numbers = new ArrayList<>();

numbers.add(10);
numbers.add(20);
numbers.add(30);

numbers.add(1, 15);

System.out.println(numbers);
// [10, 15, 20, 30]
```

Internally, an `ArrayList` maintains an array of references. When more capacity is needed, it allocates a larger array and copies existing references.

Adding another element uses existing capacity. Once full, the internal array must grow.

### Why is append amortized O(1)?

Most calls to `add()` insert an element into an available position, costing \(O(1)\).

Occasionally, the backing array needs to grow, requiring \(O(n)\) copying.

However, capacity grows geometrically, so expensive copies happen infrequently. Across many appends, the average cost per operation is constant.

This is called amortized O(1).

### Complete implementation: Dynamic array

Let's implement a simplified `ArrayList` ourselves.

```java
package example.datastructures;

import java.util.Arrays;

public class DynamicArrayExample {    
    public static void main(String[] args) {
        DynamicArray numbers = new DynamicArray();

        numbers.add(10);
        numbers.add(20);
        numbers.add(30);
        System.out.println(numbers);  // [10, 20, 30]

        numbers.insert(1, 15);
        System.out.println(numbers);  // [10, 15, 20, 30]

        numbers.remove(0);
        System.out.println(numbers);  // [15, 20, 30]
        System.out.println(numbers.size()); // 3
        System.out.println(numbers.get(2));  // 30
    }
}

class DynamicArray {

    private int[] data = new int[2];
    private int size = 0;
    
    // Add
    public void add(int value) {
        ensureCapacity();
        data[size++] = value;
    }

    private void ensureCapacity() {
        if (size == data.length) {
            data = Arrays.copyOf(data, data.length * 2);
        }
    }

    @Override 
    public String toString() {
        return Arrays.toString(Arrays.copyOf(data, size));
    }


    // Insert at index
    public void insert(int index, int value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(index);
        }

        ensureCapacity();

        System.arraycopy(
            data, index, data, index + 1, size - index
        );

        data[index] = value;
        size++;
    }

    // Remove from index
    public int remove(int index) {
        checkIndex(index);

        int removed = data[index];

        System.arraycopy(
            data, index + 1,
            data, index,
            size - index - 1
        );

        size--;
        data[size] = 0;

        return removed;
    }

    public void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(index);
        }
    }

    public int size() {
        return size;
    }

    public int get(int index) {
        checkIndex(index);
        return data[index];
    }
}
```

Output:

```
[10, 15, 20, 30]
Third: 20
[15, 20, 30]
Size: 3
```

### Complexity analysis

|Operation|Time|
|---|---|
|`get(index)`|O(1)|
|`set(index)`|O(1)|
|`add(value)`|O(1) amortized|
|`insert(index)`|O(n)|
|`remove(index)`|O(n)|
|`size()`|O(1)|

Space complexity is \(O(n)\), where \(n\) is the number of stored elements. The capacity may exceed the size.

## 4. LinkedList

A linked list stores elements in separate nodes, each containing references to other nodes.

Java's `LinkedList` is doubly linked.

Unlike an `ArrayList`, a `LinkedList` does not support constant-time access by numerical index.

To retrieve element 500, it must traverse nodes from the head or tail.

### Complete implementation: LinkedList usage

```java
package example.datastructures;

import java.util.LinkedList;

public class LinkedListExample {

    public static void main(String[] args) {

        LinkedList<String> tasks = new LinkedList<>();

        tasks.addLast("Import CSV");
        tasks.addLast("Validate records");
        tasks.addLast("Save to database");

        // High-priority task
        tasks.addFirst("Check authentication");

        while (!tasks.isEmpty()) {
            String task = tasks.removeFirst();
            System.out.println("Processing: " + task);
        }
    }
}
```

Output:

```
Processing: Check authentication
Processing: Import CSV
Processing: Validate records
Processing: Save to database
```

Adding or removing nodes at either end takes \(O(1)\).

However, accessing a node by index takes \(O(n)\).

For FIFO queues, Java's `ArrayDeque` is usually a better default than `LinkedList` because it generally has lower memory overhead and better locality.

## 5. Array vs ArrayList vs LinkedList

|Operation|Array|ArrayList|LinkedList|
|---|---|---|---|
|Indexed access|O(1)|O(1)|O(n)|
|Search|O(n)|O(n)|O(n)|
|Append|Fixed size|O(1)*|O(1)|
|Insert middle|O(n)**|O(n)|O(n)|
|Remove middle|O(n)**|O(n)|O(n)|
|Memory overhead|Low|Low–medium|Higher|

* Amortized. ** Requires copying or shifting; a fixed-size array cannot change its length. LinkedList middle insertion/removal is O(1) once the node is located, but finding it by index is O(n).

General recommendation: Prefer `ArrayList` for most application-level collections requiring indexed access. Use arrays for fixed-size or performance-sensitive data, and linked lists when their specific node-based behavior is useful.

## 6. Practical example — Package and box compatibility

Suppose a shipping application receives a list of packages and available boxes.

Each package has three dimensions:

- Width
- Height
- Depth
    
The goal is to find every box capable of containing each package, allowing rotation.

This is a simplified three-dimensional fitting problem, without packing multiple packages together.

### Algorithm

1. Store packages and boxes in lists.
2. Sort the three dimensions of each object.
3. Compare corresponding sorted dimensions.
4. Report compatible combinations.
    
Sorting dimensions handles all six axis-aligned rotations of a rectangular package.

### Complete Java 21 implementation

```java
package example.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PackageBoxMatching {
    
    record Package(int width, int height, int depth) {}

    record Box(int width, int height, int depth) {}

    public static void main(String[] args) {
    
        List<Package> packages = List.of(
            new Package(10, 10, 5),
            new Package(20, 15, 10),
            new Package(30, 20, 15)
        );

        List<Box> boxes = List.of(
            new Box(12, 12, 8),
            new Box(25, 20, 15),
            new Box(40, 30, 20)
        );

        for (Package p : packages) {
            List<Box> matches = findCompatibleBoxes(p, boxes);

            System.out.println(p + " fits in:");

            for (Box b : matches) {
                System.out.println(" " + b);
            }
        }
    }

    static List<Box> findCompatibleBoxes(Package pkg, List<Box> boxes) {
        List<Box> result = new ArrayList<>();

        int[] p = sortedDimensions(
            pkg.width(),
            pkg.height(),
            pkg.depth()
        );

        for (Box box : boxes) {
            int[] b = sortedDimensions(
                box.width(),
                box.height(),
                box.depth()
            );

            if(p[0] <= b[0] && p[1] <= b[1] && p[2] <= b[2]) {
               result.add(box);                
            } 
        }

        return result;
    } 

    static int[] sortedDimensions(int width, int height, int depth) {
        int[] dimensions = {width, height, depth};
        Arrays.sort(dimensions);
        return dimensions;
    }
}
```

Output:

```shell
Package[width=10, height=10, depth=5] fits in:
 Box[width=12, height=12, depth=8]
 Box[width=25, height=20, depth=15]
 Box[width=40, height=30, depth=20]

Package[width=20, height=15, depth=10] fits in:
 Box[width=25, height=20, depth=15]
 Box[width=40, height=30, depth=20]

Package[width=30, height=20, depth=15] fits in:
 Box[width=40, height=30, depth=20]
```
### Complexity analysis

Let:

- \(P\) = number of packages
- \(B\) = number of boxes
    
For each package, the algorithm checks every box.

```java
T(P,B) = O(P * B)
```

Sorting three dimensions is constant time because the number of dimensions is fixed.

For a single package, the matching method uses \(O(B)\) additional space in the worst case to store all compatible boxes.

### Practical improvement

If the application contains millions of boxes, scanning all boxes for every package becomes expensive.

Possible optimizations include:

- Precomputing sorted dimensions for each box.
- Filtering by minimum dimensions.
- Using spatial indexes for multidimensional queries.
- Using database indexes to reduce candidate sets.
    
These ideas will become important when studying searching algorithms and spatial data structures.

## 7. Practical example — API batch processing

Suppose a backend application receives 10,000 records from an external API.

Instead of sending all records to the database at once, we divide them into batches of 500.

```java
package example.datastructures;

import java.util.ArrayList;
import java.util.List;

public class BatchProcessingExample {

    public static void main(String[] args) {

        List<Integer> records = new ArrayList<>();

        for (int i = 1; i <= 10_000; i++) {
            records.add(i);
        }

        processInBatches(records, 500);
    }

    public static <T> void processInBatches(
            List<T> records, int batchSize) {

        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                "Batch size must be positive"
            );
        }

        for (int start = 0;
                start < records.size();
                start += batchSize) {

            int end = (int) Math.min(
                (long) start + batchSize,
                records.size()
            );

            List<T> batch = records.subList(start, end);

            System.out.println(
                "Processing batch of " + batch.size()
            );

            // repository.saveAll(batch);
        }
    }
}
```

Output:

```sh
Processing batch of 500
Processing batch of 500
...
```

`subList()` creates a view backed by the original list rather than copying its elements.

For an ArrayList, constructing each view takes \(O(1)\), and iterating over all records takes \(O(n)\).

The batching loop therefore has:

- Time: \(O(n)\), assuming constant work per record.
- Extra space: \(O(1)\) for the list views, excluding any processing allocations.
    
Be careful: a `subList` is not an independent collection. Structural changes to the original list while using the view can invalidate it.

In real Spring Boot applications, batching can reduce database round trips and control memory consumption, although actual persistence costs depend on the database and JPA configuration.

## 8. When to use each structure

|Scenario|Recommended|
|---|---|
|Fixed-size numeric data|`int[]`, `double[]`|
|REST API response collection|`ArrayList`|
|Database result processing|`ArrayList`|
|Random indexed access|Array or `ArrayList`|
|FIFO processing queue|`ArrayDeque`|
|Frequent operations at both ends|`ArrayDeque`|
|Frequent node insertion with an existing iterator|`LinkedList`|

Java 21 also introduces the `SequencedCollection` API, which provides consistent first/last element operations and reversed views for supported collections.

For example:

```java
List<String> tasks = new ArrayList<>(
    List.of("Import", "Validate", "Save")
);

System.out.println(tasks.getFirst()); // Import
System.out.println(tasks.getLast());  // Save
System.out.println(tasks.reversed());
// [Save, Validate, Import]
```

## 9. Common mistakes

**1. Assuming ArrayList insertion is always O(1).**

Appending is amortized \(O(1)\), but inserting at the beginning requires shifting elements, making it \(O(n)\).

**2. Assuming LinkedList is always faster for insertion.**

Finding the insertion position may require \(O(n)\) traversal. `ArrayList` can be faster in practice because of memory locality.

**3. Confusing size and capacity.**

`ArrayList.size()` returns the number of stored elements, not the backing array capacity.

**4. Modifying a collection during enhanced-for iteration.**

Structural modifications can cause `ConcurrentModificationException`. Use an iterator's supported removal method or `removeIf()` when appropriate.

**5. Ignoring primitive boxing.**

`ArrayList<Integer>` stores references to `Integer` objects, while `int[]` stores primitive integers. This can make a substantial memory difference for large numeric datasets.

## 10. Key takeaways

- Arrays provide fixed-size storage and \(O(1)\) indexed access.
- `ArrayList` uses a dynamically resized array, offering amortized \(O(1)\) append operations.
- `LinkedList` uses nodes and supports \(O(1)\) insertion or removal at its ends.
- Middle insertions and removals often require \(O(n)\) work.
- Algorithmic complexity depends on both the operation and the data structure.
- In everyday Java backend development, `ArrayList` is usually the preferred general-purpose list implementation.