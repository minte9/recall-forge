package dev.recallforge.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import dev.recallforge.dto.AdminUsersStatsResponse;
import dev.recallforge.service.AdminService;

@WebMvcTest(
    controllers = AdminController.class,
    excludeAutoConfiguration = {
        OAuth2ClientAutoConfiguration.class,
        OAuth2ClientWebSecurityAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private AdminService adminService;

    @Test
    void getUsers_returnsStatsFromServiceAsJson() {
        List<AdminUsersStatsResponse> stats = List.of(
            new AdminUsersStatsResponse(1L, "user@example.com", "User One", false, 10L, 5L, 100L),
            new AdminUsersStatsResponse(2L, "admin@example.com", "Admin User", true, 20L, 15L, 200L)
        );
        when(adminService.getUsersStats()).thenReturn(stats);

        mockMvcTester.get().uri("/api/admin/users")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                [
                  {
                    "userId": 1,
                    "email": "user@example.com",
                    "displayName": "User One",
                    "admin": false,
                    "totalTopics": 10,
                    "totalReviews": 5,
                    "totalTokenSpent": 100
                  },
                  {
                    "userId": 2,
                    "email": "admin@example.com",
                    "displayName": "Admin User",
                    "admin": true,
                    "totalTopics": 20,
                    "totalReviews": 15,
                    "totalTokenSpent": 200
                  }
                ]
                """);
    }

    @Test
    void getUsers_serviceReturnsEmptyList_returnsEmptyJsonArray() {
        when(adminService.getUsersStats()).thenReturn(List.of());

        mockMvcTester.get().uri("/api/admin/users")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("[]");
    }

    @Test
    void getUsers_delegatesToAdminService() {
        when(adminService.getUsersStats()).thenReturn(List.of());

        mockMvcTester.get().uri("/api/admin/users").assertThat().hasStatusOk();

        verify(adminService).getUsersStats();
    }
}
