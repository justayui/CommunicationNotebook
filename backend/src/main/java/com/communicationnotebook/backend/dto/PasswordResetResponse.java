package com.communicationnotebook.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "パスワードリセット結果を表すDTOです。")
public record PasswordResetResponse(
    @Schema(description = "パスワードリセット対象のユーザー名です。", example = "田中花子")
    String name, 
    @Schema(description = "仮パスワードです。", example = "123456")
    String temporaryPassword) {}
