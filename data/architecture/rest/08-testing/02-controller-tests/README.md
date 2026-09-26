# API / REST / Testing / Controller Tests

## 1. MockMvc

`MockMvc` sends simulated HTTP requests through the real Spring MVC
machinery (mapping, conversion, validation, advice) without starting a
server. It's fast and tests exactly the HTTP contract of a controller.

```java
mockMvc.perform(get("/books/42").accept(MediaType.APPLICATION_JSON))
       .andExpect(status().isOk());
```

## 2. Testing Controllers with @WebMvcTest

`@WebMvcTest(BookController.class)` loads only the web layer for that
controller; collaborators like services are replaced with
`@MockitoBean`. You test HTTP mapping and error handling, not business
logic.

```java
@WebMvcTest(BookController.class)
class BookControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean BookService service;     // stubbed collaborator
}
```

## 3. Verifying Status and JSON

Assert the status, headers and body. `jsonPath` checks individual fields
without coupling the test to the whole payload; also cover the error path
(400/404) because it's part of the contract too.

```java
when(service.get(42L)).thenReturn(new Book(42L, "Dune"));

mockMvc.perform(get("/books/42"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.title").value("Dune"));

mockMvc.perform(post("/books").contentType(APPLICATION_JSON)
        .content("{\"title\":\"\"}"))
    .andExpect(status().isBadRequest());     // validation path
```
