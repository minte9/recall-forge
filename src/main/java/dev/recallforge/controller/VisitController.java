package dev.recallforge.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.recallforge.service.PageVisitService;
import jakarta.servlet.http.HttpServletRequest;

// Anonymous beacon sent by login.html on load. remoteAddr is the real client IP because
// server.forward-headers-strategy=framework resolves X-Forwarded-For from Traefik.
@RestController
@RequestMapping("/api/visits")
public class VisitController {

    private final PageVisitService pageVisitService;

    public VisitController(PageVisitService pageVisitService) {
        this.pageVisitService = pageVisitService;
    }

    @PostMapping
    public ResponseEntity<Void> recordVisit(HttpServletRequest request) {
        pageVisitService.recordVisit(request.getRemoteAddr(), request.getHeader("User-Agent"));
        return ResponseEntity.noContent().build();
    }
}
