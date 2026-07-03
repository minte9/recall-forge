# Recall Forge

### 5.6 App user integration

Add current user service.

src/main/java/dev/recallforge/security/CurrentUserService.java

~~~java
package dev.recallforge.security;

import dev.recallforge.domain.UserAccount;
import dev.recallforge.repository.UserAccountRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserAccountRepository userAccountRepository;

    public CurrentUserService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public UserAccount currentUser() {
        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();

        if (!(auth instanceof OAuth2AuthenticationToken token)) {
            throw new IllegalStateException("Current user is not OAuth2 authenticated");
        }

        OAuth2User oauthUser = token.getPrincipal();

        String provider = token.getAuthorizedClientRegistrationId();
        String providerUserId = extractProviderUserId(oauthUser);

        return userAccountRepository
            .findByProviderAndProviderUserId(provider, providerUserId)
            .orElseThrow(() -> new IllegalStateException("User not found in database"));
    }

    public Long currentUserId() {
        return currentUser().getId();
    }

    private String extractProviderUserId(OAuth2User oauthUser) {
        Object id = oauthUser.getAttribute("sub"); // Google

        if (id == null) {
            id = oauthUser.getAttribute("id"); // GitHub
        }

        if (id == null) {
            throw new IllegalStateException("Missing OAuth provider user id");
        }

        return id.toString();
    }
}
~~~

Add a temporary debug endpoint:

~~~java
package dev.recallforge.controller;

import dev.recallforge.domain.UserAccount;
import dev.recallforge.security.CurrentUserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    private final CurrentUserService currentUserService;

    public MeController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/api/me")
    public UserAccount me() {
        return currentUserService.currentUser();
    }
}
~~~


### 5.7 Demo layout

- Left panel OAuth login
- Right panel Static demo layout



## Architecture

![Screenshot](docs/images/recall-forge_architecture.png)

## Screenshots

![Screenshot](docs/images/recall-forge_4.png)