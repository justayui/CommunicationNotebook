package com.communicationnotebook.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteUpdateRequest(@NotBlank @Size(max = 50) String category, @NotBlank String content) {}
