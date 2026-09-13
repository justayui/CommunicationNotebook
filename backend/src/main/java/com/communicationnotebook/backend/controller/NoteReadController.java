package com.communicationnotebook.backend.controller;

import com.communicationnotebook.backend.dto.ErrorResponse;
import com.communicationnotebook.backend.dto.NoteReaderResponse;
import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.NoteReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "既読者管理", description = "既読者の取得・登録をするAPI群です。")
@RestController
@RequestMapping("/api/notes/{noteId}/reads")
public class NoteReadController {

    private final NoteReadService noteReadService;

    public NoteReadController(NoteReadService noteReadService) {
        this.noteReadService = noteReadService;
    }

    @Operation(summary = "既読者一覧取得", description = "投稿ごとの既読者一覧を取得します。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "取得成功。既読者一覧を返却します。"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "取得失敗。未認証の時に返却されます。messageは\"認証が必要です\"が返ります。",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                        {"timestamp":"2026-09-10T06:00:00Z","status":401,"error":"Unauthorized","message":"認証が必要です","path":"/api/notes/1/reads"}""")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "取得失敗。投稿が存在しないときに返却されます。messageは\"投稿が見つかりません\"が返ります。",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                        {"timestamp":"2026-09-10T06:00:00Z","status":404,"error":"Not Found","message":"投稿が見つかりません","path":"/api/notes/1/reads"}""")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "取得失敗。サーバー内部エラー。データベースへの接続失敗など、予期せぬシステム異常が発生した場合に返却されます。messageは\"サーバーエラーが発生しました\"が返ります。",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                        {"timestamp":"2026-09-10T06:00:00Z","status":500,"error":"Internal Server Error","message":"サーバーエラーが発生しました","path":"/api/notes/1/reads"}""")
            )
        )
    })
    @GetMapping
    public List<NoteReaderResponse> findReaders(@PathVariable Integer noteId) {
        return noteReadService.findReaders(noteId);
    }

    @Operation(summary = "既読者登録", description = "既読者の情報を登録します。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "登録成功。"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "登録失敗。未認証の時に返却されます。messageは\"認証が必要です\"が返ります。",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                        {"timestamp":"2026-09-10T06:00:00Z","status":401,"error":"Unauthorized","message":"認証が必要です","path":"/api/notes/1/reads"}""")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "登録失敗。投稿またはユーザーが存在しないときに返却されます。messageは\"投稿が見つかりません\"または\"ユーザーが見つかりません\"が返ります。",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "投稿が見つからない場合", value = """
                            {"timestamp":"2026-09-10T06:00:00Z","status":404,"error":"Not Found","message":"投稿が見つかりません","path":"/api/notes/1/reads"}"""),
                    @ExampleObject(name = "ユーザーが見つからない場合", value = """
                            {"timestamp":"2026-09-10T06:00:00Z","status":404,"error":"Not Found","message":"ユーザーが見つかりません","path":"/api/notes/1/reads"}""")
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "登録失敗。サーバー内部エラー。データベースへの接続失敗など、予期せぬシステム異常が発生した場合に返却されます。messageは\"サーバーエラーが発生しました\"が返ります。",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                        {"timestamp":"2026-09-10T06:00:00Z","status":500,"error":"Internal Server Error","message":"サーバーエラーが発生しました","path":"/api/notes/1/reads"}""")
            )
        )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@PathVariable Integer noteId, @AuthenticationPrincipal UserPrincipal principal) {
        noteReadService.register(noteId, principal.getId());
    }
}
