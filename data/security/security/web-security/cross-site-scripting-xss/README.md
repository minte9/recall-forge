# Security / Security / Web Security / Cross-Site Scripting (XSS)

## 1. Reflected vs Stored XSS

Reflected XSS injects a script through something like a URL parameter that's echoed straight back into the page -- it only fires when a victim clicks a crafted link. Stored XSS is more dangerous: the malicious script is saved on the server (a comment, a username) and served to every visitor who views that content, no crafted link needed.

```java
// Stored XSS: comment saved and rendered as-is later
comment.setBody(request.getParameter("body")); // <script>...
```

## 2. Output Encoding

Output encoding converts characters that have special meaning in HTML (`<`, `>`, `&`) into their safe entity equivalents right before rendering, so a stored `<script>` tag displays as literal text instead of executing. It has to happen at render time, on every output path -- encoding on input alone misses any data that enters through another route.

```java
String safe = HtmlUtils.htmlEscape(comment.getBody());
// <script> becomes &lt;script&gt; in the rendered page
```
