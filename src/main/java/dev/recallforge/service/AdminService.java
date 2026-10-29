package dev.recallforge.service;

import org.springframework.stereotype.Service;

import dev.recallforge.repository.PageVisitRepository;
import dev.recallforge.repository.UserAccountRepository;
import dev.recallforge.security.CurrentUserService;
import dev.recallforge.dto.AdminUsersStatsResponse;
import dev.recallforge.dto.DailyVisitResponse;
import dev.recallforge.dto.ReferrerVisitResponse;

import java.time.LocalDate;
import java.util.List;

@Service
public class AdminService {

    private static final int VISIT_STATS_DAYS = 30;
    
    private final UserAccountRepository userAccountRepository;
    private final PageVisitRepository pageVisitRepository;
    private final CurrentUserService currentUserService;

    public AdminService(
            UserAccountRepository userAccountRepository,
            PageVisitRepository pageVisitRepository,
            CurrentUserService currentUserService) {

        this.userAccountRepository = userAccountRepository;
        this.pageVisitRepository = pageVisitRepository;
        this.currentUserService = currentUserService;
    }

    public List<AdminUsersStatsResponse> getUsersStats() {
        currentUserService.requireAdmin();

        return userAccountRepository.findAdminUserStats();
    }

    public List<DailyVisitResponse> getDailyVisits() {
        currentUserService.requireAdmin();

        LocalDate since = LocalDate.now().minusDays(VISIT_STATS_DAYS - 1);

        return pageVisitRepository.countDailyVisitorsSince(since).stream()
            .map(row -> new DailyVisitResponse(row.getVisitDate(), row.getVisitors()))
            .toList();
    }

    public List<ReferrerVisitResponse> getReferrerVisits() {
        currentUserService.requireAdmin();

        LocalDate since = LocalDate.now().minusDays(VISIT_STATS_DAYS - 1);

        return pageVisitRepository.countVisitorsByReferrerSince(since).stream()
            .map(row -> new ReferrerVisitResponse(row.getReferrerHost(), row.getVisitors()))
            .toList();
    }
}
