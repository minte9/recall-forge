# Security / Security / Application Security Testing / SAST vs DAST

## 1. Static Analysis (SAST)

SAST scans source code without running it, looking for known-dangerous patterns -- string-concatenated SQL, hardcoded secrets, unsafe deserialization. It catches issues early, before deployment, but it can't see anything that only emerges at runtime, like a misconfigured server header.

```java
// SAST flags this pattern without ever executing it
String sql = "SELECT * FROM users WHERE id = " + id;
```

## 2. Dynamic Analysis (DAST)

DAST tests a running application from the outside, the way an attacker would -- sending malicious payloads to live endpoints and observing the responses. It catches runtime and configuration issues SAST can't see, but it needs a deployed environment to run against and can't point at a specific line of source code the way SAST can.

```java
// DAST probes the live endpoint externally
POST /login  { "user": "admin' OR '1'='1", "pass": "x" }
```
