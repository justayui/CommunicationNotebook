package com.communicationnotebook.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteCreateRequest(@NotBlank @Size(max = 50) String category, @NotBlank String content) {}
