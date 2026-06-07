package dev.recallforge.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.recallforge.dto.DashboardResponse;
import dev.recallforge.dto.MarkdownSummaryDto;
import dev.recallforge.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse getDashboard() {
        return dashboardService.getDashboard();
    }

    @GetMapping("/markdowns")
    public List<MarkdownSummaryDto> markdownSummary() {
        return dashboardService.getMarkdownSummaries();
    }
    
}
