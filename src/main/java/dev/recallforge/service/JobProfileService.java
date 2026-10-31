package dev.recallforge.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import dev.recallforge.domain.JobProfile;
import dev.recallforge.domain.UserAccount;
import dev.recallforge.dto.JobProfileCategoryDto;
import dev.recallforge.dto.JobProfileDto;
import dev.recallforge.repository.JobProfileCategoryRepository;
import dev.recallforge.repository.JobProfileRepository;
import dev.recallforge.repository.UserAccountRepository;
import dev.recallforge.security.CurrentUserService;


@Service
public class JobProfileService {

    private static final Long TEMPLATE_USER_ID = 1L;

    private final JobProfileRepository profileRepository;
    private final JobProfileCategoryRepository categoryRepository;
    private final UserAccountRepository userRepository;
    private final CurrentUserService currentUserService;

    public JobProfileService(
            JobProfileRepository profileRepository,
            JobProfileCategoryRepository categoryRepository,
            UserAccountRepository userRepository,
            CurrentUserService currentUserService) {

        this.profileRepository = profileRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<JobProfileDto> findAll() {

        return profileRepository
            .findByUserIdAndEnabledTrueOrderByIdAsc(
                TEMPLATE_USER_ID
            )
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public JobProfileDto getActive() {

        Long userId = currentUserService.currentUserId();

        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
                )
            );

        Long profileId = user.getActiveJobProfileId();

        if (profileId == null) {
            return null;
        }

        return profileRepository
            .findByIdAndUserIdAndEnabledTrue(
                profileId,
                TEMPLATE_USER_ID
            )
            .map(this::toDto)
            .orElse(null);
    }

    @Transactional
    public void setActive(Long profileId) {

        Long userId = currentUserService.currentUserId();

        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
                )
            );

        if (profileId != null) {

            profileRepository
                .findByIdAndUserIdAndEnabledTrue(
                    profileId,
                    TEMPLATE_USER_ID
                )
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid job profile"
                    )
                );
        }

        user.setActiveJobProfileId(profileId);
        userRepository.save(user);
    }

    private JobProfileDto toDto(JobProfile profile) {

        List<JobProfileCategoryDto> categories =
            categoryRepository.findCategories(profile.getId())
                .stream()
                .map(c -> new JobProfileCategoryDto(
                    c.getId(),
                    c.getCategory(),
                    c.getEnvironment()
                ))
                .toList();

        return new JobProfileDto(
            profile.getId(),
            profile.getName(),
            profile.getDescription(),
            categories
        );
    }
}
