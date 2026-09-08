package com.communicationnotebook.backend.controller;

import com.communicationnotebook.backend.dto.CategoryResponse;
import com.communicationnotebook.backend.dto.ErrorResponse;
import com.communicationnotebook.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "カテゴリ名", description = "カテゴリの取得を行うAPIです。")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "カテゴリ名の一覧取得", description = "カテゴリ名の一覧を取得します。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "カテゴリ名の一覧を取得しました。"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "カテゴリ名の一覧取得に失敗。未認証です。messageは\"No message available\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "カテゴリ名の一覧取得に失敗。内部エラーが発生しました。messageは\"サーバーエラーが発生しました\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping
    public List<CategoryResponse> findAll() {
        return categoryService.findAll();
    }
}
