package dev.recallforge.service;

import dev.recallforge.dto.DashboardResponse;
import dev.recallforge.dto.WeakAreaResponse;
import dev.recallforge.repository.TopicRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final TopicRepository topicRepository;

    public DashboardService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
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
            .limit(5)
            .map(area -> new WeakAreaResponse(
                area.getEnvironment(),
                area.getCategory(),
                area.getSubcategory(),
                area.getDueCount(),
                area.getAverageMemoryScore()
            ))
            .toList();

        return new DashboardResponse(
            dueNow,
            dueTomorrow, 
            dueThisWeek,
            totalTopics,
            weakAreas
        );
    }
}
