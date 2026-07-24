package dev.recallforge.security;

import dev.recallforge.domain.UserAccount;
import dev.recallforge.repository.UserAccountRepository;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

/**
 * OIDC login path (Google): delegates to Spring's {@link OidcUserService}
 * to fetch/validate the ID token, then upserts a {@link UserAccount} keyed
 * by {@code (provider, providerUserId)}. See {@link DatabaseOAuth2UserService}
 * for the plain-OAuth2 (GitHub) equivalent.
 */
@Service
public class DatabaseOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();
    private final UserAccountRepository userAccountRepository;

    public DatabaseOidcUserService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attrs = oidcUser.getAttributes();

        String providerUserId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String displayName = oidcUser.getFullName();
        String avatarUrl = attrs.get("picture") == null ? null : attrs.get("picture").toString();

        UserAccount user = userAccountRepository
            .findByProviderAndProviderUserId(provider, providerUserId)
            .orElseGet(UserAccount::new);

        user.setProvider(provider);
        user.setProviderUserId(providerUserId);
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setAvatarUrl(avatarUrl);
        user.setLastLoginAt(Instant.now());

        userAccountRepository.save(user);

        return oidcUser;
    }
}