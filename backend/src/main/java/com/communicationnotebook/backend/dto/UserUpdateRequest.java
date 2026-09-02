package com.communicationnotebook.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(@NotBlank String name) {}
