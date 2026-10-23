package dev.recallforge.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.recallforge.dto.JobProfileDto;
import dev.recallforge.dto.SelectJobProfileRequest;
import dev.recallforge.service.JobProfileService;

@RestController
@RequestMapping("/api/job-profiles")
public class JobProfileController {

    private final JobProfileService profileService;

    public JobProfileController(
            JobProfileService profileService) {

        this.profileService = profileService;
    }

    @GetMapping
    public List<JobProfileDto> list() {
        return profileService.findAll();
    }

    @GetMapping("/active")
    public JobProfileDto active() {
        return profileService.getActive();
    }

    @PutMapping("/active")
    public ResponseEntity<Void> updateActive(
            @RequestBody SelectJobProfileRequest request) {

        profileService.setActive(request.profileId());

        return ResponseEntity.noContent().build();
    }
}