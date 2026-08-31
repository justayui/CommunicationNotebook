package com.communicationnotebook.backend.dto;

import com.communicationnotebook.backend.entity.Note;
import java.time.LocalDateTime;

public record NoteResponse(
        Integer id,
        Integer userId,
        String category,
        String content,
        String author,
        LocalDateTime createdAt,
        boolean favorited) {

    public static NoteResponse from(Note note) {
        return from(note, false);
    }

    public static NoteResponse from(Note note, boolean favorited) {
        return new NoteResponse(
                note.getId(),
                note.getUser().getId(),
                note.getCategory(),
                note.getContent(),
                note.getUser().getName(),
                note.getCreatedAt(),
                favorited);
    }
}
