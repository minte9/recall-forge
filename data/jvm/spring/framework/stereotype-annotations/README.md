# JVM / Spring / Framework / Stereotype Annotations

## 1. @Controller and Web-Layer Semantics

`@Controller` handles HTTP requests and returns view names to render. `@RestController` = `@Controller` + `@ResponseBody` -- every method's return value is written straight to the response body (usually as JSON). Both are just `@Component` underneath, but the choice signals the class's role and enables `@GetMapping`/`@PostMapping`.

```java
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // = @Controller + @ResponseBody on every method
class UserRestController {

    @GetMapping("/users/{id}")
    String getUser(String id) {
        return "User " + id; // written directly to the HTTP response body
    }
}

@Controller // returns a view name, not the response body directly
class UserPageController {

    @GetMapping("/users/{id}/page")
    String userPage(String id) {
        return "user-details"; // resolved to a view template, e.g. user-details.html
    }
}
```


## 2. @Service and @Repository Semantics

`@Service` marks the business-logic layer -- operations that coordinate repositories or other services. `@Repository` marks the data-access layer, wrapping persistence calls. Neither changes behavior beyond plain `@Component` (except `@Repository`'s exception translation, next topic) -- they're mainly there to signal architecture.

```java
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository // data-access layer
class UserRepository {
    String findNameById(int id) {
        return "Ann"; // stands in for a real database call
    }
}

@Service // business-logic layer
class UserService {
    private final UserRepository userRepository;

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    String greetUser(int id) {
        return "Hello, " + userRepository.findNameById(id);
    }
}
```


## 3. Exception Translation on @Repository

`@Repository` does have one real behavioral difference: Spring wraps its exceptions with "exception translation," converting low-level ones (JDBC's `SQLException`, JPA's `PersistenceException`) into a consistent `DataAccessException` hierarchy. Calling code can catch one technology-agnostic exception type, without caring whether the repository uses JDBC, JPA, or something else.

```java
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
class AccountRepository {
    void save(String account) {
        // A real driver-specific exception here gets translated by Spring
        // into a DataAccessException before it reaches the caller.
        throw new DataAccessException("Simulated persistence failure") {};
    }
}

@Service
class AccountService {
    private final AccountRepository accountRepository;

    AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    void createAccount(String account) {
        try {
            accountRepository.save(account);
        } catch (DataAccessException e) { // one exception type, regardless of persistence tech
            System.out.println("Failed to save account: " + e.getMessage());
        }
    }
}
```
