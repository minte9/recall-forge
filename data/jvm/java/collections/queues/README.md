## JVM / Java / Collections / Queues

### 1. Queue Basics

A Queue stores elements in First In, First Out (FIFO) order, where the first element added is the first one removed. It is commonly used to model waiting lines, task scheduling, and message processing. Java provides pairs of methods (offer/add, poll/remove, peek/element) that perform the same operations but differ in how they handle failures.

~~~java
import java.util.ArrayDeque;
import java.util.Queue;

public class QueueBasics {
    public static void main(String[] args) {
        
        // Create a Queue
        Queue<String> queue = new ArrayDeque<>();

        // Add elements (People entering line)
        queue.offer("Alice");
        queue.offer("Bob");
        queue.offer("Charlie");

        // View front (Next to be served)
        String first = queue.peek();
        System.out.println("Next customer: " + first);  // Alice

        // Remove element (Serving customer)
        String served = queue.poll();
        System.out.println("Served: " + served);  // Alice
        System.out.println("Queue after serving: " + queue);  // [Bob, Charlie]

        // Add more people
        queue.offer("Diana");
        queue.offer("Eva");
        System.out.println("Updated queue: " + queue);  // [Bob, Charlie, Diana, Eva]

        // Process entire queue
        while (!queue.isEmpty()) {
            System.out.println("Serving: " + queue.poll()); 
                // Bob
                // Charlie ...
        }
    }
}
~~~


### 2. Deque Example

A Deque (double-ended queue) allows elements to be inserted and removed from both the front and the back of the collection. This makes it suitable for implementing both queues (FIFO) and stacks (LIFO). In most cases, ArrayDeque is the preferred implementation because it is faster than LinkedList for deque operations. 

~~~java
package collections.queues;

import java.util.ArrayDeque;
import java.util.Deque;

public class DequeExample {
    public static void main(String[] args) {
        
        // Create Deque
        Deque<String> deque = new ArrayDeque<>(); // Both ends (not like Queue)

        // Use as queue (FIFO)
        deque.offerLast("Alice");  // same as addLast (but better)
        deque.offerLast("Bob");
        deque.offerLast("Charlie");
        System.out.println("Serve: " + deque.pollFirst()); // remove from front (Alice)

        // Use as stack (LIFO)
        deque.push("X");  // same as addFirst
        deque.push("Y");
        deque.push("Z");
        System.out.println("Pop: " + deque.pop());  // Z

        // Working with both end
        deque.addFirst("Start");
        deque.addLast("End");
        System.out.println(deque);  // [Start, Y, X, Bob, Charlie, End]
        System.out.println("Remove first: " + deque.removeFirst());  // Start
        System.out.println("Remove last: " + deque.removeLast());  // End

        // Peek operations
        System.out.println("Peek first: " + deque.peekFirst());  // Y
        System.out.println("Peek last: " + deque.peekLast());  // Charlie
    }
}
~~~
