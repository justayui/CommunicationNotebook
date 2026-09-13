package com.communicationnotebook.backend.controller;

import com.communicationnotebook.backend.dto.ErrorResponse;
import com.communicationnotebook.backend.dto.PasswordResetResponse;
import com.communicationnotebook.backend.dto.UserResponse;
import com.communicationnotebook.backend.dto.UserUpdateRequest;
import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.UserService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ユーザー情報管理", description = "ユーザー情報の取得・更新・削除・パスワードリセットを行うAPI群です。管理者のみ操作権限があります。")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "ユーザー情報一覧取得", description = "ユーザー情報の一覧を取得します。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "取得成功。ユーザー情報一覧を返却します。"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "取得失敗。未認証の時に返却されます。messageは\"No message available\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "取得失敗。管理者以外が実行しようとしたときに返却されます。messageは\"管理者のみ実行できます。\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "取得失敗。サーバー内部エラー。データベースへの接続失敗など、予期せぬシステム異常が発生した場合に返却されます。messageは\"サーバーエラーが発生しました\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @Operation(summary = "ユーザー情報のID検索", description = "IDに紐づくユーザーの情報を取得します。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "取得成功。IDに紐づくユーザー情報を返却します。"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "取得失敗。未認証の時に返却されます。messageは\"No message available\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "取得失敗。管理者以外が実行しようとしたときに返却されます。messageは\"管理者のみ実行できます。\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "取得失敗。サーバー内部エラー。データベースへの接続失敗など、予期せぬシステム異常が発生した場合に返却されます。messageは\"サーバーエラーが発生しました\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Integer id) {
        return userService.findById(id);
    }

    @Operation(summary = "ユーザー情報更新", description = "IDに紐づくユーザーの情報を更新します。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "更新成功。"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "更新失敗。未認証の時に返却されます。messageは\"No message available\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "更新失敗。管理者以外が更新しようとしたときに返却されます。messageは\"管理者のみ実行できます。\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "更新失敗。ユーザーが存在しないときに返却されます。messageは\"User not found: {userId}\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "更新失敗。サーバー内部エラー。データベースへの接続失敗など、予期せぬシステム異常が発生した場合に返却されます。messageは\"サーバーエラーが発生しました\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PutMapping("/{id}")
    public UserResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return userService.updateName(principal.getId(), id, request);
    }

    @Operation(summary = "ユーザー情報削除", description = "IDに紐づくユーザー情報を削除します。")
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
            description = "削除失敗。管理者以外が削除しようとしたときに返却されます。messageは\"管理者のみ実行できます。\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "削除失敗。ユーザーが存在しないときに返却されます。messageは\"User not found: {userId}\"が返ります。",
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
        userService.delete(principal.getId(), id);
    }

    @Operation(summary = "パスワードリセット", description = "IDに紐づくユーザーのパスワードをリセットします。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "リセット成功。"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "リセット失敗。未認証の時に返却されます。messageは\"No message available\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "リセット失敗。管理者以外がパスワードをリセットしようとしたときに返却されます。messageは\"管理者のみ実行できます。\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "リセット失敗。ユーザーが存在しないときに返却されます。messageは\"User not found: {userId}\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "リセット失敗。サーバー内部エラー。データベースへの接続失敗など、予期せぬシステム異常が発生した場合に返却されます。messageは\"サーバーエラーが発生しました\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping("/{id}/password-reset")
    public PasswordResetResponse resetPassword(
            @PathVariable Integer id, @AuthenticationPrincipal UserPrincipal principal) {
        return userService.resetPassword(principal.getId(), id);
    }
}
