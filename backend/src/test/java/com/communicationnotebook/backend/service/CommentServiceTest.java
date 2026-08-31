package com.communicationnotebook.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.communicationnotebook.backend.dto.CommentCreateRequest;
import com.communicationnotebook.backend.dto.CommentResponse;
import com.communicationnotebook.backend.entity.Comment;
import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.CommentRepository;
import com.communicationnotebook.backend.repository.NoteRepository;
import com.communicationnotebook.backend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private Note newNote(Integer id, boolean deleted) {
        Note note = new Note();
        note.setId(id);
        note.setDeleted(deleted);
        return note;
    }

    private User newUser(Integer id, boolean deleted, boolean admin) {
        User user = new User();
        user.setId(id);
        user.setDeleted(deleted);
        user.setAdmin(admin);
        return user;
    }

    private Comment newComment(Integer id, Integer noteId, User author, String content) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setNote(newNote(noteId, false));
        comment.setUser(author);
        comment.setContent(content);
        comment.setDeleted(false);
        comment.setCreatedAt(LocalDateTime.of(2026, 8, 31, 10, 0));
        return comment;
    }

    @Test
    void findAll_returnsActiveCommentsForNote() {
        User author = newUser(1, false, false);
        Comment comment = newComment(100, 10, author, "コメントです");

        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, false)));
        when(commentRepository.findByNote_IdAndDeletedFalseOrderByCreatedAtAsc(10)).thenReturn(List.of(comment));

        List<CommentResponse> result = commentService.findAll(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).content()).isEqualTo("コメントです");
    }

    @Test
    void findAll_throwsNotFound_whenNoteDoesNotExist() {
        when(noteRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.findAll(99))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Note not found");
    }

    @Test
    void findAll_throwsNotFound_whenNoteIsDeleted() {
        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, true)));

        assertThatThrownBy(() -> commentService.findAll(10))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Note not found");
    }

    @Test
    void create_savesCommentAndReturnsResponse() {
        User user = newUser(1, false, false);
        Comment saved = newComment(100, 10, user, "投稿します");

        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, false)));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        CommentResponse result = commentService.create(10, new CommentCreateRequest("投稿します"), 1);

        assertThat(result.noteId()).isEqualTo(10);
        assertThat(result.content()).isEqualTo("投稿します");
    }

    @Test
    void create_throwsNotFound_whenNoteDoesNotExist() {
        when(noteRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(99, new CommentCreateRequest("投稿します"), 1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Note not found");
    }

    @Test
    void create_throwsNotFound_whenUserDoesNotExist() {
        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, false)));
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(10, new CommentCreateRequest("投稿します"), 99))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void delete_marksCommentAsDeleted_whenRequesterIsAuthor() {
        User author = newUser(1, false, false);
        Comment comment = newComment(100, 10, author, "削除対象");

        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, false)));
        when(userRepository.findById(1)).thenReturn(Optional.of(author));
        when(commentRepository.findByIdAndNote_Id(100, 10)).thenReturn(Optional.of(comment));

        commentService.delete(10, 100, 1);

        assertThat(comment.isDeleted()).isTrue();
        verify(commentRepository).save(comment);
    }

    @Test
    void delete_marksCommentAsDeleted_whenRequesterIsAdmin() {
        User author = newUser(1, false, false);
        User admin = newUser(2, false, true);
        Comment comment = newComment(100, 10, author, "削除対象");

        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, false)));
        when(userRepository.findById(2)).thenReturn(Optional.of(admin));
        when(commentRepository.findByIdAndNote_Id(100, 10)).thenReturn(Optional.of(comment));

        commentService.delete(10, 100, 2);

        assertThat(comment.isDeleted()).isTrue();
    }

    @Test
    void delete_throwsForbidden_whenRequesterIsNeitherAuthorNorAdmin() {
        User author = newUser(1, false, false);
        User otherUser = newUser(2, false, false);
        Comment comment = newComment(100, 10, author, "削除対象");

        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, false)));
        when(userRepository.findById(2)).thenReturn(Optional.of(otherUser));
        when(commentRepository.findByIdAndNote_Id(100, 10)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.delete(10, 100, 2))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only the author or an admin");
    }

    @Test
    void delete_throwsNotFound_whenCommentDoesNotExist() {
        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, false)));
        when(userRepository.findById(1)).thenReturn(Optional.of(newUser(1, false, false)));
        when(commentRepository.findByIdAndNote_Id(999, 10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.delete(10, 999, 1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Comment not found");
    }

    @Test
    void delete_throwsNotFound_whenCommentIsAlreadyDeleted() {
        User author = newUser(1, false, false);
        Comment comment = newComment(100, 10, author, "削除対象");
        comment.setDeleted(true);

        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, false)));
        when(userRepository.findById(1)).thenReturn(Optional.of(author));
        when(commentRepository.findByIdAndNote_Id(100, 10)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.delete(10, 100, 1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Comment not found");
    }
}
