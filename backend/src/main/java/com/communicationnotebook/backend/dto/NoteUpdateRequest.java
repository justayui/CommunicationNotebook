package com.communicationnotebook.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "投稿を更新する際に使用するリクエストボディです。")
public record NoteUpdateRequest(
    @Schema(description = "投稿のカテゴリです。GET /api/categoriesで取得できる値のいずれかを指定します。空文字・未入力不可です。", example = "委員会")
    @NotBlank 
    @Size(max = 50) 
    String category, 
    
    @Schema(description = "投稿の内容です。空文字・未入力不可です。", example = "投稿の更新です。")
    @NotBlank 
    String content
    ) {}
