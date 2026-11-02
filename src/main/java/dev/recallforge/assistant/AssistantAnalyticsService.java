package dev.recallforge.assistant;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.recallforge.repository.ReviewRepository;
import dev.recallforge.repository.TopicRepository;
import dev.recallforge.service.RewardService;


@Service
public class AssistantAnalyticsService {

    private final ReviewRepository reviewRepository;
    private final TopicRepository topicRepository;


    public AssistantAnalyticsService(
            ReviewRepository reviewRepository,
            TopicRepository topicRepository) {

        this.reviewRepository = reviewRepository;
        this.topicRepository = topicRepository;
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

    @Transactional(readOnly = true)
    public ProgressStatisticsDto getProgressStatistics(
            Long userId,
            String category) {

        String categoryFilter =
            category == null || category.isBlank()
                ? null
                : category.trim();

        YearMonth current = YearMonth.now();
        YearMonth previous = current.minusMonths(1);

        ReviewStatsProjection currentStats =
            reviewRepository.getReviewStatistics(
                userId,
                categoryFilter,
                current.atDay(1).atStartOfDay(),
                current.plusMonths(1).atDay(1).atStartOfDay()
            );

        ReviewStatsProjection previousStats =
            reviewRepository.getReviewStatistics(
                userId,
                categoryFilter,
                previous.atDay(1).atStartOfDay(),
                current.atDay(1).atStartOfDay()
            );

        var currentMonth = toMonthlyStats(
            current.toString(), currentStats
        );

        var previousMonth = toMonthlyStats(
            previous.toString(), previousStats
        );

        Double scoreChange = null;

        if (currentMonth.averageScore() != null
                && previousMonth.averageScore() != null) {

            scoreChange =
                currentMonth.averageScore()
                - previousMonth.averageScore();
        }

        return new ProgressStatisticsDto(
            categoryFilter == null ? "All" : categoryFilter,
            previousMonth,
            currentMonth,
            scoreChange,
            Math.toIntExact(
                currentMonth.reviewCount()
                - previousMonth.reviewCount()
            )
        );
    }

    private ProgressStatisticsDto.MonthlyStats toMonthlyStats(
            String period,
            ReviewStatsProjection stats) {

        return new ProgressStatisticsDto.MonthlyStats(
            period,
            stats.getReviewCount(),
            stats.getAverageScore(),
            stats.getHighScoreRate()
        );
    }

    @Transactional(readOnly = true)
    public List<KnowledgeAreaDto> getKnowledgeAreas(
            Long userId,
            String category) {

        String categoryFilter =
            category == null || category.isBlank()
                ? null
                : category.trim();

        return topicRepository.findKnowledgeAreas(
            userId,
            categoryFilter,
            RewardService.MASTERY_MEMORY_SCORE,
            RewardService.MASTERY_MIN_STREAK,
            LocalDateTime.now()
        );
    }
}