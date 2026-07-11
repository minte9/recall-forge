package dev.recallforge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class LoginController {
    
    @GetMapping("/login")
    public String login() {
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
