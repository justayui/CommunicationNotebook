package com.communicationnotebook.backend.controller;

import com.communicationnotebook.backend.dto.CommentCreateRequest;
import com.communicationnotebook.backend.dto.CommentResponse;
import com.communicationnotebook.backend.dto.ErrorResponse;
import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name ="コメント機能管理", description ="コメントを取得・投稿・削除を行うAPI群です。")
@RestController
@RequestMapping("/api/notes/{noteId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary ="コメント一覧取得", description ="投稿ごとのコメント一覧を取得します。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "取得成功。コメント一覧を返却します。"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "取得失敗。未認証の時に返却されます。messageは\"認証が必要です\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "取得失敗。投稿が存在しないときに返却されます。messageは\"Note not found: {noteId}\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "取得失敗。サーバー内部エラー。データベースへの接続失敗など、予期せぬシステム異常が発生した場合に返却されます。messageは\"サーバーエラーが発生しました\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping
    public List<CommentResponse> findAll(@PathVariable Integer noteId) {
        return commentService.findAll(noteId);
    }

    @Operation(summary ="コメント登録", description ="投稿ごとのコメントを登録します。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "登録成功。"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "登録失敗。未認証の時に返却されます。messageは\"認証が必要です\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "登録失敗。投稿が存在しないときに返却されます。messageは\"Note not found: {noteId}\"が返ります。",
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
    public CommentResponse create(
            @PathVariable Integer noteId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return commentService.create(noteId, request, principal.getId());
    }

    @Operation(summary ="コメント削除", description ="コメントを削除します。投稿者・管理者以外は削除権限がありません。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "削除成功。"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "削除失敗。未認証の時に返却されます。messageは\"認証が必要です\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "削除失敗。投稿者・管理者以外が削除しようとしたときに返却されます。messageは\"Only the author or an admin can delete this comment\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "削除失敗。投稿・コメント・ユーザーが存在しないときに返却されます。messageは\"Note not found: {noteId}\"または\"Comment not found: {commentId}\"または\"User not found: {userId}\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "削除失敗。サーバー内部エラー。データベースへの接続失敗など、予期せぬシステム異常が発生した場合に返却されます。messageは\"サーバーエラーが発生しました\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Integer noteId,
            @PathVariable Integer commentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        commentService.delete(noteId, commentId, principal.getId());
    }
}
