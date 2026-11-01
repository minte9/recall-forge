package dev.recallforge.assistant;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.recallforge.repository.ReviewRepository;

@Service
public class AssistantAnalyticsService {

    private final ReviewRepository reviewRepository;

    public AssistantAnalyticsService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public List<MistakeDto> getMistakes(
            Long userId,
            String category) {

        return reviewRepository.findRecentMistakes(
            userId,
            category,
            PageRequest.of(0, 20)
        );
    }

    @Transactional(readOnly = true)
    public List<ReviewHistoryDto> getReviewHistory(
            Long userId,
            String category,
            int limit) {

        int safeLimit = Math.max(1, Math.min(limit, 10));

        String categoryFilter =
            category == null || category.isBlank()
                ? null
                : category.trim();

        return reviewRepository.findReviewHistory(
            userId,
            categoryFilter,
            PageRequest.of(0, safeLimit)
        );
    }
}