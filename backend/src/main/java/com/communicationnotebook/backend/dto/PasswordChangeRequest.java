package com.communicationnotebook.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "パスワード変更に用いる新旧パスワードを含むリクエストボディです。")
public record PasswordChangeRequest(
    @Schema(description = "変更前のパスワードです。空文字・未入力不可です。", example = "test123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank String currentPassword, 
    
    @Schema(description = "変更後のパスワードです。空文字・未入力不可です。", example = "test321", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank String newPassword) {}
