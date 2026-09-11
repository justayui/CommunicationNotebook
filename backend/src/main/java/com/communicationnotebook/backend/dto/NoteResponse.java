package com.communicationnotebook.backend.dto;

import com.communicationnotebook.backend.entity.Note;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "投稿内容に、お気に入り・コメント数・既読・既読数を追加した状態で一つのデータとしてクライアントに返すためのDTOです。")
public record NoteResponse(
        @Schema(description = "投稿のIDです。", example = "1")
        Integer id,
        @Schema(description = "投稿者のIDです。", example = "1")
        Integer userId,
        @Schema(description = "投稿のカテゴリです。", example = "手順変更")
        String category,
        @Schema(description = "投稿の内容です。", example = "テスト投稿です。")
        String content,
        @Schema(description = "投稿者名です。", example = "田中")
        String author,
        @Schema(description = "投稿作成時間です。LocalDateTimeが自動で設定されます。", example = "2026-04-01T16:00:00")
        LocalDateTime createdAt,
        @Schema(description = "お気に入り登録の状態です。", example = "false")
        boolean favorited,
        @Schema(description = "コメント数です。", example = "1")
        long commentCount,
        @Schema(description = "既読・未読の状態です。", example = "true")
        boolean read,
        @Schema(description = "既読数です。", example = "2")
        long readCount
        ) {

    //投稿新規作成直後のレスポンス用です。
    public static NoteResponse from(Note note) {
        return from(note, false, 0L, false, 0L);
    }

    public static NoteResponse from(
            Note note, boolean favorited, long commentCount, boolean read, long readCount) {
        return new NoteResponse(
                note.getId(),
                note.getUser().getId(),
                note.getCategory(),
                note.getContent(),
                note.getUser().getName(),
                note.getCreatedAt(),
                favorited,
                commentCount,
                read,
                readCount);
    }
}
