package com.communicationnotebook.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Schema(description = "エラーが発生したときに返却するエラーレスポンスの内容を表すDTO。")
public record ErrorResponse(
    @Schema(description = "エラー発生日時です。timestampが自動で設定されます。", example = "2026-09-10T06:00:00Z")
    Instant timestamp, 
    @Schema(description = "HTTPステータスコードです。", example = "404")
    int status, 
    @Schema(description = "エラーの種類です。", example = "Not Found")
    String error, 
    @Schema(description = "クライアントに通知するメッセージです。", example = "お探しのページは見つかりませんでした。")
    String message, 
    @Schema(description = "エラーが発生したAPIのエンドポイントです。", example = "/api/users/99")
    String path
    ) {

    public static ErrorResponse of(HttpStatusCode status, String message, String path) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        String error;
        if (resolved != null) {
            error = resolved.getReasonPhrase();
        } else {
            error = status.toString();
        }
        return new ErrorResponse(Instant.now(), status.value(), error, message, path);
    }
}
