# JVM / Java / Basics / Control Flow

## 1. Switch Fall-Through and the Modern Arrow Syntax

Classic switch statements fall through to the next case unless each one ends with break -- a frequent source of bugs when a break is forgotten. The arrow syntax (case X ->), finalized in Java 14, doesn't fall through at all and can be used as an expression, returning a value directly.

```java
int day = 3;

switch (day) {
    case 1: System.out.println("Mon");
    case 2: System.out.println("Tue"); // falls through if day == 1, likely a bug
        break;
}

String name = switch (day) { // arrow syntax: no fall-through, usable as an expression
    case 1 -> "Mon";
    case 2 -> "Tue";
    default -> "Unknown";
};
```


## 2. Labeled Break and Continue

A labeled break/continue targets an outer loop from inside a nested one, something a plain break/continue can't do (it only affects the innermost loop). Useful for escaping nested loops early without a flag variable, though it's used sparingly since it can hurt readability if overused.

```java
outer:
for (int i = 0; i < 3; i++) {
    for (int j = 0; j < 3; j++) {
        if (j == 1) continue outer; // skips to the next i, not just the next j
        System.out.println(i + "," + j);
    }
}
```


## 3. for vs for-each vs while

A classic for loop is best when you need the index itself; for-each is clearer for simply visiting every element and prevents off-by-one errors; while fits when the number of iterations isn't known upfront (e.g. reading until a condition changes). Picking the most specific one signals intent to the reader.

```java
for (int i = 0; i < list.size(); i++) { } // need the index

for (String item : list) { }              // just visiting each element

while (scanner.hasNextLine()) { }         // iteration count unknown upfront
```
