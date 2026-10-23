package dev.recallforge.service;

import org.springframework.stereotype.Service;

import dev.recallforge.repository.UserAccountRepository;
import dev.recallforge.security.CurrentUserService;
import dev.recallforge.dto.AdminUsersStatsResponse;

import java.util.List;

@Service
public class AdminService {
    
    private final UserAccountRepository userAccountRepository;
    private final CurrentUserService currentUserService;

    public AdminService(
            UserAccountRepository userAccountRepository,
            CurrentUserService currentUserService) {

        this.userAccountRepository = userAccountRepository;
        this.currentUserService = currentUserService;
    }

    public List<AdminUsersStatsResponse> getUsersStats() {
        currentUserService.requireAdmin();

        return userAccountRepository.findAdminUserStats();
    }
}
