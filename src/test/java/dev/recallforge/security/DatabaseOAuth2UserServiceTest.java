package dev.recallforge.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import dev.recallforge.domain.UserAccount;
import dev.recallforge.repository.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
class DatabaseOAuth2UserServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private DefaultOAuth2UserService delegate;

    @Mock
    private OAuth2UserRequest userRequest;

    @Mock
    private ClientRegistration clientRegistration;

    @Mock
    private OAuth2User oauthUser;

    private DatabaseOAuth2UserService service;

    private void setUp(String provider) {
        service = new DatabaseOAuth2UserService(userAccountRepository);
        ReflectionTestUtils.setField(service, "delegate", delegate);

        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(provider);
        when(delegate.loadUser(userRequest)).thenReturn(oauthUser);
    }

    @Test
    void loadUser_newGoogleUser_createsUserAccountFromSubEmailNamePicture() {
        setUp("google");
        when(oauthUser.getAttributes()).thenReturn(Map.of(
            "sub", "g-123",
            "email", "alice@example.com",
            "name", "Alice",
            "picture", "http://pic/alice.png"
        ));
        when(userAccountRepository.findByProviderAndProviderUserId("google", "g-123"))
            .thenReturn(Optional.empty());

        OAuth2User result = service.loadUser(userRequest);

        assertThat(result).isSameAs(oauthUser);
        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        UserAccount saved = captor.getValue();
        assertThat(saved.getProvider()).isEqualTo("google");
        assertThat(saved.getProviderUserId()).isEqualTo("g-123");
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
        assertThat(saved.getDisplayName()).isEqualTo("Alice");
        assertThat(saved.getAvatarUrl()).isEqualTo("http://pic/alice.png");
        assertThat(saved.getLastLoginAt()).isNotNull();
    }

    @Test
    void loadUser_newGithubUser_fallsBackToIdLoginAndAvatarUrl() {
        setUp("github");
        when(oauthUser.getAttributes()).thenReturn(Map.of(
            "id", 456,
            "login", "alice-gh",
            "avatar_url", "http://avatar/alice.png"
        ));
        when(userAccountRepository.findByProviderAndProviderUserId("github", "456"))
            .thenReturn(Optional.empty());

        service.loadUser(userRequest);

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        UserAccount saved = captor.getValue();
        assertThat(saved.getProviderUserId()).isEqualTo("456");
        assertThat(saved.getDisplayName()).isEqualTo("alice-gh");
        assertThat(saved.getAvatarUrl()).isEqualTo("http://avatar/alice.png");
        assertThat(saved.getEmail()).isNull();
    }

    @Test
    void loadUser_existingUser_updatesSameUserAccountInstance() {
        setUp("google");
        when(oauthUser.getAttributes()).thenReturn(Map.of(
            "sub", "g-123",
            "email", "new@example.com",
            "name", "Alice Updated"
        ));
        UserAccount existing = new UserAccount();
        existing.setId(9L);
        existing.setEmail("old@example.com");
        existing.setLastLoginAt(Instant.EPOCH);
        when(userAccountRepository.findByProviderAndProviderUserId("google", "g-123"))
            .thenReturn(Optional.of(existing));

        service.loadUser(userRequest);

        verify(userAccountRepository).save(existing);
        assertThat(existing.getEmail()).isEqualTo("new@example.com");
        assertThat(existing.getDisplayName()).isEqualTo("Alice Updated");
        assertThat(existing.getLastLoginAt()).isAfter(Instant.EPOCH);
    }

    @Test
    void loadUser_missingSubAndIdAttributes_throwsOAuth2AuthenticationException() {
        setUp("google");
        when(oauthUser.getAttributes()).thenReturn(Map.of("email", "alice@example.com"));

        assertThatThrownBy(() -> service.loadUser(userRequest))
            .isInstanceOf(OAuth2AuthenticationException.class)
            .extracting(ex -> ((OAuth2AuthenticationException) ex).getError().getErrorCode())
            .isEqualTo("Missing OAuth provider user id");

        verify(userAccountRepository, never()).save(any());
    }
}
