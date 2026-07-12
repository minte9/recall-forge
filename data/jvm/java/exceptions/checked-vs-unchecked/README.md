## JVM / Java / Exceptions / Checked vs Unchecked

### 1. Checked Exceptions

Checked exceptions (subclasses of Exception, excluding RuntimeException) must be either caught or declared with throws in the method signature -- the compiler enforces this. They represent conditions a well-written program should anticipate and recover from, like IOException when a file might not exist. This forces callers to consciously decide how to handle the failure.

~~~java
void readFile(String path) throws IOException { // must declare, or catch internally
    Files.readString(Path.of(path));
}

void caller() {
    try {
        readFile("data.txt");
    } catch (IOException e) { // compiler forces this decision
        System.out.println("Could not read file: " + e.getMessage());
    }
}
~~~


### 2. Unchecked Exceptions

Unchecked exceptions extend RuntimeException and are not enforced by the compiler -- no throws declaration or catch block is required, though either is still allowed. They usually represent programming errors (NullPointerException, IllegalArgumentException) that could have been prevented, rather than conditions to recover from at runtime, so forcing every caller to handle them would just add noise.

~~~java
void withdraw(double amount) {
    if (amount < 0) {
        throw new IllegalArgumentException("Amount cannot be negative"); // no throws needed
    }
}

withdraw(-10); // compiles fine without a try/catch, throws at runtime if reached
~~~


### 3. Error vs Exception

Throwable has two main subclasses: Exception (recoverable conditions, the ones application code handles) and Error (serious problems like OutOfMemoryError or StackOverflowError that a program usually shouldn't try to catch or recover from, since the JVM itself may be in an unstable state). Catching Throwable or Error directly is almost always a mistake.

~~~java
try {
    recurse(); // eventually throws StackOverflowError
} catch (Exception e) {
    // does NOT catch StackOverflowError -- Error is a sibling of Exception, not a subclass
    System.out.println("Handled: " + e);
}
~~~
