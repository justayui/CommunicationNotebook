package com.communicationnotebook.backend.controller;

import com.communicationnotebook.backend.dto.ErrorResponse;
import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "お気に入り管理", description = "お気に入りの登録・解除をするAPI群です。")
@RestController
@RequestMapping("/api/notes/{noteId}/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Operation(summary = "お気に入り登録", description = "投稿をお気に入り登録します。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "登録成功。"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "登録失敗。未認証の時に返却されます。messageは\"No message available\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "登録失敗。投稿またはユーザーが存在しないときに返却されます。messageは\"Note not found: {noteId}\"または\"User not found: {userId}\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "登録失敗。既にお気に入り登録済みの時に返却されます。messageは\"Note is already favorited: {noteId}\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "登録失敗。サーバー内部エラー。データベースへの接続失敗など、予期せぬシステム異常が発生した場合に返却されます。messageは\"サーバーエラーが発生しました\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@PathVariable Integer noteId, @AuthenticationPrincipal UserPrincipal principal) {
        favoriteService.register(noteId, principal.getId());
    }

    @Operation(summary = "お気に入り登録削除", description = "投稿のお気に入りを解除します。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "削除成功。"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "削除失敗。未認証の時に返却されます。messageは\"No message available\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "削除失敗。投稿者以外が削除しようとしたときに返却されます。messageは\"Only the author can delete this favorite\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "削除失敗。投稿・ユーザー・お気に入りが存在しないときに返却されます。messageは\"Note not found: {noteId}\"または\"User not found: {userId}\"または\"Favorite not found: {noteId}\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "削除失敗。サーバー内部エラー。データベースへの接続失敗など、予期せぬシステム異常が発生した場合に返却されます。messageは\"サーバーエラーが発生しました\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unregister(@PathVariable Integer noteId, @AuthenticationPrincipal UserPrincipal principal) {
        favoriteService.unregister(noteId, principal.getId());
    }
}
