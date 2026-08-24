## Security / Security / Cryptography Fundamentals / Hashing vs Encryption

### 1. Hashing

Hashing is a one-way transformation -- there's no key to reverse it, so it's used to verify data hasn't changed (checksums) or to store a password without ever storing the password itself. The same input always produces the same output, and a tiny change in input produces a completely different hash.

~~~java
byte[] hash = MessageDigest.getInstance("SHA-256")
    .digest(data.getBytes());
~~~

### 2. Encryption

Encryption is two-way -- it transforms data into ciphertext using a key, and that same (or a paired) key can transform it back into the original plaintext. Use encryption when you need the original data back later, like an encrypted database column; use hashing when you only ever need to verify, never retrieve, like a password.

~~~java
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
cipher.init(Cipher.ENCRYPT_MODE, secretKey);
byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
~~~
