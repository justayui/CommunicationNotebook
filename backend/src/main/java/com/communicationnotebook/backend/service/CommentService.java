package com.communicationnotebook.backend.service;

import com.communicationnotebook.backend.dto.CommentCreateRequest;
import com.communicationnotebook.backend.dto.CommentResponse;
import com.communicationnotebook.backend.entity.Comment;
import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.CommentRepository;
import com.communicationnotebook.backend.repository.NoteRepository;
import com.communicationnotebook.backend.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public CommentService(
            CommentRepository commentRepository, NoteRepository noteRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    public List<CommentResponse> findAll(Integer noteId) {
        findActiveNote(noteId);
        return commentRepository.findByNote_IdAndDeletedFalseOrderByCreatedAtAsc(noteId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    public CommentResponse create(Integer noteId, CommentCreateRequest request, Integer userId) {
        Note note = findActiveNote(noteId);
        User user = findActiveUser(userId);

        Comment comment = new Comment();
        comment.setNote(note);
        comment.setUser(user);
        comment.setContent(request.content());
        comment.setDeleted(false);

        Comment saved = commentRepository.save(comment);
        Comment reloaded = commentRepository.findByIdWithUser(saved.getId()).orElseThrow();
        return CommentResponse.from(reloaded);
    }

    public void delete(Integer noteId, Integer commentId, Integer userId) {
        findActiveNote(noteId);
        User requester = findActiveUser(userId);

        Comment comment = commentRepository
                .findByIdAndNote_Id(commentId, noteId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found: " + commentId));

        boolean isAuthor = comment.getUser().getId().equals(requester.getId());
        if (!isAuthor && !requester.isAdmin()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only the author or an admin can delete this comment");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    private Note findActiveNote(Integer noteId) {
        return noteRepository
                .findById(noteId)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found: " + noteId));
    }

    private User findActiveUser(Integer userId) {
        return userRepository
                .findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
    }
}
