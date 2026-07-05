package dev.recallforge.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.recallforge.dto.CategoryOptionResponse;
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
    public DashboardResponse getDashboard(
            @RequestParam(required = false, defaultValue = "Java") String category) {
        return dashboardService.getDashboard(category);
    }

    @GetMapping("/markdowns")
    public List<MarkdownSummaryDto> markdownSummary(
            @RequestParam(required = false, defaultValue = "Java") String category) {
        return dashboardService.getMarkdownSummaries(category);
    }

    @GetMapping("/categories")
    public List<CategoryOptionResponse> categories() {
        return dashboardService.getCategoryOptions();
    }

}
