package com.communicationnotebook.backend.dto;

import com.communicationnotebook.backend.entity.Note;
import java.time.LocalDateTime;

public record NoteResponse(Integer id, String category, String content, String author, LocalDateTime createdAt) {

    public static NoteResponse from(Note note) {
        return new NoteResponse(
                note.getId(), note.getCategory(), note.getContent(), note.getUser().getName(), note.getCreatedAt());
    }
}
