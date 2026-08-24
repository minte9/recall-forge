## Security / Security / Web Security / CORS

### 1. Same-Origin Policy

The browser's same-origin policy blocks a script running on one origin (scheme + host + port) from reading responses from a different origin by default -- it's a browser-enforced isolation rule, not a server-side security control. Without it, a malicious site's JavaScript could freely read your bank session's data just because you're logged in elsewhere in the same browser.

~~~java
// https://app.com calling https://api.com -- different origin,
// blocked by the browser unless api.com opts in via CORS
fetch("https://api.com/account");
~~~

### 2. CORS Configuration

CORS is how a server explicitly opts back into cross-origin requests -- it responds with headers naming exactly which origins, methods, and headers it allows. A permissive `Access-Control-Allow-Origin: *` combined with credentials is a common misconfiguration; it effectively disables the protection same-origin policy was providing in the first place.

~~~java
@CrossOrigin(origins = "https://trusted-app.com")
@GetMapping("/account")
Account getAccount() { ... }
~~~
