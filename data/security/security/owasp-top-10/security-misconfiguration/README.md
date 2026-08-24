## Security / Security / OWASP Top 10 / Security Misconfiguration

### 1. Security Misconfiguration

Security misconfiguration covers everything from default credentials left unchanged, to verbose error pages leaking stack traces, to cloud storage left publicly readable -- it's not one specific bug but a whole category of "the system works, but its settings expose more than intended." It's common precisely because secure defaults require deliberate configuration, not just an absence of bugs.

~~~java
// Misconfigured: stack trace leaks internals to the client
@ExceptionHandler(Exception.class)
ResponseEntity<String> handle(Exception e) {
    return ResponseEntity.status(500).body(e.toString());
}
~~~

### 2. Vulnerable and Outdated Components

An application is only as secure as its weakest dependency -- a known CVE in a logging library or a web framework gives attackers a documented, often automated path in, regardless of how careful the application's own code is. This is why dependency versions need active tracking, not a "set it once and forget it" approach.

~~~java
// pom.xml: pinned to a version with a known CVE
<log4j.version>2.14.1</log4j.version>  <!-- CVE-2021-44228 -->
~~~
