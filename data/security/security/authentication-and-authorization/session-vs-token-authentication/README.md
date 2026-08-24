## Security / Security / Authentication & Authorization / Session vs Token Authentication

### 1. Session-Based Authentication

In session-based auth, the server creates a session on login and stores it server-side, handing the client only an opaque session ID, usually in a cookie, that points to it. The server can invalidate the session at any time, but that also means the server must keep session state somewhere, which complicates scaling across multiple instances.

~~~java
HttpSession session = request.getSession(true);
session.setAttribute("userId", user.getId());
// client only holds the session ID cookie, JSESSIONID
~~~

### 2. Token-Based Authentication (JWT)

In token-based auth, the server issues a signed token, typically a JWT, containing the user's identity, and the client sends it back on every request -- the server verifies the signature instead of looking anything up, so it's stateless and scales easily. The trade-off: a token can't be revoked before it expires without extra infrastructure like a blocklist.

~~~java
String token = Jwts.builder()
    .setSubject(user.getId().toString())
    .setExpiration(Date.from(now().plusHours(1)))
    .signWith(secretKey)
    .compact();
~~~
