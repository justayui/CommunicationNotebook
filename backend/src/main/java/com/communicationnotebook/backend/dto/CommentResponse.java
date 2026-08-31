package com.communicationnotebook.backend.dto;

import com.communicationnotebook.backend.entity.Comment;
import java.time.LocalDateTime;

public record CommentResponse(
        Integer id, Integer noteId, Integer userId, String author, String content, LocalDateTime createdAt) {

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
