package com.communicationnotebook.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.communicationnotebook.backend.config.SecurityConfig;
import com.communicationnotebook.backend.dto.CommentCreateRequest;
import com.communicationnotebook.backend.dto.CommentResponse;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.CommentService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(CommentController.class)
@Import(SecurityConfig.class)
class CommentControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private CommentService commentService;

    private UserPrincipal principal(Integer id) {
        User user = new User();
        user.setId(id);
        user.setEmployeeId("E00" + id);
        user.setName("テスト太郎");
        user.setPassword("hashed");
        user.setAdmin(false);
        user.setDeleted(false);
        return new UserPrincipal(user);
    }

    @Test
    void findAll_returnsCommentList() {
        CommentResponse comment =
                new CommentResponse(100, 1, 1, "テスト太郎", "コメントです", LocalDateTime.of(2026, 8, 31, 10, 0));
        when(commentService.findAll(1)).thenReturn(List.of(comment));

        mockMvc.get().uri("/api/notes/1/comments")
                .with(user(principal(1)))
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].content")
                .isEqualTo("コメントです");
    }

    @Test
    void findAll_returnsNotFound_whenServiceThrowsNotFound() {
        when(commentService.findAll(99)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found: 99"));

        mockMvc.get().uri("/api/notes/99/comments").with(user(principal(1))).assertThat().hasStatus(404);
    }

    @Test
    void findAll_returnsUnauthorized_whenNotAuthenticated() {
        mockMvc.get().uri("/api/notes/1/comments").assertThat().hasStatus(401);
    }

    @Test
    void create_returnsCreatedComment() {
        CommentResponse comment =
                new CommentResponse(100, 1, 1, "テスト太郎", "投稿します", LocalDateTime.of(2026, 8, 31, 10, 0));
        when(commentService.create(eq(1), any(CommentCreateRequest.class), eq(1))).thenReturn(comment);

        mockMvc.post()
                .uri("/api/notes/1/comments")
                .with(user(principal(1)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"投稿します\"}")
                .assertThat()
                .hasStatus(201)
                .bodyJson()
                .extractingPath("$.author")
                .isEqualTo("テスト太郎");
    }

    @Test
    void create_returnsBadRequest_whenContentIsBlank() {
        mockMvc.post()
                .uri("/api/notes/1/comments")
                .with(user(principal(1)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"\"}")
                .assertThat()
                .hasStatus(400);
    }

    @Test
    void create_returnsNotFound_whenServiceThrowsNotFound() {
        when(commentService.create(eq(99), any(CommentCreateRequest.class), eq(1)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found: 99"));

        mockMvc.post()
                .uri("/api/notes/99/comments")
                .with(user(principal(1)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"投稿します\"}")
                .assertThat()
                .hasStatus(404);
    }

    @Test
    void create_returnsUnauthorized_whenNotAuthenticated() {
        mockMvc.post()
                .uri("/api/notes/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"投稿します\"}")
                .assertThat()
                .hasStatus(401);
    }

    @Test
    void delete_returnsNoContent_whenSuccessful() {
        doNothing().when(commentService).delete(eq(1), eq(100), eq(1));

        mockMvc.delete().uri("/api/notes/1/comments/100").with(user(principal(1))).assertThat().hasStatus(204);
    }

    @Test
    void delete_returnsForbidden_whenServiceThrowsForbidden() {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author or an admin can delete this comment"))
                .when(commentService)
                .delete(eq(1), eq(100), eq(2));

        mockMvc.delete().uri("/api/notes/1/comments/100").with(user(principal(2))).assertThat().hasStatus(403);
    }

    @Test
    void delete_returnsNotFound_whenServiceThrowsNotFound() {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found: 999"))
                .when(commentService)
                .delete(eq(1), eq(999), eq(1));

        mockMvc.delete().uri("/api/notes/1/comments/999").with(user(principal(1))).assertThat().hasStatus(404);
    }

    @Test
    void delete_returnsUnauthorized_whenNotAuthenticated() {
        mockMvc.delete().uri("/api/notes/1/comments/100").assertThat().hasStatus(401);
    }
}
