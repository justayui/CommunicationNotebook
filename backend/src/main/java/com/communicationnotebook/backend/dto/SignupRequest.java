package com.communicationnotebook.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record SignupRequest(@NotBlank String employeeId, @NotBlank String name, @NotBlank String password) {}
