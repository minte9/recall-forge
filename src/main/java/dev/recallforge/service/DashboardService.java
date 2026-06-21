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
import dev.recallforge.security.CurrentUserService;

@Service
public class DashboardService {

    private final TopicRepository topicRepository;
    private final RewardService rewardService;
    private final CurrentUserService currentUserService;

    public DashboardService(
        TopicRepository topicRepository,
        RewardService rewardService,
        CurrentUserService currentUserService
    ) {
        this.topicRepository = topicRepository;
        this.rewardService = rewardService;
        this.currentUserService = currentUserService;
    }
    
    public DashboardResponse getDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime tomorrowEnd = tomorrowStart.plusDays(1);
        LocalDateTime weekEnd = now.plusDays(7);

        Long userId = currentUserService.currentUserId();

        long dueNow = topicRepository.countDueNowByUser(userId, now);
        long dueTomorrow = topicRepository.countDueBetweenByUser(userId, tomorrowStart, tomorrowEnd);
        long dueThisWeek = topicRepository.countDueBetweenByUser(userId, tomorrowStart, weekEnd);
        long totalTopics = topicRepository.countByUser(userId);


        List<WeakAreaResponse> weakAreas = topicRepository.findWeakAreas(userId, now)
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

        List<KnowledgeAreaResponse> strongKnowledgeAreas = topicRepository.findStrongKnowledgeAreas(userId)
            .stream()
            .limit(2)
            .map(area -> new KnowledgeAreaResponse(
                area.getTitle(), 
                area.getAverageMemoryScore()
            ))
            .toList();

        List<KnowledgeAreaResponse> weakKnowledgeAreas = topicRepository.findWeakKnowledgeAreas(userId)
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
            rewardService.getCurrentUserReward()
        );
    }

    public List<MarkdownSummaryDto> getMarkdownSummaries() {
        Long userId = currentUserService.currentUserId();
        
        return topicRepository.findMarkdownSummaries(userId, LocalDateTime.now());
    }
}
