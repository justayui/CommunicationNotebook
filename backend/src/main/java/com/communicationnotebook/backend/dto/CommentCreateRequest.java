package com.communicationnotebook.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "投稿へのコメントを作成するためのリクエストボディです。")
public record CommentCreateRequest(
    @Schema(description = "コメントの内容です。空文字・未入力では登録できません。", example = "この投稿は非常に参考になりました。", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank String content) {}
