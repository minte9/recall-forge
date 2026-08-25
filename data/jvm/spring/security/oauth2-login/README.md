# JVM / Spring / Security / OAuth2 Login

## 1. OAuth2 Login Basics

oauth2Login() delegates authentication entirely to an external provider (Google, GitHub) instead of a local username/password form -- Spring Security handles the redirect to the provider, the callback, and token exchange automatically once client-id/secret are configured. The user never enters a password your application ever sees or stores.

```java
http.oauth2Login(oauth2 -> oauth2
    .loginPage("/login") // custom login page listing provider buttons
);

// application.properties
// spring.security.oauth2.client.registration.google.client-id=...
// spring.security.oauth2.client.registration.google.client-secret=...
```


## 2. OAuth2User and Provider Attributes

After a successful login, the authenticated principal is an OAuth2User whose attributes map depend entirely on the provider -- Google exposes sub as the stable user ID, GitHub exposes id instead. Reading the wrong attribute name for a given provider is a common bug when supporting more than one login provider.

```java
OAuth2User principal = (OAuth2User) authentication.getPrincipal();
String googleId = (String) principal.getAttribute("sub");  // Google's stable ID
Object githubId = principal.getAttribute("id");             // GitHub uses a different key
```


## 3. Mapping to Your Own User Domain

A custom OAuth2UserService implementation is the usual place to look up or create your own UserAccount entity from the provider's attributes, keyed by (provider, providerUserId) since email alone isn't a reliable stable identifier across all providers. This keeps your domain model independent of any single OAuth provider's attribute shape.

```java
@Service
class DatabaseOAuth2UserService extends DefaultOAuth2UserService {
    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oauth2User = super.loadUser(request);
        // look up or create a UserAccount keyed by (provider, providerUserId)
        return oauth2User;
    }
}
```
