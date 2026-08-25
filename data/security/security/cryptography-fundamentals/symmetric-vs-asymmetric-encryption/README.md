# Security / Security / Cryptography Fundamentals / Symmetric vs Asymmetric Encryption

## 1. Symmetric Encryption

Symmetric encryption uses the same key to encrypt and decrypt -- fast, well-suited to bulk data, but both parties need to already share that key securely, which is a real logistical problem before any data ever moves. AES is the standard choice today.

```java
KeyGenerator keyGen = KeyGenerator.getInstance("AES");
keyGen.init(256);
SecretKey key = keyGen.generateKey(); // same key encrypts & decrypts
```

## 2. Asymmetric Encryption

Asymmetric encryption uses a key pair -- data encrypted with the public key can only be decrypted with the private key. This solves symmetric encryption's key-sharing problem, since the public key can be shared openly, but it's much slower, so in practice it's often used just to exchange a symmetric key, which then does the bulk work.

```java
KeyPair pair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
cipher.init(Cipher.ENCRYPT_MODE, pair.getPublic());
```
