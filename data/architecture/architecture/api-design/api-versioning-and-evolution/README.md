## Architecture / Architecture / API Design / API Versioning & Evolution

### 1. Versioning Strategies

URI versioning (`/v1/orders`) is the most visible and cache-friendly, but forces clients onto a whole new path per change. Header versioning (`Accept: application/vnd.api.v2+json`) keeps the URL stable and lets client and server negotiate per request, at the cost of being far less discoverable when someone's just reading the URL.

~~~java
// URI versioning
GET /v1/orders/42

// Header versioning, same URL
GET /orders/42
Accept: application/vnd.api.v2+json
~~~

### 2. Backward Compatibility

A backward-compatible change only adds -- a new optional field, a new endpoint -- never removes or renames what's already there, so old clients keep working untouched. Renaming a field or tightening validation breaks that contract, which is why those changes belong behind a new version rather than a silent update to the existing one.

~~~java
// Backward-compatible: old clients ignore the new field
record OrderResponse(Long id, String status, String trackingUrl) {}
// trackingUrl added later -- old clients that ignore it still work
~~~
