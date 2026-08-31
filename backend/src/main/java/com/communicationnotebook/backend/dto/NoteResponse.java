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
        boolean favorited,
        long commentCount,
        boolean read,
        long readCount) {

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
