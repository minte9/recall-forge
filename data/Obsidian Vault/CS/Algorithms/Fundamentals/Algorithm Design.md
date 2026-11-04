e## 1. What Is Algorithm Design?

Algorithm design is the process of choosing a strategy for solving a computational problem efficiently and correctly. Rather than immediately writing code, we first examine the problem's constraints, available data, and desired result.

Several common design techniques appear across computer science:

|Technique|Main idea|Example|
|---|---|---|
|Brute Force|Explore all possible solutions|Find the shortest route by checking every path|
|Divide and Conquer|Split into smaller independent problems|Merge sort, binary search|
|Greedy|Make the best local choice|Select activities, interval scheduling|
|Dynamic Programming|Reuse solutions to overlapping subproblems|Knapsack, coin change|
|Backtracking|Explore choices and undo unsuccessful ones|Sudoku, package placement|

These are general strategies, not individual algorithms. The same problem can often be solved using several approaches, each with different performance and correctness guarantees.

## 2. Brute Force

Brute force solves a problem by systematically examining possible solutions, without relying on sophisticated optimization.

Consider finding two numbers in an array whose sum equals a target.

```java
public static boolean hasPair(int[] nums, int target) {
    for (int i = 0; i < nums.length; i++) {
        for (int j = i + 1; j < nums.length; j++) {
            if (nums[i] + nums[j] == target) {
                return true;
            }
        }
    }
    return false;
}
```

The algorithm checks every distinct pair until it finds a match.

Complexity: O(n²) worst-case time, O(1) auxiliary space.

Brute force is often useful as a starting point because it is straightforward to implement and can serve as a reference for testing optimized solutions.

### Improving with a HashSet

Two sum problem:

```java
public static boolean hasPair(int[] nums, int target) {
    Set<Integer> seen = new HashSet<>();

    for (int num : nums) {
        if (seen.contains(target - num)) {
            return true;
        }
        seen.add(num);
    }

    return false;
}
```

The key idea is:

```java
a + b = target  =>  b = target - a
```

The improved solution has expected O(n) time complexity and O(n) auxiliary space, assuming ordinary constant-time hash operations and no integer overflow in the subtraction.

The key improvement comes from using a data structure that makes previous values easy to find.

Complete example:

```java
import java.util.HashSet;
import java.util.Set;

public class TwoSum {
    public static void main(String[] args) {
        int[] nums = {2, 7, 11, 5};
        int target = 9;

        System.out.println(hasPair(nums, target));  // true

        int[] items = {4, 4};
        target = 8;

        System.out.println(hasPair(items, target));  // true

    }   
    
    public static boolean hasPair(int[] nums, int target) {
        Set<Integer> seen = new HashSet<>();

        for (int n : nums) {
            if (seen.contains(target - n)) {
                return true;
            }
            seen.add(n);
        }

        return false;
    }
}
```

## 3. Divide and Conquer

Divide and conquer solves a problem by dividing it into smaller subproblems, solving them independently, and combining their results.

### Example: Finding the maximum recursively

This algorithm finds the maximum value in an array using recursion and Divide and Conquer.

```java
public static int max(int[] arr, int left, int right) {
    if (left == right) {
        return arr[left];
    }

    int mid = left + (right - left) / 2;

    int maxLeft = max(arr, left, mid);
    int maxRight = max(arr, mid + 1, right);

    return Math.max(maxLeft, maxRight);
}
```

Call with `max(arr, 0, arr.length - 1)` for a nonempty array.

The algorithm divides the array into halves until each subproblem contains one element. It then combines the results by selecting the larger value.

Complete example:

```java
public class FindMaximum {

    public static void main(String[] args) {
        int[] arr = {3, 8, 2, 10, 5, 7};
        int result = max(arr, 0, arr.length - 1);
        System.out.println("Maximum: " + result);
    }

    public static int max(int[] arr, int left, int right) {
        // Base case: only one element remains
        if (left == right) {
            return arr[left];
        }

        // Divide the array into two halves
        int mid = left + (right - left) / 2;

        // Find maximum in the left half
        int maxLeft = max(arr, left, mid);

        // Find maximum in the right half
        int maxRight = max(arr, mid + 1, right);

        // Combine the results
        return Math.max(maxLeft, maxRight);
    }
}
```

Time: O(n). Auxiliary space: O(log n) for the recursion stack.

This is not faster than a simple linear scan, but it demonstrates the divide-and-conquer structure used in more sophisticated algorithms such as merge sort.

![[Pasted image 20261103190626.png]]

## 4. Greedy Algorithms

A greedy algorithm constructs a solution by repeatedly choosing what appears best at the current step, without reconsidering previous choices.

Greedy strategies are often fast, but they do not always produce a globally optimal solution.

### Example: Selecting non-overlapping activities

Suppose several activities have start and end times. We want to select the maximum number of activities that do not overlap.

A correct greedy strategy is to select the activity that finishes earliest, then repeat with compatible activities.

```java
import java.util.*;

record Activity(int start, int end) {}

public static List<Activity> selectActivities(
        List<Activity> activities) {

    List<Activity> sorted = new ArrayList<>(activities);
    sorted.sort(Comparator.comparingInt(Activity::end));

    List<Activity> selected = new ArrayList<>();
    int lastEnd = Integer.MIN_VALUE;

    for (Activity activity : sorted) {
        if (activity.start() >= lastEnd) {
            selected.add(activity);
            lastEnd = activity.end();
        }
    }

    return selected;
}
```

Time: O(n log n), dominated by sorting.

Auxiliary space: O(n), for the copied list and selected activities.

Choosing the earliest finishing activity is optimal for maximizing the number of compatible activities under the standard interval-scheduling assumptions.

### When greedy fails

Consider making change for 6 units using coins worth 1, 3, and 4.

A greedy strategy chooses the largest possible coin first:

- 4 + 1 + 1 = 3 coins

But the optimal solution is:

- 3 + 3 = 2 coins

This demonstrates that local optimization does not always produce a globally optimal result.

## 5. Dynamic Programming

Dynamic programming (DP) solves problems by breaking them into overlapping subproblems and storing previously computed results.

It is particularly useful when a problem has:

- Overlapping subproblems: The same smaller problems appear repeatedly.
- Optimal substructure: An optimal solution can be constructed from optimal solutions to smaller subproblems.
    
### Example: Minimum number of coins

For coin denominations 1, 3, and 4, find the minimum number of coins needed to make a given amount.

This is the Coin Change Problem, a classic example of Dynamic Programming (DP).

The goal is to find the minimum number of coins needed to make a given amount, assuming each coin denomination can be used an unlimited number of times.

```java
import java.util.*;

public static int minCoins(int[] coins, int amount) {
    int[] dp = new int[amount + 1];

    Arrays.fill(dp, amount + 1);
    dp[0] = 0;

    for (int value = 1; value <= amount; value++) {
        for (int coin : coins) {
            if (coin <= value) {
                dp[value] = Math.min(
                    dp[value],
                    dp[value - coin] + 1
                );
            }
        }
    }

    return dp[amount] > amount ? -1 : dp[amount];
}
```

Assuming positive coin denominations and a nonnegative amount, the algorithm calculates the minimum number of coins for every amount from 0 to the target.

Time: O(amount × number of coin types).

Space: O(amount).

For amount 6, the algorithm returns 2, corresponding to 3 + 3.

## 6. Backtracking

Backtracking explores possible solutions incrementally. When a partial solution cannot lead to a valid complete solution, the algorithm abandons that path and tries another.

It is commonly used for Sudoku, constraint satisfaction, combinations, and placement problems.

### Example: Generating subsets

```java
import java.util.*;

public static void subsets(
        int[] nums,
        int index,
        List<Integer> current) {

    if (index == nums.length) {
        System.out.println(current);
        return;
    }

    // Exclude current element
    subsets(nums, index + 1, current);

    // Include current element
    current.add(nums[index]);
    subsets(nums, index + 1, current);

    // Undo the choice
    current.remove(current.size() - 1);
}
```

For an array of `n` elements, there are 2ⁿ subsets.

Time: O(n × 2ⁿ), including printing or copying each subset.

Auxiliary space: O(n), for the recursion stack and current subset.

The defining backtracking operation is undoing a choice before exploring the next alternative.

## 7. Practical Application — Packing Packages into Boxes

Suppose we have packages with different volumes and want to minimize the number of boxes needed.

This is a version of the bin packing problem, which is NP-hard.

A common heuristic is First-Fit Decreasing (FFD):

1. Sort packages by volume in descending order.
2. Place each package into the first existing box with sufficient remaining capacity.
3. Open a new box if no existing box can accommodate it.
    

```java
import java.util.*;

public static int pack(List<Integer> packages, int capacity) {
    List<Integer> sorted = new ArrayList<>(packages);
    sorted.sort(Comparator.reverseOrder());

    List<Integer> remaining = new ArrayList<>();

    for (int size : sorted) {
        boolean placed = false;

        for (int i = 0; i < remaining.size(); i++) {
            if (remaining.get(i) >= size) {
                remaining.set(i, remaining.get(i) - size);
                placed = true;
                break;
            }
        }

        if (!placed) {
            remaining.add(capacity - size);
        }
    }

    return remaining.size();
}
```

This example assumes positive package sizes, positive box capacity, and that no package exceeds box capacity.

Time: O(n log n + n²) = O(n²) worst case.

Space: O(n).

This is a one-dimensional capacity model. Real 3D packing must also consider width, height, depth, rotation, collision detection, and placement constraints.

FFD is efficient and often produces good results, but it does not guarantee the minimum possible number of boxes.

## 8. Choosing an Algorithm Design Strategy

|Situation|Good starting strategy|
|---|---|
|Small search space|Brute force|
|Independent smaller problems|Divide and conquer|
|Proven safe local choices|Greedy|
|Repeated overlapping subproblems|Dynamic programming|
|Constraints and many possible configurations|Backtracking|
|Large NP-hard optimization problems|Heuristics or approximation algorithms|

The important engineering skill is recognizing the problem's structure before choosing an algorithm.

For example, finding the shortest path between cities with nonnegative road distances is efficiently solvable with Dijkstra's algorithm. Finding the shortest tour that visits every city and returns to the starting point is a different, generally NP-hard problem.
