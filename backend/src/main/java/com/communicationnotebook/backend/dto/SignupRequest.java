package com.communicationnotebook.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "新規アカウント登録に使用するリクエストボディです。")
public record SignupRequest(
    @Schema(description = "従業員番号です。ログインIDになります。空文字・未入力不可です。", example = "E001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank String employeeId, 
    
    @Schema(description = "アカウント名です。空文字・未入力不可です。", example = "田中花子", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank String name, 
    
    @Schema(description = "パスワードです。空文字・未入力不可です。", example = "test123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank String password) {}
