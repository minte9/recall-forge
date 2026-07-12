package dev.recallforge.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.domain.Topic;
import dev.recallforge.domain.UserAccount;
import dev.recallforge.exception.TemplateUserOperationException;
import dev.recallforge.repository.MarkdownFileRepository;
import dev.recallforge.repository.ReviewRepository;
import dev.recallforge.repository.TopicRepository;
import dev.recallforge.repository.UserAccountRepository;
import dev.recallforge.repository.UserRewardRepository;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private MarkdownFileRepository markdownFileRepository;

    @Mock
    private UserRewardRepository userRewardRepository;

    @Mock
    private OAuth2User oauthUser;

    private CurrentUserService currentUserService;

    private void setUp() {
        currentUserService = new CurrentUserService(
            userAccountRepository,
            reviewRepository,
            topicRepository,
            markdownFileRepository,
            userRewardRepository
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateWith(OAuth2User principal, String provider) {
        OAuth2AuthenticationToken token =
            new OAuth2AuthenticationToken(principal, List.of(), provider);
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Test
    void currentUser_googlePrincipalWithSubAttribute_looksUpBySubAsProviderUserId() {
        setUp();
        when(oauthUser.getAttribute("sub")).thenReturn("google-123");
        authenticateWith(oauthUser, "google");
        UserAccount user = new UserAccount();
        when(userAccountRepository.findByProviderAndProviderUserId("google", "google-123"))
            .thenReturn(Optional.of(user));

        UserAccount result = currentUserService.currentUser();

        assertThat(result).isSameAs(user);
    }

    @Test
    void currentUser_githubPrincipalWithoutSub_fallsBackToIdAttribute() {
        setUp();
        when(oauthUser.getAttribute("sub")).thenReturn(null);
        when(oauthUser.getAttribute("id")).thenReturn(42);
        authenticateWith(oauthUser, "github");
        UserAccount user = new UserAccount();
        when(userAccountRepository.findByProviderAndProviderUserId("github", "42"))
            .thenReturn(Optional.of(user));

        UserAccount result = currentUserService.currentUser();

        assertThat(result).isSameAs(user);
    }

    @Test
    void currentUser_missingSubAndIdAttributes_throwsIllegalStateException() {
        setUp();
        when(oauthUser.getAttribute("sub")).thenReturn(null);
        when(oauthUser.getAttribute("id")).thenReturn(null);
        authenticateWith(oauthUser, "google");

        assertThatThrownBy(() -> currentUserService.currentUser())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Missing OAuth provider user id");
    }

    @Test
    void currentUser_notOAuth2Authenticated_throwsIllegalStateException() {
        setUp();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("user", "password")
        );

        assertThatThrownBy(() -> currentUserService.currentUser())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not OAuth2 authenticated");
    }

    @Test
    void currentUser_noUserAccountForProviderAndId_throwsIllegalStateException() {
        setUp();
        when(oauthUser.getAttribute("sub")).thenReturn("google-999");
        authenticateWith(oauthUser, "google");
        when(userAccountRepository.findByProviderAndProviderUserId("google", "google-999"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> currentUserService.currentUser())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("User not found in database");
    }

    @Test
    void currentUserId_delegatesToCurrentUserAndReturnsItsId() {
        setUp();
        when(oauthUser.getAttribute("sub")).thenReturn("google-7");
        authenticateWith(oauthUser, "google");
        UserAccount user = new UserAccount();
        user.setId(7L);
        when(userAccountRepository.findByProviderAndProviderUserId("google", "google-7"))
            .thenReturn(Optional.of(user));

        Long id = currentUserService.currentUserId();

        assertThat(id).isEqualTo(7L);
    }

    @Test
    void resetCurrentUserLearningData_templateUser_throwsTemplateUserOperationException() {
        setUp();
        when(oauthUser.getAttribute("sub")).thenReturn("google-1");
        authenticateWith(oauthUser, "google");
        UserAccount templateUser = new UserAccount();
        templateUser.setId(1L);
        when(userAccountRepository.findByProviderAndProviderUserId("google", "google-1"))
            .thenReturn(Optional.of(templateUser));

        assertThatThrownBy(() -> currentUserService.resetCurrentUserLearningData())
            .isInstanceOf(TemplateUserOperationException.class)
            .hasMessageContaining("Template user (1) cannot reset learning data.");
    }

    @Test
    void resetCurrentUserReviewsForMarkdownFile_ownedFile_deletesReviewsAndResetsTopicProgress() {
        setUp();
        when(oauthUser.getAttribute("sub")).thenReturn("google-7");
        authenticateWith(oauthUser, "google");
        UserAccount user = new UserAccount();
        user.setId(7L);
        when(userAccountRepository.findByProviderAndProviderUserId("google", "google-7"))
            .thenReturn(Optional.of(user));

        MarkdownFile markdownFile = mock(MarkdownFile.class);
        when(markdownFileRepository.findByIdAndUserId(10L, 7L)).thenReturn(Optional.of(markdownFile));

        Topic topic1 = mock(Topic.class);
        Topic topic2 = mock(Topic.class);
        List<Topic> topics = List.of(topic1, topic2);
        when(topicRepository.findTopicsByMarkdownFileId(10L, 7L)).thenReturn(topics);

        currentUserService.resetCurrentUserReviewsForMarkdownFile(10L);

        verify(reviewRepository).deleteByTopicMarkdownFileIdAndUserId(10L, 7L);
        verify(topic1).resetProgress();
        verify(topic2).resetProgress();
        verify(topicRepository).saveAll(topics);
    }

    @Test
    void resetCurrentUserReviewsForMarkdownFile_fileNotOwnedOrMissing_throwsIllegalArgumentException() {
        setUp();
        when(oauthUser.getAttribute("sub")).thenReturn("google-7");
        authenticateWith(oauthUser, "google");
        UserAccount user = new UserAccount();
        user.setId(7L);
        when(userAccountRepository.findByProviderAndProviderUserId("google", "google-7"))
            .thenReturn(Optional.of(user));

        when(markdownFileRepository.findByIdAndUserId(99L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currentUserService.resetCurrentUserReviewsForMarkdownFile(99L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Markdown file not found: 99");

        verify(reviewRepository, never()).deleteByTopicMarkdownFileIdAndUserId(any(), any());
        verify(topicRepository, never()).saveAll(any());
    }
}
