package dev.recallforge.security;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.recallforge.domain.Topic;
import dev.recallforge.domain.UserAccount;
import dev.recallforge.repository.MarkdownFileRepository;
import dev.recallforge.repository.ReviewRepository;
import dev.recallforge.repository.TopicRepository;
import dev.recallforge.repository.UserAccountRepository;
import dev.recallforge.repository.UserRewardRepository;

@Service
public class CurrentUserService {

    private final UserAccountRepository userAccountRepository;
    private final ReviewRepository reviewRepository;
    private final TopicRepository topicRepository;
    private final MarkdownFileRepository markdownFileRepository;
    private final UserRewardRepository userRewardRepository;

    public CurrentUserService(
            UserAccountRepository userAccountRepository,
            ReviewRepository reviewRepository,
            TopicRepository topicRepository,
            MarkdownFileRepository markdownFileRepository,
            UserRewardRepository userRewardRepository) {

        this.userAccountRepository = userAccountRepository;
        this.reviewRepository = reviewRepository;
        this.topicRepository = topicRepository;
        this.markdownFileRepository = markdownFileRepository;
        this.userRewardRepository = userRewardRepository;
    }

    public UserAccount currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

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

    @Transactional
    public void resetCurrentUserLearningData() {
        UserAccount user = currentUser();
        Long userId = user.getId();

        reviewRepository.deleteByUserId(userId);
        topicRepository.deleteByUserId(userId);
        markdownFileRepository.deleteByUserId(userId);
        userRewardRepository.deleteByUserId(userId);
    }

    @Transactional
    public void resetCurrentUserReviews() {
        UserAccount user = currentUser();
        Long userId = user.getId();

        reviewRepository.deleteByUserId(userId);
        userRewardRepository.deleteByUserId(userId);

        List<Topic> topics = topicRepository.findByUserId(userId);
        topics.forEach(Topic::resetProgress);
        topicRepository.saveAll(topics);
    }
}