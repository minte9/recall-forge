## JVM / Spring / Data / Repositories

### 1. JpaRepository and CrudRepository

JpaRepository<T, ID> is Spring Data's main repository interface -- extend it with your entity type and ID type, and you get save(), findById(), findAll(), delete(), and more with zero implementation code. It extends CrudRepository (basic CRUD) and PagingAndSortingRepository, adding JPA-specific extras like flush() and batch deletes.

~~~java
interface UserRepository extends JpaRepository<User, Long> {
    // No implementation needed -- save(), findById(), findAll(), delete() all just work
}

@Service
class UserService {
    private final UserRepository userRepository;
    UserService(UserRepository userRepository) { this.userRepository = userRepository; }

    User create(User user) { return userRepository.save(user); } // inherited, no code written
}
~~~


### 2. What Spring Data Generates For You

At startup, Spring Data creates a dynamic proxy implementing your repository interface -- there's no hand-written implementation class anywhere. This is possible because JpaRepository's inherited methods are generic enough to implement once for any entity, and custom finder methods are parsed from their name (see Derived Queries).

~~~java
interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data implements this interface at runtime with a dynamic proxy --
    // you'll never see or write "class UserRepositoryImpl implements UserRepository"
}
~~~


### 3. Custom Repository Methods

Beyond the inherited CRUD methods, a repository interface can declare its own finder methods, either derived from the method name or backed by @Query -- Spring Data implements both the same way, at startup, without you writing a method body. This is what makes repositories feel like plain interfaces despite doing real work.

~~~java
interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // derived query, no body needed
}
~~~
