package dev.recallforge.security;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import dev.recallforge.domain.UserAccount;
import dev.recallforge.repository.UserAccountRepository;

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

    public void requireAdmin() {
        UserAccount user = currentUser();

        if (!user.isAdmin()) {
            throw new AccessDeniedException("Admin access required");
        }
    }
}