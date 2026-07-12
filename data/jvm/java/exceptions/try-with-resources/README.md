## JVM / Java / Exceptions / try-with-resources

### 1. AutoCloseable Basics

try-with-resources automatically calls close() on any resource implementing AutoCloseable when the try block exits, whether normally or via an exception -- no finally block needed. This replaced the old pattern of manually closing resources in finally, which was easy to get wrong (forgetting it, or closing in the wrong order for multiple resources).

~~~java
try (BufferedReader reader = new BufferedReader(new FileReader("file.txt"))) {
    System.out.println(reader.readLine());
} // reader.close() is called automatically here, even if an exception was thrown
~~~


### 2. Multiple Resources and Closing Order

Multiple resources can be declared in one try-with-resources, separated by semicolons -- they're closed in the reverse order they were opened, so a resource that depends on another (like a wrapped stream) is always closed before the one it wraps.

~~~java
try (FileInputStream fis = new FileInputStream("file.txt");
     BufferedInputStream bis = new BufferedInputStream(fis)) {
    // bis is closed first, then fis -- reverse of declaration order
}
~~~


### 3. Suppressed Exceptions

If both the try block and a resource's close() throw, the close() exception doesn't replace the original -- it's attached as a "suppressed" exception on the original one instead, retrievable via getSuppressed(). This preserves the root cause as the primary exception instead of silently losing it behind a cleanup failure.

~~~java
try {
    // ... throws OriginalException
} catch (Exception e) {
    for (Throwable suppressed : e.getSuppressed()) {
        System.out.println("Also failed during cleanup: " + suppressed);
    }
}
~~~
