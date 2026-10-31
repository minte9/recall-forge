package dev.recallforge.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.recallforge.domain.Topic;
import dev.recallforge.dto.RecallInsightResponse;
import dev.recallforge.repository.ReviewRepository;
import dev.recallforge.repository.TopicRepository;
import dev.recallforge.security.CurrentUserService;

@Service
public class RecallInsightService {

    private final TopicRepository topicRepository;
    private final CurrentUserService currentUserService;
    private final ReviewRepository reviewRepository;

    public RecallInsightService(
            TopicRepository topicRepository,
            CurrentUserService currentUserService,
            ReviewRepository reviewRepository
        ) {

        this.topicRepository = topicRepository;
        this.currentUserService = currentUserService;
        this.reviewRepository = reviewRepository;
    }

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public RecallInsightResponse getInsight(String category) {

        Long userId = currentUserService.currentUserId();

        List<Topic> topics =
        topicRepository.findDueTopicsByCategory(
                userId,
                category,
                LocalDateTime.now()
        );

        if (topics.isEmpty()) {
            return new RecallInsightResponse(
                    category,
                    "No due topics in " + category + ".",
                    0, 0, 0, null, null, null, null
            );
        }

        long dueTopicCount = topics.size();

        // An actual review is required before we can infer
        // anything about the user's performance.
        // We will retrieve reviewed topic IDs separately.
        var reviewedTopicIds =
                reviewRepository.findReviewedTopicIds(userId, category);

        List<Topic> reviewedTopics = topics.stream()
                .filter(t -> reviewedTopicIds.contains(t.getId()))
                .toList();

        long reviewedCount = reviewedTopics.size();

        if (reviewedCount == 0) {
            return new RecallInsightResponse(
                    category,
                    "No reviewed topics are due in " + category + ".",
                    dueTopicCount,
                    0, 0, null, null, null, null
            );
        }

        double averageScore = reviewedTopics.stream()
                .mapToDouble(Topic::getMemoryScore)
                .average()
                .orElse(0);

        Topic weakest = reviewedTopics.get(0);

        String subcategory =
                weakest.getMarkdownFile().getSubcategory();

        String topicTitle =
                weakest.getTitle().replaceFirst("^\\d+\\.\\s*", "");

        String message = String.format(
                "Your weakest %s topic is %s (score: %.2f) in %s subcategory.",
                category,
                topicTitle,
                weakest.getMemoryScore(),
                subcategory              
        );

        return new RecallInsightResponse(
                category,
                message,
                dueTopicCount,
                reviewedCount,
                Math.round(averageScore * 100.0) / 100.0,
                topicTitle,
                subcategory,
                weakest.getId(),
                weakest.getMarkdownFile().getId()
        );
    }
}