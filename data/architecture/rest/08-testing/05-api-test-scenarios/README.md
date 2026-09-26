# API / REST / Testing / API Test Scenarios

## 1. Success Scenarios

For each endpoint, test the happy path end to end: correct status (200,
201, 204), key response fields, and headers that are part of the contract
(`Location`, `Content-Type`). Then check the side effect, not just the
response.

```java
mockMvc.perform(post("/books").contentType(APPLICATION_JSON)
        .content("{\"title\":\"Dune\",\"authorId\":7}"))
    .andExpect(status().isCreated())
    .andExpect(header().exists("Location"));
// and: GET the Location returns the new book
```

## 2. Validation and Error Scenarios

Errors are part of the API contract: missing/invalid fields → 400 with
field details, unknown id → 404, conflicts → 409. Assert the error body
shape too, since clients parse it.

```java
mockMvc.perform(get("/books/999"))
    .andExpect(status().isNotFound())
    .andExpect(jsonPath("$.title").value("Not Found"));   // ProblemDetail
```

## 3. Authentication Scenarios

Test the security matrix: no credentials → 401, valid but insufficient
→ 403, correct role → success. Spring Security test support fakes users
or JWTs without a real login.

```java
mockMvc.perform(delete("/books/42"))
    .andExpect(status().isUnauthorized());
mockMvc.perform(delete("/books/42").with(jwt()))
    .andExpect(status().isForbidden());           // no ADMIN
mockMvc.perform(delete("/books/42")
        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
    .andExpect(status().isNoContent());
```
