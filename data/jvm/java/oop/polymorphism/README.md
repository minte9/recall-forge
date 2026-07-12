## JVM / Java / OOP / Polymorphism

### 1. Storage Service Application

Polymorphism means that the same method call can produce different behavior depending on the object's actual type at runtime. Code works with a common interface rather than concrete implementations, making it easier to extend and maintain.

~~~java
/**
 * A storage service can work with different storage providers
 * through the same interface.
 *
 * The application does not care if files are stored:
 * - locally
 * - in Amazon S3
 * - in Google GCS
 *
 * It only knows: store(fileName)
 * At runtime, Java decides which implementation to execute.
 */
package com.minte9.oop.polymorphism;

public class StorageServiceApp {
    public static void main(String[] args) {

        // Same interface reference
        StorageProvider storage = new LocalStorage();

        FileService service = new FileService(storage);
        service.uploadFile("photo.jpg");  // Saving file locally: photo.jpg

        // Runtime behavior changes
        storage = new CloudStorage();
        service = new FileService(storage);
        service.uploadFile("video.mp4");  // Uploading file to cloud: video.mp4
    }
}

interface StorageProvider {
    void storage(String fileName);
}

// First implementation
class LocalStorage implements StorageProvider {
    @Override
    public void storage(String fileName) {
        System.out.println("Saving file locally: " + fileName);
    }
}

// Second implementation
class CloudStorage implements StorageProvider {
    @Override
    public void storage(String fileName) {
        System.out.println("Uploading file to cloud: " + fileName);
    }
}

// Composition
class FileService {

    // Dependency Inversion:
    private StorageProvider storage;  // Business logic depends on abstraction

    // Dependency Injection:
    public FileService(StorageProvider storage) {  // Dependency injectected from outside
        this.storage = storage;
    }

    // Polymorphism:
    public void uploadFile(String fileName) {
        storage.storage(fileName);  // Same method call, different runtime behavior
    }
}
~~~


### 2. Dependency Injection

Dependency Injection -- receiving a dependency from outside instead of creating it -- is what makes `FileService` work with any `StorageProvider` implementation without changes. See the dedicated Spring / Framework subcategory for DI in a full framework context.

~~~java
class FileService {
    private StorageProvider storage;
    public FileService(StorageProvider storage) { // injected, not created internally
        this.storage = storage;
    }
}
~~~


### 3. Final Keyword

Declaring a field as final means its reference can be assigned only once. This helps create immutable object relationships by preventing dependencies from being replaced after construction, while still allowing the referenced object itself to change if it is mutable.

~~~java
class StorageProvider {
    private String bucket;

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getBucket() {
        return bucket;
    }
}

class FileService {
    private final StorageProvider storage;

    public FileService(StorageProvider storage) {
        this.storage = storage;
    }

    public void configureStorage() {
        storage.setBucket("documents"); // Allowed
    }

    public void replaceStorage() {
        // storage = new StorageProvider(); // Compilation error
    }
}
~~~


### 4. Static Keyword

The static keyword declares members that belong to the class rather than to individual objects. Static fields are shared by all instances and are commonly used for counters, constants, and utility methods.

~~~java
class FileService {

    public static int totalUploads = 0;  // shared by all instances
    private StorageProvider storage;

    public FileService(StorageProvider storage) {
        this.storage = storage;
    }

    public void uploadFile(String fileName) {
        storage.store(fileName);
        totalUploads++;  // Look Here
    }
}
~~~
