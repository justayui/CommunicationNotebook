package com.communicationnotebook.backend.dto;

import com.communicationnotebook.backend.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "投稿のカテゴリ選択肢、もしくはカテゴリ別検索条件。")
public record CategoryResponse(
    @Schema(description = "投稿作成時もしくはカテゴリ別検索時に表示するカテゴリ名です。プルダウンから選択します。", example = "手順変更")
    String name
    ) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getName());
    }
}
