## JVM / Java / Collections / Framework

### 1. Java Collections Framework (JCF)

In Java, Collections are a full arhitecture of interfaces, classes, utilities. They are part of the Java Collection Framework (JCF), located in java.util package. The JCF provides interfaces (List, Set, Map, Queue), implementations (ArrayList, HashSet, HashMap, LinkedList) and algorithms (sorting, searching).

~~~sh
               Iterable
                  |
              Collection
      ┌───────────┼──────────┐
      |           |          |
     List        Set       Queue
      |           |          |
   ArrayList   HashSet    ArrayDeque
   LinkedList  TreeSet    PriorityQueue
 
                Map
                 |
              HashMap
              TreeMap
~~~

### 2. Collection Interface

The Collectioin Interface represents a group of objects (like a bag). List subinterfaces are ordered, have indexed access and have implemntations like ArrayList and LinkedList. Sets have no duplicates and  have implementation like HashSet (unordered), TreeSet (ordered). Queue and Deque are FIFO/LIFO and have implementations like LinkedList, ArrayDeque.

~~~java
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class CollectionExample {
    public static void main(String[] args) {

        // Collection reference can point to different implementations
        Collection<String> list = new ArrayList<>();
        list.add("Java");
        list.add("Python");

        Collection<String> set = new HashSet<>();
        set.add("Java");
        set.add("Java");      // duplicate ignored

        System.out.println(list); // [Java, Python]
        System.out.println(set);  // [Java]
    }
}
~~~

### 3. Map Interace

Map is NOT a subtype of Collection. It stores key-value pairs and have implementations like HashMap, LinkedHashMap and TreeMap.

~~~java
import java.util.HashMap;
import java.util.Map;

public class MapExample {
    public static void main(String[] args) {

        Map<String, Integer> ages = new HashMap<>();

        ages.put("Ana", 25);
        ages.put("Maria", 30);
        ages.put("Ana", 26);   // replaces previous value

        System.out.println(ages.get("Ana"));   // 26
        System.out.println(ages.containsKey("Maria")); // true
    }
}
~~~

### 4. Collections vs Arrays

Arrays are fixed size, can store primitives, have no built-in utilities and are hard to manipulate. Collections have dynamic size, store objects (but wrappers allow primitives), are rich in functionality and it is easy to add/remove/find items.

~~~java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JCF {
    public static void main(String[] args) {

        // LIST (ordered, allows duplicates)
        // =================================
        List<String> names = new ArrayList<>();

        names.add("Alex");
        names.add("Maria");

        for (String n : names) {
            System.out.println(n); // Alex, Maria
        }

        // SET (no duplicates, unordered)
        // ==============================
        Set<String> cities = new HashSet<>();

        cities.add("Bucharest");
        cities.add("Cluj");
        cities.add("Bucharest"); // duplicate ignored

        for (String c : cities) {
            System.out.println(c);  // Bucharest, Cluj
        }

        // MAP (key-value pairs)
        // =====================
        Map<String, Integer> ages = new HashMap<>();

        ages.put("Ana", 25);
        ages.put("Bogdan", 32);
        ages.put("Ana", 26);  // overwrites previous value
        
        for (Map.Entry<String, Integer> entry : ages.entrySet()) {
            System.out.println(
                entry.getKey() + " -> " + entry.getValue()  // Ana -> 26  Bogdan -> 32
            );
        }
    }
}
~~~
