package com.communicationnotebook.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "新規アカウント登録に使用するリクエストボディです。")
public record SignupRequest(
    @Schema(description = "職員IDです。ログインIDになります。空文字・未入力不可です。", example = "E001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank String employeeId, 
    
    @Schema(description = "ユーザー名です。空文字・未入力不可です。", example = "田中花子", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank String name, 
    
    @Schema(description = "パスワードです。空文字・未入力不可です。", example = "test123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank String password) {}
