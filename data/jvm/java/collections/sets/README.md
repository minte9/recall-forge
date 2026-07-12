## JVM / Java / Collections / Sets

### 1. Set Basics

A Set is a collection that stores unique elements, meaning duplicates are not allowed. Unlike a List, a Set does not provide indexed access, so elements cannot be retrieved by position. The most common implementation is HashSet, which offers very fast insertion, removal, and lookup operations.  

~~~java
import java.util.HashSet;
import java.util.Set;

public class SetBasics {            
    public static void main(String[] args) {     

        // Create a set
        // ============
        Set<String> fruits = new HashSet<>();

        // Add elements
        // =============
        fruits.add("Apple");
        fruits.add("Banana");
        fruits.add("Apple"); // ignored
        fruits.add("Orange");
        System.out.println(fruits);  // [Apple, Banana, Orange] (no duplicate)

        // Check existance
        // ===============
        boolean exists = fruits.contains("Apple");  // O(1) fast
        System.out.println(exists);  // true

        // Remove elements
        // ===============
        fruits.remove("Orange");
        System.out.println("After removal: " + fruits);  // [Apple, Banana]

        // Iterate through elements
        // ========================
        for (String f : fruits) {
            System.out.println(f); // Apple, Banana
        }
    }
}
~~~


### 2. HashSet Example

HashSet is the most widely used implementation of the Set interface. It stores elements in an unordered collection and prevents duplicates by using the equals() and hashCode() methods. For custom classes, these methods should be overridden so that logically equal objects are treated as duplicates.

~~~java
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class HashSetExample {
    public static void main(String[] args) {

        // Custom type
        Set<Song> songs = new HashSet<>();
        songs.add(new Song("Imagine", "John Lennon"));
        songs.add(new Song("Imagine", "John Lennon")); // duplicate
        songs.add(new Song("Africa", "Toto"));
        songs.add(new Song("Africa", "Weezer"));  // not a duplicate

        System.out.println(songs); 
            // Imagine (John Lennon), Africa (Weezer), Africa (Toto)
    }
}

class Song { // implements Object implicit
    private String title;
    private String artist;

    public Song (String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    @Override
    public String toString() {
        return title + " (" + artist + ")";
    }

    @Override
    public boolean equals(Object o) {
        Song other = (Song) o;
        return title.equals(other.title) && artist.equals(other.artist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, artist);
    }
}
~~~


### 3. TreeSet Example

TreeSet is a Set implementation that automatically keeps its elements in sorted order. Unlike HashSet, it uses the natural ordering of elements (or a custom Comparator) to organize and compare them. Operations like add(), remove() run in O(log n).  

~~~java
package collections.sets;

import java.util.TreeSet;
import java.util.Set;

public class TreeSetExample {
    public static void main(String[] args) {

        Set<Item> myTree = new TreeSet<>();
        
        myTree.add(new Item("F", "1"));
        myTree.add(new Item("G", "2"));
        myTree.add(new Item("H", "4"));
        myTree.add(new Item("H", "3"));

        System.out.println(myTree); // [F:1, G:2, H:4]
    }
}

class Item implements Comparable<Item> {

    public String title;
    public String artist;

    public Item(String t, String a) {
        title = t;
        artist = a;
    }

    @Override
    public int compareTo(Item item) {
        return title.compareTo(item.title);
    }

    @Override
    public String toString() {
        return title + ":" + artist;
    }
}
~~~