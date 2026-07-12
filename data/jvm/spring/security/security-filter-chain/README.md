## JVM / Spring / Security / SecurityFilterChain

### 1. SecurityFilterChain Basics

SecurityFilterChain (a @Bean method returning it) is the modern way to configure Spring Security, replacing the older WebSecurityConfigurerAdapter subclassing style. It builds a chain of servlet filters that intercept every request -- authentication, CSRF checks, authorization -- configured through a fluent HttpSecurity builder rather than method overrides.

~~~java
@Configuration
class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/login").permitAll()
            .anyRequest().authenticated()
        );
        return http.build();
    }
}
~~~


### 2. Authorizing Requests

authorizeHttpRequests() rules are evaluated in order, and the first matching rule wins -- so specific paths (like /admin/**) must come before broader catch-alls (anyRequest()). permitAll() skips authentication entirely; authenticated() requires a logged-in user; hasRole("ADMIN") additionally requires a specific authority.

~~~java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/admin/**").hasRole("ADMIN") // must come before the catch-all below
    .requestMatchers("/", "/public/**").permitAll()
    .anyRequest().authenticated()
);
~~~


### 3. Common Configuration Mistakes

A frequent mistake is ordering a broad permitAll() rule before a more specific restricted one -- since the first match wins, the broad rule silently overrides the restriction that was meant to apply. Another is forgetting anyRequest().authenticated() at the end, which leaves unmatched paths open to anyone by default.

~~~java
// Bug: /admin/secret is reachable by anyone, because permitAll() matches first
http.authorizeHttpRequests(auth -> auth
    .anyRequest().permitAll()                      // too broad, placed first
    .requestMatchers("/admin/**").hasRole("ADMIN") // never actually reached
);
~~~
