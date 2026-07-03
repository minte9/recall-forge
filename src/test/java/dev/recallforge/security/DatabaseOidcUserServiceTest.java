package dev.recallforge.security;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;

import dev.recallforge.domain.UserAccount;
import dev.recallforge.repository.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
class DatabaseOidcUserServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private OidcUserService delegate;

    @Mock
    private OidcUserRequest userRequest;

    @Mock
    private ClientRegistration clientRegistration;

    @Mock
    private OidcUser oidcUser;

    private DatabaseOidcUserService service;

    private void setUp() {
        service = new DatabaseOidcUserService(userAccountRepository);
        ReflectionTestUtils.setField(service, "delegate", delegate);

        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn("google");
        when(delegate.loadUser(userRequest)).thenReturn(oidcUser);
    }

    @Test
    void loadUser_newUser_createsUserAccountFromSubjectEmailFullNameAndPicture() {
        setUp();
        when(oidcUser.getSubject()).thenReturn("g-123");
        when(oidcUser.getEmail()).thenReturn("alice@example.com");
        when(oidcUser.getFullName()).thenReturn("Alice");
        when(oidcUser.getAttributes()).thenReturn(Map.of("picture", "http://pic/alice.png"));
        when(userAccountRepository.findByProviderAndProviderUserId("google", "g-123"))
            .thenReturn(Optional.empty());

        OidcUser result = service.loadUser(userRequest);

        assertThat(result).isSameAs(oidcUser);
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
    void loadUser_noPictureAttribute_leavesAvatarUrlNull() {
        setUp();
        when(oidcUser.getSubject()).thenReturn("g-123");
        when(oidcUser.getEmail()).thenReturn("alice@example.com");
        when(oidcUser.getFullName()).thenReturn("Alice");
        when(oidcUser.getAttributes()).thenReturn(Map.of());
        when(userAccountRepository.findByProviderAndProviderUserId("google", "g-123"))
            .thenReturn(Optional.empty());

        service.loadUser(userRequest);

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getAvatarUrl()).isNull();
    }

    @Test
    void loadUser_existingUser_updatesSameUserAccountInstance() {
        setUp();
        when(oidcUser.getSubject()).thenReturn("g-123");
        when(oidcUser.getEmail()).thenReturn("new@example.com");
        when(oidcUser.getFullName()).thenReturn("Alice Updated");
        when(oidcUser.getAttributes()).thenReturn(Map.of("picture", "http://pic/new.png"));
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
        assertThat(existing.getAvatarUrl()).isEqualTo("http://pic/new.png");
        assertThat(existing.getLastLoginAt()).isAfter(Instant.EPOCH);
    }
}
