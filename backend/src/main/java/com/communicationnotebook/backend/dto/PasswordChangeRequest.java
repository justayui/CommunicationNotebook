package com.communicationnotebook.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequest(@NotBlank String currentPassword, @NotBlank String newPassword) {}
