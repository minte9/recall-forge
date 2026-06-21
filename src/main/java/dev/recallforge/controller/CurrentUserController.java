package dev.recallforge.controller;

import dev.recallforge.domain.UserAccount;
import dev.recallforge.dto.CurrentUserResponse;
import dev.recallforge.security.CurrentUserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CurrentUserController {

    private final CurrentUserService currentUserService;

    public CurrentUserController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    public CurrentUserResponse me() {
        UserAccount user = currentUserService.currentUser();

        return new CurrentUserResponse(
            user.getId(),
            user.getEmail(),
            user.getDisplayName(),
            user.getRole().name(),
            user.getAvatarUrl()
        );
    }
}