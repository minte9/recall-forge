package dev.recallforge.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.recallforge.dto.VisitRequest;
import dev.recallforge.service.PageVisitService;
import jakarta.servlet.http.HttpServletRequest;

// Anonymous beacon sent by login.html on load. remoteAddr is the real client IP because
// server.forward-headers-strategy=framework resolves X-Forwarded-For from Traefik.
// The referrer comes from the body (document.referrer): the beacon's own Referer header
// would always be the login page.
@RestController
@RequestMapping("/api/visits")
public class VisitController {

    private final PageVisitService pageVisitService;

    public VisitController(PageVisitService pageVisitService) {
        this.pageVisitService = pageVisitService;
    }

    @PostMapping
    public ResponseEntity<Void> recordVisit(
            HttpServletRequest request,
            @RequestBody(required = false) VisitRequest body) {

        pageVisitService.recordVisit(
            request.getRemoteAddr(),
            request.getHeader("User-Agent"),
            body != null ? body.referrer() : null,
            request.getServerName()
        );
        return ResponseEntity.noContent().build();
    }
}
