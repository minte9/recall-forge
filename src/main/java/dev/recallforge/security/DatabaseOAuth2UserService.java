package dev.recallforge.security;

import dev.recallforge.domain.UserAccount;
import dev.recallforge.repository.UserAccountRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class DatabaseOAuth2UserService
    implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UserAccountRepository userAccountRepository;

    public DatabaseOAuth2UserService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
        throws OAuth2AuthenticationException {

        OAuth2User oauthUser = delegate.loadUser(userRequest);

        String provider = userRequest
            .getClientRegistration()
            .getRegistrationId();

        Map<String, Object> attrs = oauthUser.getAttributes();

        String providerUserId = extractProviderUserId(provider, attrs);
        String email = extractEmail(provider, attrs);
        String displayName = extractDisplayName(provider, attrs);
        String avatarUrl = extractAvatarUrl(provider, attrs);

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

        return oauthUser;
    }

    private String extractProviderUserId(String provider, Map<String, Object> attrs) {
        Object id = attrs.get("sub"); // Google

        if (id == null) {
            id = attrs.get("id"); // GitHub

            // https://recallforge.dev/login/oauth2/code/github
            // http://localhost:9090/login/oauth2/code/github
        }

        if (id == null) {
            throw new OAuth2AuthenticationException("Missing OAuth provider user id");
        }

        return id.toString();
    }

    private String extractEmail(String provider, Map<String, Object> attrs) {
        Object email = attrs.get("email");
        return email == null ? null : email.toString();
    }

    private String extractDisplayName(String provider, Map<String, Object> attrs) {
        Object name = attrs.get("name");

        if (name == null) {
            name = attrs.get("login"); // GitHub username
        }

        return name == null ? null : name.toString();
    }

    private String extractAvatarUrl(String provider, Map<String, Object> attrs) {
        Object avatar = attrs.get("picture"); // Google

        if (avatar == null) {
            avatar = attrs.get("avatar_url"); // GitHub
        }

        return avatar == null ? null : avatar.toString();
    }
}