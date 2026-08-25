# JVM / Spring / Boot / Boot Testing

## 1. @SpringBootTest vs Test Slices

`@SpringBootTest` loads the full application context -- realistic, but slow since it wires everything. Test slices load only a relevant part: `@WebMvcTest` loads just the web layer, `@DataJpaTest` just the JPA layer. Prefer a slice when testing one layer in isolation; reach for the full context only when layer interaction matters.

```java
@SpringBootTest // full context -- use when you actually need the whole app wired
class OrderServiceIntegrationTest {
    @Autowired OrderService orderService;
}
```


## 2. @WebMvcTest and MockMvc

`@WebMvcTest(MyController.class)` loads only the web layer -- controllers, filters, `@ControllerAdvice` -- skipping the service/repository beans entirely, which must be mocked. `MockMvc` then drives HTTP-like requests against the controller without starting a real server, making these tests fast.

```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired MockMvc mockMvc;

    @Test
    void getUser_returnsOk() throws Exception {
        mockMvc.perform(get("/users/1")).andExpect(status().isOk()); // no real server started
    }
}
```


## 3. @MockBean/@MockitoBean for Isolating Dependencies

`@MockBean` (or `@MockitoBean` in newer Spring Boot) replaces a real bean in the test's application context with a Mockito mock, so a slice test can stub out dependencies it doesn't want to exercise for real -- like a repository or an external API client.

```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    @MockitoBean UserService userService; // replaces the real bean in the test context

    @Test
    void getUser_returnsStubbedResult() throws Exception {
        when(userService.findById(1)).thenReturn(new User("Ann"));
        mockMvc.perform(get("/users/1"));
    }
}
```
