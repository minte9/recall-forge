## JVM / Java / Collections / Lists

### 1. Lists Basics

A List is an ordered collection that allows duplicate elements and preserves the order in which they are inserted. List is an interface, while ArrayList is its most commonly used implementation, storing elements in a dynamically resizable array. Unlike arrays, lists can grow and shrink as needed, but they store objects rather than primitive types (autoboxing automatically converts primitives to their wrapper classes). Time Complexity is O(1) for index access, O(1) for insertion/removal at the and and O(n) for worst case, insertion/removal in the middle.

 ~~~java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListBasics {
   public static void main(String[] args) {
        
        // Create and populate
        // ===================
        List<String> A = new ArrayList<>();
        A.add("a");
        A.add("b");
        A.add("b");  // duplicates are allowed
        A.add("c");
        A.add("d");

        // Iterate using enhanced for loop
        // ===============================
        for (String s : A) {
            System.out.print(s + " ");  // a b b c d 
        }

        // Remove element by value OR index
        // ================================
        A.remove("b");
        A.remove(2);
        System.out.println(A);  // a, b, d]

        // Check if element exists
        // =======================
        Boolean exists = A.contains("c");
        System.out.println("c exists = " + exists); // true

        // Get element by index
        // ====================
        String first = A.get(0);
        System.out.println("Fist is " + first);  // a

        // Find the index
        // ==============
        int index = A.indexOf("c");
        System.out.println("Index of c " + index);  // 1

        // Get sublist
        // ===========
        List<String> sublist = A.subList(0, 2);
        System.out.println("Sublist " + sublist);  // [a, c]

        // Get size
        // ========
        int size = A.size();
        System.out.println("Size = " + size);  // 2

        // Sorting
        // =======
        A.add("b");
        Collections.sort(A);
        System.out.println(A);  // [a, b, b, d]

        // Array to fixed-size List view
        // =============================
        String[] arr = {"A", "B", "C"};            // size fixed
        List<String> fixed = Arrays.asList(arr);   // size fixed
        
        fixed.set(1, "B+");                        // OK: writes back into arr
            // fixed.add("D");                     // ? UnsupportedOperationException
        System.out.println("Array after set via view: " 
            + Arrays.toString(arr)                  // [A, B+, C]
        );  
         
        // Array to resizable List
        // ========================
        List<String> resizable = new ArrayList<>(fixed);
        resizable.add("D");
        System.out.println("Resizable list: " + resizable);  // [A, B+, C, D]
   } 
}
~~~


### 2. List Examples

The following examples show how ArrayList is used in practice to manage a collection whose size changes over time. They demonstrate adding, reading, updating, and removing elements, checking for their existence, and iterating through the list. Unlike arrays, no initial capacity needs to be specified, as the list grows automatically when new elements are added.

~~~java
import java.util.ArrayList;
import java.util.List;

public class ListExamples {
    public static void main(String[] args) {

        // Grades - Dynamic size
        // =====================
        List<Integer> grades = new ArrayList<>();
        grades.add(85);
        grades.add(90);
        grades.add(78);

        System.out.println("Initial: " + grades);       // [85, 90, 78]
        System.out.println("Size: " + grades.size());   // Size: 3

        // Add more letter (grows as needed)
        grades.add(92);
        grades.add(88);

        // Parsing loop
        for (int g : grades) {
            System.out.print(g + " ");                  // 85 90 78 92 88
        }
        
        // Read and modify elements
        int last = grades.get(grades.size() - 1);       // 88
        grades.set(grades.size() - 1, last + 12);       // 100

        System.out.println("Later: " + grades);         // [85, 90, 78, 92, 100]
        System.out.println("Size: " + grades.size());   // 5

        // Check contain and remove
        if (grades.contains(90)) {
            grades.remove(Integer.valueOf(90));             // remove by value
        }
        System.out.println("After remove(90): " + grades);  // [85, 78, 92, 100]

        // Remove by index
        grades.remove(0); // remove first
        System.out.println("After remove(0): " + grades);  // [78, 92, 100]
    }
}
~~~


### 3. Array vs ArrayList

Arrays and ArrayList are both used to store sequences of elements, but they have different characteristics. Arrays have a fixed size and can store primitive values directly, while ArrayList grows and shrinks dynamically but stores only objects (wrapper classes are used for primitives).

~~~java
import java.util.ArrayList;
import java.util.List;

public class ArrayVsArrayList {
    public static void main(String[] args) {

        // Fixed vs Dynamic
        // ================
        int[] arr = new int[3];
        arr[0] = 1;                  // cannot arr.add()
        System.out.println(arr[0]);  // 1

        List<String> arrayList = new ArrayList<>(); 
        arrayList.add("a");
        System.out.println(arrayList.get(0));        // a
        System.out.println(arrayList.indexOf("a"));  // 0

        // Types of elements 
        // =================
        int[] nums = {1, 2, 3};
        nums[0] = 4;                 // cannot nums[0] = new Object();
        System.out.println(arr[0]);  // 1

        List<Integer> numsList = new ArrayList<>();
        numsList.add(1);
        numsList.add(2);
        System.out.println(numsList.get(1));  // 2
    }
}
~~~


### 4. List Sort

Lists can be sorted in ascending or descending order using the utility methods provided by the Collections class. By default, sorting follows the natural ordering of the elements and modifies the original list. For custom classes, you must define how objects are compared by implementing Comparable or providing a Comparator.

~~~java
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class ListSort {
    public static void main(String[] args) {

        List<String> myList = new ArrayList<>();
        myList.add("AB");
        myList.add("BC");
        myList.add("AD");

        // Sort the list alphabetically (ascending)
        Collections.sort(myList);
        System.out.println(myList); // [AB, AD, BC]

        // Sort in reverse order
        Collections.sort(myList, Collections.reverseOrder());
        System.out.println(myList);  // [BC, AD, AB]

        // Equivalent modern call (Java 8+)
        myList.sort(String::compareTo);
        System.out.println(myList);  // [AB, AD, BC]

        // Sorting objects (the compiler doesn't know what t sort)
        List<Song> songList = new ArrayList<>();
        songList.add(new Song("A", "y"));
        songList.add(new Song("B", "z"));
        songList.add(new Song("C", "w"));

        Collections.sort(songList);
        System.out.println(songList);  // [A:y, B:z, C:w]
    }
}

class Song implements Comparable<Song> {
    String title;
    String artist;

    public Song(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    @Override
    public int compareTo(Song song) {
        return title.compareTo(song.title);
    }

    @Override public String toString() {
        return title + ":" + artist;
    }
}
~~~


### 5. ArrayList vs. LinkedList

ArrayList and LinkedList are the two most common implementations of the List interface. Both preserve insertion order and allow duplicate elements, but they use different internal data structures. ArrayList provides fast random access, while LinkedList is more efficient for frequent insertions and removals.

~~~java
package collections.lists;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ArrayListVsLinkedList {
    public static void main(String[] args) {
        
        // Declaration
        List<String> arrayList = new ArrayList<>();
        List<String> linkedList = new LinkedList<>();

        // Adding elements
        arrayList.add("A");
        arrayList.add("B");

        linkedList.add("A");
        linkedList.add("B");

        // Accesing elements
        System.out.println("ArrayList ge(1): " + arrayList.get(1));    // Fast
        System.out.println("LinkedList get(1): " + linkedList.get(1)); // Slower

        // Inserting in the middle
        arrayList.add(1, "X");  // Slower (shifts elements)
        linkedList.add(1, "X"); // Faster (node relinking)

        System.out.println("ArrayList: " + arrayList);   // [A, X, B]
        System.out.println("LinkedList: " + linkedList); // [A, X, B]

        // Removing elements
        arrayList.remove(2);  // Slower
        linkedList.remove(2); // Faster

        System.out.println("ArrayList: " + arrayList);   // [A, X]
        System.out.println("LinkedList: " + linkedList); // [A, X]
    }
}
~~~
