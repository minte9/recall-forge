## Security / Security / Secure Coding Practices / Input Validation

### 1. Allowlist Validation

Allowlist validation defines exactly what valid input looks like -- a username matches `^[a-zA-Z0-9_]{3,20}$` -- and rejects everything that doesn't match. Blocklisting, trying to enumerate every bad character or pattern, almost always misses an edge case an attacker will find; allowlisting is safer because it only has to define "good," not enumerate every possible "bad."

~~~java
if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
    throw new IllegalArgumentException("Invalid username");
}
~~~

### 2. Sanitization vs Validation

Validation rejects input that doesn't meet a rule; sanitization modifies input to make it safe, like stripping HTML tags, instead of rejecting it outright. Sanitization is useful when some transformation of the input is genuinely valid (rich text), but it's easy to get wrong -- validation, which just says yes or no, has a much smaller surface for mistakes.

~~~java
// Sanitize: strip disallowed tags, keep the rest
String clean = Jsoup.clean(userHtml, Safelist.basic());
~~~
