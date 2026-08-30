package com.communicationnotebook.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String employeeId, @NotBlank String password) {}
