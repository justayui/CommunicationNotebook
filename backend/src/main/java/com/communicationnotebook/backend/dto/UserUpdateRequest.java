package com.communicationnotebook.backend.dto;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ユーザー情報の更新に使用するリクエストボディです。")
public record UserUpdateRequest(
    @Schema(description = "ユーザー名です。空文字・未入力不可です。", example = "山田花子", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank 
    String name
    ) {}
