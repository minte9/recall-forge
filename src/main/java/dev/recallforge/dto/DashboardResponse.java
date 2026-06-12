package dev.recallforge.dto;

import java.util.List;

public record DashboardResponse(
    long dueNow,
    long dueTomorrow,
    long dueThisWeek,
    long totalTopics,
    List<WeakAreaResponse> weakAreas,
    List<KnowledgeAreaResponse> strongAreas,
    List<KnowledgeAreaResponse> weakKnowledgeAreas,
    RewardResponse reward
) {}