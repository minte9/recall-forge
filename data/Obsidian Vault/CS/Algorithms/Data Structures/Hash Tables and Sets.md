## 1. Overview

Hash tables are data structures designed for fast insertion, lookup, and removal of elements.

Unlike arrays, which access elements using numerical indexes, hash tables use keys to locate values.

In Java 21, the most important implementations are:

- `HashMap<K, V>` — stores key-value pairs.
- `HashSet<E>` — stores unique elements.
- `LinkedHashMap<K, V>` — preserves insertion order or access order.
- `LinkedHashSet<E>` — stores unique elements while preserving insertion order.
    
Hash tables are widely used in software engineering for:

- Detecting duplicate records during imports.
- Looking up objects by identifiers.
- Counting occurrences of events.
- Implementing in-memory caches.
- Grouping records by category.
- Tracking visited nodes in graph algorithms.
    
Their main advantage is average-case O(1) lookup, compared with O(n) for a linear search through an unsorted array.

## 2. How hash tables work

A hash table uses a hash function to transform a key into a numerical value.

The hash value determines where the key should be stored.

Conceptually:

```txt
index = hash(key) mod capacity
```

Java's `HashMap` uses additional hash mixing and, with power-of-two capacities, computes the bucket index using a bit mask.

For example, with capacity 8:

```
hash(42) = 42
42 % 8 = 2
```

The entry is stored in bucket 2.

To retrieve it, the same calculation is performed again.

Instead of searching every element, the hash table goes directly to the appropriate bucket.

## 3. HashMap in Java 21

A `HashMap` stores pairs of keys and values.

```java
Map<String, Integer> scores = new HashMap<>();

scores.put("Java", 90);
scores.put("Python", 85);
scores.put("SQL", 95);

System.out.println(scores.get("Java")); // 90
```

### Core operations

|Operation|Description|Average time|
|---|---|---|
|`put(k, v)`|Insert or update|O(1)|
|`get(k)`|Retrieve value|O(1)|
|`remove(k)`|Remove entry|O(1)|
|`containsKey(k)`|Check existence|O(1)|
|`size()`|Number of entries|O(1)|

These are expected average-case complexities, assuming a suitable hash distribution.

### Complete implementation — Lookup by ID

Consider a backend application that needs to find users by their identifiers.

```java
package example.datastructures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserLookupExample {

    record User(long id, String name, String email) {}

    public static void main(String[] args) {

        List<User> users = List.of(
            new User(101, "Alice", "alice@example.com"),
            new User(102, "Bob", "bob@example.com"),
            new User(103, "Carol", "carol@example.com")
        );

        Map<Long, User> usersById = new HashMap<>();

        for (User user : users) {
            usersById.put(user.id(), user);
        }

        User found = usersById.get(102L);

        if (found != null) {
            System.out.println("Found: " + found.name());
            System.out.println("Email: " + found.email());
        }
    }
}
```

Output:

```
Found: Bob
Email: bob@example.com
```

### Complexity analysis

Let \(n\) be the number of users.

Building the map:

```txt
T(n)=O(n)
```

Looking up a user:

```txt
T_lookup = O(1) average
```

Additional space:

```txt
S(n) = O(n)
```

If you perform many lookups, building a hash-based index can be significantly faster than repeatedly scanning a list.

For example, performing \(m\) lookups:

- List: \(O(mn)\)
- HashMap: \(O(n+m)\) expected, including index construction.

## 4. Hash collisions

A collision occurs when different keys map to the same bucket.

For example, with capacity 8:

```
10 % 8 = 2
18 % 8 = 2
26 % 8 = 2
```

All three keys map to bucket 2.

![[Pasted image 20261107185940.png|463]]

Collision using separate chaining

Different keys can share a bucket. Each entry still retains its original key for equality comparison.

### Collision resolution strategies

Two common approaches are:

Separate chaining: Each bucket stores multiple entries, traditionally using a linked list.

Open addressing: When a bucket is occupied, another location is searched according to a probing strategy.

Java's `HashMap` uses separate chaining. In modern Java, sufficiently large collision chains may be transformed into balanced trees when capacity and key requirements allow.

This helps prevent excessively long linear searches through heavily populated buckets.

### Worst-case complexity

|Situation|Lookup complexity|
|---|---|
|Good hash distribution|O(1) expected|
|Long linked collision chain|O(n)|
|Treeified bucket|O(log n) for suitable keys|

Treeification does not provide an unconditional \(O(\log n)\) guarantee for every possible key type or adversarial input.

## 5. Implementing a hash table from scratch

Let's implement a simplified hash table using separate chaining.

This implementation supports integer keys and string values, including collisions, updates, removals, and automatic resizing.

```java
package example.datastructures.hash_tables;

import java.util.LinkedList;

public class SimpleHashMap {

    public static void main(String[] args) {
        SimpleHashMap map = new SimpleHashMap();

        map.put(10, "Java");
        map.put(18, "Python");
        map.put(26, "SQL");
        map.put(34, "Docker");

        System.out.println(map.get(18));
        System.out.println(map.get(26));

        map.put(18, "Spring Boot");
        System.out.println(map.get(18));

        System.out.println(map.remove(10));
        System.out.println("Size: " + map.size());
    }  

    // Stores a key-value pair
    private static class Entry {
        int key;
        String value;

        Entry(int key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    // Array of bukets; each buckets handles collison using a liked list
    private LinkedList<Entry>[] buckets;
    private int size = 0;

    @SuppressWarnings("unchecked")
    public SimpleHashMap() {
        buckets = new LinkedList[4];
    }
    
    // Convert a key into a valid bucket index
    private int index(int key) {
        return Math.floorMod(key, buckets.length);
    }

    public void put(int key, String value) {
        int index = index(key);

        // Create the bucket if it doesn't exist
        if (buckets[index] == null) {
            buckets[index] = new LinkedList<>();
        }

        // Update the value if the key already exists
        for (Entry entry : buckets[index]) {
            if (entry.key == key) {
                entry.value = value;
                return;
            }
        }

        // Add a new entry (collision handled by the linked list)
        buckets[index].add(new Entry(key, value));
        size++;

        // Double capacity when load factor exceeds 75%
        if (size > buckets.length * 0.75) {
            resize();
        }
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        // Double the number of buckets
        LinkedList<Entry>[] oldBuckets = buckets;
        buckets = new LinkedList[oldBuckets.length * 2];

        // Rehash all entries because bucket indexes may change
        for (LinkedList<Entry> bucket : oldBuckets) {
            if (bucket == null) {
                continue;
            }

            for (Entry entry : bucket) {
                int index = index(entry.key);

                if (buckets[index] == null) {
                    buckets[index] = new LinkedList<>();
                }

                buckets[index].add(entry);
            }
        }
    }

    public String get(int key) {
        LinkedList<Entry> bucket = buckets[index(key)];

        if (bucket == null) {
            return null;
        }

        // Search the bucket for the matching key
        for (Entry entry : bucket) {
            if (entry.key == key) {
                return entry.value;
            }
        }

        return null;
    }

    public String remove(int key) {
        LinkedList<Entry> bucket = buckets[index(key)];

        if (bucket == null) {
            return null;
        }

        // Use an iterator to safely remove entries during traversal
        var iterator = bucket.iterator();

        while (iterator.hasNext()) {
            Entry entry = iterator.next();

            if (entry.key == key) {
                iterator.remove();
                size--;
                return entry.value;
            }
        }

        return null;
    }

    public int size() {
        return size;
    }
}
```

Output:

```
Python
SQL
Spring Boot
Java
Size: 3
```

One important detail in this example: keys `10`, `18`, `26`, and `34` initially all map to bucket `2`, because each gives a remainder of `2` when divided by `4`.

This demonstrates hash collisions. The linked list stores multiple entries in the same bucket, and `resize()` redistributes them when the capacity increases.

### Complexity analysis

Let \(n\) be the number of entries.

- `put()` — expected amortized \(O(1)\).
- `get()` — expected \(O(1)\).
- `remove()` — expected \(O(1)\).
- `resize()` — \(O(n)\).
- Space — \(O(n)\).
    
The implementation is intentionally simplified. It does not support generic types, null-value disambiguation, treeified buckets, or concurrent access.

## 6. Load factor and resizing

The load factor measures how full a hash table is relative to its bucket capacity.

```txt
LoadFactor = numberOfEntries / capacity
```

Java's default `HashMap` load factor is 0.75.

When the number of entries exceeds the resizing threshold, the table grows and entries are redistributed.

A lower load factor reduces collision probability but uses more memory.

A higher load factor saves memory but can increase lookup costs.

For most Java applications, the default is a good choice.

## 7. HashSet — Storing unique elements

A `HashSet` stores unique values without associating separate values with keys.

Internally, Java's `HashSet` uses a hash-based map.

```java
Set<String> technologies = new HashSet<>();

technologies.add("Java");
technologies.add("Python");
technologies.add("Java");

System.out.println(technologies.size()); // 2
```

Duplicate values are ignored.

### Practical example — Detecting duplicate API records

Suppose an external API returns transaction identifiers, possibly containing duplicates.

We want to process each transaction only once.

```java
package example.datastructures.hash_tables;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DuplicateTransactionDetector {

    record Transaction(String id, double amount) {}

    public static void main(String[] args) {
        
        List<Transaction> transactions = List.of(
            new Transaction("TX100", 150.0),
            new Transaction("TX101", 250.0),
            new Transaction("TX100", 150.0),
            new Transaction("TX102", 300.0),
            new Transaction("TX101", 250.0)
        );

        Set<String> seenIds = new HashSet<>();

        for (Transaction tx : transactions) {

            if (!seenIds.add(tx.id())) {
                System.out.println("Duplicate: " + tx.id());
                continue;
            }

            System.out.println("Processing: " + tx.id());
        }
    }
}
```

Output:

```
Processing: TX100
Processing: TX101
Duplicate: TX100
Processing: TX102
Duplicate: TX101
```

### Why use `add()` directly?

`HashSet.add()` returns:

- `true` if the element was newly inserted.
- `false` if the element already existed.
    
This avoids performing a separate `contains()` check.

### Complexity

For \(n\) transactions:

- Time: expected \(O(n)\).
- Space: \(O(n)\) in the worst case.
    

Production consideration: An in-memory set only detects duplicates within the current processing scope. For persistent deduplication, use a database unique constraint or another durable idempotency mechanism. Transaction identifiers should also be scoped appropriately, such as by source system.

## 8. Practical example — Frequency counting

A common algorithmic problem is counting how frequently each value appears.

Examples include:

- HTTP response status codes.
- Application error types.
- Product categories.
- User activity events.
    
### Complete implementation

```java
package example.datastructures.hash_tables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrequencyCounter {

    public static void main(String[] args) {

        // HTTP status codes collected from requests
        List<Integer> statusCodes = List.of(
            200, 200, 404, 200, 500,
            404, 200, 500, 200
        );

        // Map each status code to its occurrence count
        Map<Integer, Integer> counts = new HashMap<>();

        // Count occurrences of each status code
        for (int status : statusCodes) {
            
            // Insert 1 if absent, otherwise increment existing count
            counts.merge(status, 1, Integer::sum);
        }

        // Sort by status code and print the results
        counts.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry ->
                System.out.printf(
                    "HTTP %d: %d requests%n",
                    entry.getKey(),
                    entry.getValue()
                )
            );
    }
}
```

Output:

```
HTTP 200: 5 requests
HTTP 404: 2 requests
HTTP 500: 2 requests
```

The key operation is:

```java
counts.merge(status, 1, Integer::sum);
```

If the key does not exist, its value becomes 1. Otherwise, the existing value is incremented.

### Complexity

Let \(n\) be the number of requests and \(k\) the number of distinct status codes.

- Frequency counting:  expected \(O(n)\).
- Sorting the output:  \(O(k log k)\).
- Space:  \(O(k)\).
    

Total time:

```txt
O(n + k log k)
```

Without sorting, frequency counting alone is expected \(O(n)\).

## 9. Practical example — In-memory LRU cache

An LRU (Least Recently Used) cache removes the least recently accessed entry when capacity is reached.

This is useful for caching:

- Frequently requested API responses.
- Configuration lookups.
- Database query results.
- Computed metadata.
    

Java's `LinkedHashMap` supports access-order iteration, making it convenient for building a simple LRU cache.

### Complete implementation

```java
package example.datastructures.hash_tables;

import java.util.LinkedHashMap;
import java.util.Map;

// LRU cache: removes the least recently used entry when full
public class LruCache<K, V>
        extends LinkedHashMap<K, V> {

    private final int capacity;

    public static void main(String[] args) {

        // Create a cache that holds at most 3 entries
        LruCache<Integer, String> cache =
            new LruCache<>(3);

        // Insert entries in order
        cache.put(1, "Java");
        cache.put(2, "Python");
        cache.put(3, "SQL");

        // Access key 1, making it the most recently used
        cache.get(1);

        // Cache exceeds capacity: key 2 is evicted (least recently used)
        cache.put(4, "Docker");

        // Print entries from least to most recently used
        System.out.println(cache);
    }

    public LruCache(int capacity) {

        // Initial capacity 16, load factor 0.75, access-order enabled
        super(16, 0.75f, true);

        if (capacity <= 0) {
            throw new IllegalArgumentException(
                "Capacity must be positive"
            );
        }

        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(
            Map.Entry<K, V> eldest) {
        
        // Automatically evict the oldest entry when capacity is exceeded
        return size() > capacity;
    }
}
```

Output:

```
{3=SQL, 1=Java, 4=Docker}
```

### How it works

Initially, entries are ordered by insertion:

```
1 → 2 → 3
```

After accessing key 1:

```
2 → 3 → 1
```

After inserting key 4, key 2 is evicted:

```
3 → 1 → 4
```

### Complexity

- `get()` — expected \(O(1)\).
- `put()` — expected \(O(1)\).
- Eviction — \(O(1)\).
- Space — \(O(C)\), where \(C\) is cache capacity.
    

This implementation is not thread-safe and has no expiration policy. For production Spring Boot applications, a caching library such as Caffeine provides concurrency support, expiration, and more advanced eviction policies.

## 10. equals() and hashCode()

Java hash-based collections depend on two methods:

```java
equals()
hashCode()
```

The essential contract is:

If two objects are equal according to `equals()`, they must return the same `hashCode()`.

However, two unequal objects may return the same hash code.

### Example using Java records

```java
package example.datastructures;

import java.util.HashSet;
import java.util.Set;

public class RecordHashExample {

    record Product(String sku, String name) {}

    public static void main(String[] args) {

        Set<Product> products = new HashSet<>();

        products.add(new Product("A100", "Laptop"));
        products.add(new Product("A100", "Laptop"));
        products.add(new Product("B200", "Monitor"));

        System.out.println(products.size()); // 2
    }
}
```

Records automatically provide `equals()` and `hashCode()` implementations based on their components.

Notice that both `sku` and `name` participate in equality. If products should be considered identical based only on SKU, that business rule requires a different equality strategy or storing SKUs in a `HashSet<String>`.

Avoid modifying fields that participate in equality or hashing while an object is stored in a hash-based collection.

## 11. Common mistakes

**1. Assuming HashMap iteration order is guaranteed**

`HashMap` does not promise insertion order. Use `LinkedHashMap` if order matters.

**2. Confusing collisions with duplicate keys**

Different keys may share a hash code or bucket. Equality checks determine whether they represent the same key.

**3. Forgetting equals() and hashCode()**

Incorrect implementations can cause failed lookups or unexpected duplicates.

**4. Assuming all operations are guaranteed O(1)**

Hash-based collections provide expected constant-time performance, not unconditional worst-case guarantees.

**5. Using mutable objects as keys**

Changing a key's hash-relevant fields after insertion can make the entry difficult or impossible to retrieve normally.

**6. Using HashMap concurrently without synchronization**

`HashMap` is not thread-safe. For shared concurrent access, consider `ConcurrentHashMap` or appropriate synchronization.

## 12. Key takeaways

- Hash tables use hash functions to map keys to storage buckets.
- `HashMap` provides expected \(O(1)\) insertion, lookup, and removal.
- `HashSet` is ideal for uniqueness checks and duplicate detection.
- Hash collisions are normal and must be resolved correctly.
- Load factor controls the trade-off between memory usage and collision frequency.
- `equals()` and `hashCode()` must follow their contract.
- `LinkedHashMap` can implement an efficient LRU cache.
- Hash-based indexes are particularly useful for repeated lookups, deduplication, counting, and caching.