package dev.recallforge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import dev.recallforge.config.AppLoginProperties;
import dev.recallforge.exception.LoginDisabledException;

@Controller
public class LoginController {

    private final AppLoginProperties loginProperties;

    public LoginController(AppLoginProperties loginProperties) {
        this.loginProperties = loginProperties;
    }

    @GetMapping("/login")
    public String login() {
        if (!loginProperties.enabled()) {
            throw new LoginDisabledException();
        }

        return "forward:/login.html";
    }

    @GetMapping("/terms")
    public String terms() {
        return "forward:/terms.html";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "forward:/privacy.html";
    }
}
