package com.communicationnotebook.backend.controller;

import com.communicationnotebook.backend.dto.ErrorResponse;
import com.communicationnotebook.backend.dto.NoteCreateRequest;
import com.communicationnotebook.backend.dto.NoteResponse;
import com.communicationnotebook.backend.dto.NoteUpdateRequest;
import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.NoteService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name ="投稿管理", description = "投稿の表示・登録・更新・削除をするAPI群です。")
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @Operation(summary ="投稿一覧取得", description ="全件、または指定された条件（キーワード・カテゴリー・お気に入り）に一致する投稿一覧を取得します。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "取得成功。全件もしくは指定された条件に一致する投稿の一覧を返却します。"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "取得失敗。リクエスト不正時(パラメータの型不一致)に返却されます。messageは\"パラメータの型が不正です\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "取得失敗。未認証の時に返却されます。messageは\"認証が必要です\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "取得失敗。サーバー内部エラー。データベースへの接続失敗など、予期せぬシステム異常が発生した場合に返却されます。messageは\"サーバーエラーが発生しました\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping
    public List<NoteResponse> findAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "false") boolean favoriteOnly,
            @AuthenticationPrincipal UserPrincipal principal) {
        return noteService.findAll(keyword, category, favoriteOnly, principal.getId());
    }

    @Operation(summary = "新規投稿登録", description = "新規投稿を登録します。")
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
            responseCode = "500", 
            description = "登録失敗。サーバー内部エラー。データベースへの接続失敗など、予期せぬシステム異常が発生した場合に返却されます。messageは\"サーバーエラーが発生しました\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse create(
            @Valid @RequestBody NoteCreateRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        return noteService.create(request, principal.getId());
    }

    @Operation(summary ="投稿更新", description ="投稿のカテゴリ・内容を更新します。投稿者以外は更新権限がありません。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "更新成功。"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "更新失敗。未認証の時に返却されます。messageは\"認証が必要です\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "更新失敗。投稿者以外が更新しようとしたときに返却されます。messageは\"Only the author can update this note\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "更新失敗。投稿が存在しないときに返却されます。messageは\"Note not found: {noteId}\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "更新失敗。サーバー内部エラー。データベースへの接続失敗など、予期せぬシステム異常が発生した場合に返却されます。messageは\"サーバーエラーが発生しました\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PutMapping("/{id}")
    public NoteResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody NoteUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return noteService.update(id, request, principal.getId());
    }

    @Operation(summary ="投稿削除", description ="投稿を削除します。投稿者・管理者以外は削除権限がありません。")
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
            description = "削除失敗。投稿者・管理者以外が削除しようとしたときに返却されます。messageは\"Only the author or an admin can delete this note\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "削除失敗。投稿が存在しないときに返却されます。messageは\"Note not found: {noteId}\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "削除失敗。サーバー内部エラー。データベースへの接続失敗など、予期せぬシステム異常が発生した場合に返却されます。messageは\"サーバーエラーが発生しました\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id, @AuthenticationPrincipal UserPrincipal principal) {
        noteService.delete(id, principal.getId());
    }
}
