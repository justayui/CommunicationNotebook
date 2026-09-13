package com.communicationnotebook.backend.dto;


import com.communicationnotebook.backend.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "コメント情報を取得します")
public record CommentResponse(
        @Schema(description = "コメントのIDです。", example = "1")
        Integer id, 
        @Schema(description = "コメントに紐づく投稿のIDです。", example = "1")
        Integer noteId, 
        @Schema(description = "コメントを記載したユーザーのIDです。", example = "1")
        Integer userId, 
        @Schema(description = "コメントを記載したユーザーの名前です。", example = "田中花子")
        String author, 
        @Schema(description = "コメントの内容です。", example = "この投稿は非常に参考になりました。")
        String content, 
        @Schema(description = "コメント投稿時間です。LocalDateTimeが自動で設定されます。", example = "2026-04-01T16:00:00")
        LocalDateTime createdAt) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getNote().getId(),
                comment.getUser().getId(),
                comment.getUser().getName(),
                comment.getContent(),
                comment.getCreatedAt());
    }
}
