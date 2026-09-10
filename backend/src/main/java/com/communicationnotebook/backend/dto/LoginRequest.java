package com.communicationnotebook.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

//入力されたログインデータを受け取り、Controllerに渡すためのDTOです。
@Schema(description = "ログインに使用するリクエストボディです。")
public record LoginRequest(
    @Schema(description = "ログインに使用するユーザーIDです。空文字・未入力不可です。", example = "E001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank 
    String employeeId, 

    @Schema(description = "ログインパスワードです。空文字・未入力不可です。", example = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank 
    String password) {}
