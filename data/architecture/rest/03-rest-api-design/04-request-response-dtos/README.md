# API / REST / REST API Design / Request and Response DTOs

## 1. Entity vs DTO

An entity models persistence (JPA annotations, lazy relations, internal
fields); a DTO models the API contract. Exposing entities couples the API
to the schema, leaks fields like password hashes, and triggers lazy-loading
surprises during serialization.

```java
@Entity class User { Long id; String email; String passwordHash; }
record UserResponse(Long id, String email) {}   // hash never leaves
```

## 2. Request and Response DTOs

Use separate types for input and output. The request DTO has only what the
client may set (no id, no audit fields); the response DTO has what the
client may see. This prevents mass-assignment bugs where a client sets
`role` or `id`.

```java
record CreateUserRequest(String email, String password) {}
record UserResponse(Long id, String email, Instant createdAt) {}
```

## 3. Mapping Between Objects

Keep mapping explicit and close to the boundary, as a static factory, a
small mapper class or MapStruct for large models. Controllers speak DTOs,
services speak entities or domain objects.

```java
record UserResponse(Long id, String email) {
    static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail());
    }
}
```
