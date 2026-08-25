# JVM / Java / Collections / Arrays

## 1. Array Basics

Arrays in Java have fixed size. They can store either primitives or objects (wrappers unboxing). As low-level structures they are very fast but are are less flexible than ArrayList. They are not part of the Collections framework, they belongs to the the java.util package.

```java
package collections.arrays;

import java.util.Arrays;

public class ArrayBasics {
    public static void main(String[] args ) {

        // Create and populate
        // ===================
        String[] arr = new String[3];
        arr[0] = "a";
        arr[1] = "b";
        arr[2] = "c";
        

        // Get element (by index)
        // ======================
        String first = arr[0];
        System.out.println("First " + first);  // a


        // Set element
        // ===========
        arr[1] = "x";
        System.out.println("Changed " + arr[1]);  // x


        // Iterate using enhanced for loop
        // ===============================
        for (String s : arr) {
            System.out.println(s);  // a x c
        }


        // Get size
        // ========
        int size = arr.length;
        System.out.println("Length " + size);  // 3


        // Object unboxing
        // ===============
        int[] nums = {1, 2, 3, 0};
        nums[2] = 4;
        nums[3] = Integer.parseInt("4");
        System.out.println(nums[3]);  // 4


        // Copy arrays safely
        // ==================
        int[] copy = Arrays.copyOf(nums, nums.length);


        // Chaning the copy does not affect the original
        // =============================================
        copy[0] = 100;  
        System.out.println("Original: " + nums[0]);  // 1


        // Changing the original does not affect the copy
        // ==============================================
        nums[0] = 999;
        System.out.println("Copy: " + copy[0]);  // 100

        
        // Array intialization with literals
        // =================================
        String[] chars = {"a", "b", "c", "d"};
        System.out.println(chars[0]);  // a

        int[][] matrix = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9},
        };
        System.out.println(matrix[1][2]);  // 6
    }
}
```

## 2. Array Examples

Once an array is created, its size cannot be changed. Elements are accessed using their index, which starts at 0. The following examples demonstrate two common uses of arrays: storing a fixed set of exam grades and representing the seven days of the week.

```java
package collections.arrays;

public class ArrayExamples {
    public static void main(String[] args) {

        // GRADES - Fixed number of grades - exactly 5
        // ===========================================
        int[] grades = new int[5];
        grades[0] = 85;
        grades[1] = 90;
        grades[2] = 78;
        grades[3] = 92;
        grades[4] = 88;
        System.out.println(
            "Exam grades: " + grades.length  // Exam grades: 5
        );
        for (int g : grades) {
            System.out.print(g + " ");  // 85 90 78 92 88
        }
        
        // WEEK DAYS - Fixed numer of week days
        // ====================================
        String days[] = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        System.out.println(
            "Week days: " + days.length // Week days: 7
        );
        for (String d : days) {
            System.out.print(d + " ");  // Mon Tue Wed Thu Fri Sat Sun
        }
    }
}
```~
