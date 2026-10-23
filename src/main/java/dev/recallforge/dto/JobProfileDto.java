package dev.recallforge.dto;

import java.util.List;

public record JobProfileDto(
    Long id,
    String name,
    String description,
    List<JobProfileCategoryDto> categories
) {}
