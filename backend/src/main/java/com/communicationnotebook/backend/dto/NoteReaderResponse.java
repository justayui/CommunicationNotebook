package com.communicationnotebook.backend.dto;

import com.communicationnotebook.backend.entity.NoteRead;

public record NoteReaderResponse(Integer userId, String name) {

    public static NoteReaderResponse from(NoteRead noteRead) {
        return new NoteReaderResponse(noteRead.getUser().getId(), noteRead.getUser().getName());
    }
}
