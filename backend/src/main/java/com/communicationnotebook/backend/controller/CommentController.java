package com.communicationnotebook.backend.controller;

import com.communicationnotebook.backend.dto.CommentCreateRequest;
import com.communicationnotebook.backend.dto.CommentResponse;
import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notes/{noteId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<CommentResponse> findAll(@PathVariable Integer noteId) {
        return commentService.findAll(noteId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(
            @PathVariable Integer noteId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return commentService.create(noteId, request, principal.getId());
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Integer noteId,
            @PathVariable Integer commentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        commentService.delete(noteId, commentId, principal.getId());
    }
}
