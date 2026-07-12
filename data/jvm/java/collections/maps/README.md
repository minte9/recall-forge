## JVM / Java / Collections / Maps

### 1. Key-value pairs

A Map stores data as key-value pairs, where each key is unique and maps to a single value. The most common implementation is HashMap, which uses hashing to provide very fast insertion and lookup operations. If a key already exists, calling put() replaces its associated value with the new one.

~~~java
package collections.maps;

import java.util.HashMap;
import java.util.Map;

public class HashMapExample {
    public static void main(String[] args) {
    
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Mary", 10);
        scores.put("Mary", 20); // overrites value for key "Mary"
        scores.put("John", 20);
        scores.put("Boby", 16);

        System.out.println(scores); // {John=20, Boby=16, Mary=20}
        System.out.println(scores.get("John"));  // 20
    }
}
~~~


### 2. Map Iteration

Maps can be traversed by iterating over their keys, values, or key-value pairs. The keySet(), values(), and entrySet() methods provide efficient ways to access the information stored in a map, depending on what your code needs.

~~~java
/**
 * Java does not support Javascript style object literals.
 *      let map = { a:1, b:2 }  // ? NOT valid in Java
 * 
 * But Java has alternatives to initialize a map without put().
 *      Map.of() (Java 9+) 
 */
import java.util.HashMap;
import java.util.Map;

public class MapLoop {
    public static void main(String[] args) {
    
        Map<String, Integer> scores = Map.of(  // Java 9+
            "Mary", 10,
            "John", 20,
            "Boby", 16
        );

        // Using put() (most common)
        scores = new HashMap<>();
        scores.put("Mary", 10);
        scores.put("John", 20);
        scores.put("Boby", 16);

        // Loop 1: keys only
        for (String key : scores.keySet()) {
            System.out.println(key);  // John Mary Boby
        }

        // Loop 2: values only
        for (Integer value : scores.values()){
            System.out.println(value);  // 20 16 10
        }

        // Loop 3: both keys and values
        for(Map.Entry<String, Integer> entry : scores.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
                // John 20
                // Boby 16
                // Mary 10
        }
    }
}
~~~
