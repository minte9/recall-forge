package dev.recallforge.controller;

import dev.recallforge.config.AppAiProperties;
import dev.recallforge.config.AppMarkdownLimitProperties;
import dev.recallforge.config.AppReviewLimitProperties;
import dev.recallforge.domain.UserAccount;
import dev.recallforge.dto.CurrentUserResponse;
import dev.recallforge.security.CurrentUserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api")
public class CurrentUserController {

    private final CurrentUserService currentUserService;
    private final AppReviewLimitProperties reviewLimitProperties;
    private final AppAiProperties aiProperties;
    private final AppMarkdownLimitProperties markdownLimitProperties;

    public CurrentUserController(
            CurrentUserService currentUserService,
            AppReviewLimitProperties reviewLimitProperties,
            AppAiProperties aiProperties,
            AppMarkdownLimitProperties markdownLimitProperties) {
        this.currentUserService = currentUserService;
        this.reviewLimitProperties = reviewLimitProperties;
        this.aiProperties = aiProperties;
        this.markdownLimitProperties = markdownLimitProperties;
    }

    @GetMapping("/me")
    public CurrentUserResponse me() {
        UserAccount user = currentUserService.currentUser();

        return new CurrentUserResponse(
            user.getId(),
            user.getEmail(),
            user.isAdmin(),
            user.getDisplayName(),
            user.getRole().name(),
            user.getProvider(),
            user.getAvatarUrl(),
            reviewLimitProperties.dailyLimit(),
            reviewLimitProperties.dailyQuestionLimit(),
            reviewLimitProperties.dailyUserReviewLimit(),
            reviewLimitProperties.dailyUserQuestionLimit(),
            markdownLimitProperties.dailyOptimizeLimit(),
            markdownLimitProperties.maxTopicsPerUser(),
            aiProperties.enabled()
        );
    }

    @DeleteMapping("/me/learning-data")
    public ResponseEntity<Void> resetLearningData() {
        currentUserService.resetCurrentUserLearningData();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/reviews-data")
    public ResponseEntity<Void> resetReviewsData() {
        currentUserService.resetCurrentUserReviews();
        return ResponseEntity.noContent().build();
    }
}