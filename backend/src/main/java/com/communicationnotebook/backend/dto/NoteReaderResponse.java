package com.communicationnotebook.backend.dto;

import com.communicationnotebook.backend.entity.NoteRead;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "既読者の情報を表すDTOです。")
public record NoteReaderResponse(
    @Schema(description = "ユーザーのIDです。", example = "E001")
    Integer userId, 
    @Schema(description = "既読者の名前です。", example = "田中花子")
    String name) {

    public static NoteReaderResponse from(NoteRead noteRead) {
        return new NoteReaderResponse(noteRead.getUser().getId(), noteRead.getUser().getName());
    }
}
