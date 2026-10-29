package dev.recallforge.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.recallforge.dto.AdminUsersStatsResponse;
import dev.recallforge.dto.DailyVisitResponse;
import dev.recallforge.dto.ReferrerVisitResponse;
import dev.recallforge.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public List<AdminUsersStatsResponse> getUsers() {
        return adminService.getUsersStats();
    }

    @GetMapping("/visits")
    public List<DailyVisitResponse> getDailyVisits() {
        return adminService.getDailyVisits();
    }

    @GetMapping("/referrers")
    public List<ReferrerVisitResponse> getReferrerVisits() {
        return adminService.getReferrerVisits();
    }
}
