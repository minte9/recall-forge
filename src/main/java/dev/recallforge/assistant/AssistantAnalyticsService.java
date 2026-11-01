package dev.recallforge.assistant;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.recallforge.repository.ReviewRepository;

@Service
public class AssistantAnalyticsService {

    private final ReviewRepository reviews;

    public AssistantAnalyticsService(ReviewRepository reviews) {
        this.reviews = reviews;
    }

    @Transactional(readOnly = true)
    public List<MistakeDto> getMistakes(
            Long userId,
            String category) {

        return reviews.findRecentMistakes(
            userId,
            category,
            PageRequest.of(0, 20)
        );
    }
}