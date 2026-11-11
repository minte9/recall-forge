package dev.recallforge.dto;

import dev.recallforge.domain.AiModel;
import jakarta.validation.constraints.NotNull;

public record UpdateAiModelRequest(
    @NotNull  AiModel aiModel
) {}
