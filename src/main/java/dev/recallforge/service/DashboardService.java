package dev.recallforge.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import dev.recallforge.dto.DashboardResponse;
import dev.recallforge.dto.KnowledgeAreaResponse;
import dev.recallforge.dto.MarkdownSummaryDto;
import dev.recallforge.dto.WeakAreaResponse;
import dev.recallforge.repository.TopicRepository;

@Service
public class DashboardService {

    private final TopicRepository topicRepository;
    private final RewardService rewardService;

    public DashboardService(TopicRepository topicRepository, RewardService rewardService) {
        this.topicRepository = topicRepository;
        this.rewardService = rewardService;
    }
    
    public DashboardResponse getDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime tomorrowEnd = tomorrowStart.plusDays(1);
        LocalDateTime weekEnd = now.plusDays(7);

        long dueNow = topicRepository.countByNextReviewAtLessThanEqual(now);
        long dueTomorrow = topicRepository.countByNextReviewAtBetween(tomorrowStart, tomorrowEnd);
        long dueThisWeek = topicRepository.countByNextReviewAtBetween(tomorrowStart, weekEnd);
        long totalTopics = topicRepository.count();

        List<WeakAreaResponse> weakAreas = topicRepository.findWeakAreas(now)
            .stream()
            .limit(3)
            .map(area -> new WeakAreaResponse(
                area.getEnvironment(),
                area.getCategory(),
                area.getSubcategory(),
                area.getDueCount(),
                area.getAverageMemoryScore()
            ))
            .toList();

        List<KnowledgeAreaResponse> strongKnowledgeAreas = topicRepository.findStrongKnowledgeAreas()
            .stream()
            .limit(2)
            .map(area -> new KnowledgeAreaResponse(
                area.getTitle(), 
                area.getAverageMemoryScore()
            ))
            .toList();

        List<KnowledgeAreaResponse> weakKnowledgeAreas = topicRepository.findWeakKnowledgeAreas()
            .stream()
            .limit(2)
            .map(area -> new KnowledgeAreaResponse(
                area.getTitle(), 
                area.getAverageMemoryScore()
            ))
            .toList();

        return new DashboardResponse(
            dueNow,
            dueTomorrow, 
            dueThisWeek,
            totalTopics,
            weakAreas,
            strongKnowledgeAreas,
            weakKnowledgeAreas,
            rewardService.getDefaultUserReward()
        );
    }

    public List<MarkdownSummaryDto> getMarkdownSummaries() {
        return topicRepository.findMarkdownSummaries(LocalDateTime.now());
    }
}
