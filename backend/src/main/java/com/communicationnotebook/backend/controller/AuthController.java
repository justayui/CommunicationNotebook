package com.communicationnotebook.backend.controller;

import com.communicationnotebook.backend.dto.ErrorResponse;
import com.communicationnotebook.backend.dto.LoginRequest;
import com.communicationnotebook.backend.dto.PasswordChangeRequest;
import com.communicationnotebook.backend.dto.SignupRequest;
import com.communicationnotebook.backend.dto.UserResponse;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name ="認証関連管理", description = "ユーザーのログイン・サインアップ・パスワード更新を行うAPI群です。")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserService userService;

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.userService = userService;
    }

    @Operation(summary = "ログイン", description = "ログイン処理を行います。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "ログイン成功。"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "ログイン失敗。IDやパスワード不正時に返却されます。messageは\"職員IDまたはパスワードが正しくありません\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "ログイン失敗。ユーザーが存在しないときに返却されます。messageは\"User not found: {userId}\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "ログイン失敗。サーバー内部エラー。データベースへの接続失敗など、予期せぬシステム異常が発生した場合に返却されます。messageは\"サーバーエラーが発生しました\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping("/login")
    public UserResponse login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authResult;
        try {
            authResult = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.employeeId(), request.password()));
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "職員IDまたはパスワードが正しくありません");
        }

        establishSession(authResult, httpRequest, httpResponse);

        return UserResponse.from(((UserPrincipal) authResult.getPrincipal()).getUser());
    }

    @Operation(summary = "サインアップ", description = "新規アカウント登録処理後、自動ログインを行います。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "サインアップ成功。"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "サインアップ失敗。IDが重複している際に返却されます。messageは\"職員IDは既に使用されています\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "サインアップ失敗。サーバー内部エラー。データベースへの接続失敗など、予期せぬシステム異常が発生した場合に返却されます。messageは\"サーバーエラーが発生しました\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(
            @Valid @RequestBody SignupRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        User user = userService.signup(request);
        UserPrincipal principal = new UserPrincipal(user);
        Authentication authResult =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        establishSession(authResult, httpRequest, httpResponse);

        return UserResponse.from(user);
    }

    @Operation(summary = "ユーザー情報取得", description = "ログイン中のユーザー情報を取得します。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "取得成功。"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "取得失敗。未認証の時に返却されます。messageは\"No message available\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "取得失敗。ユーザーが存在しないときに返却されます。messageは\"User not found: {userId}\"が返ります。",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "取得失敗。サーバー内部エラー。データベースへの接続失敗など、予期せぬシステム異常が発生した場合に返却されます。messageは\"サーバーエラーが発生しました\"が返ります。", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return UserResponse.from(principal.getUser());
    }

    @Operation(summary = "パスワード変更", description = "ログイン中のユーザーのパスワードを更新します。")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "更新成功。"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "更新失敗。現パスワードが誤っている時に返却されます。messageは\"現在のパスワードが正しくありません\"が返ります。", 
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
    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(principal.getId(), request);
    }

    private void establishSession(
            Authentication authResult, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
    }
}
