package com.communicationnotebook.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.communicationnotebook.backend.config.SecurityConfig;
import com.communicationnotebook.backend.dto.NoteCreateRequest;
import com.communicationnotebook.backend.dto.NoteResponse;
import com.communicationnotebook.backend.dto.NoteUpdateRequest;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.NoteService;
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

@WebMvcTest(NoteController.class)
@Import(SecurityConfig.class)
class NoteControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private NoteService noteService;

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
    void findAll_returnsNoteListWithFavoritedField() {
        NoteResponse note = new NoteResponse(
                1, "雑談", "これはテスト投稿です。", "テスト太郎", LocalDateTime.of(2026, 8, 30, 10, 0), true);
        when(noteService.findAll(null, null, false, 1)).thenReturn(List.of(note));

        mockMvc.get().uri("/api/notes")
                .with(user(principal(1)))
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].favorited")
                .isEqualTo(true);
    }

    @Test
    void findAll_passesQueryParamsToService() {
        when(noteService.findAll("懇親会", "業務連絡", true, 1)).thenReturn(List.of());

        mockMvc.get()
                .uri("/api/notes?keyword=懇親会&category=業務連絡&favoriteOnly=true")
                .with(user(principal(1)))
                .assertThat()
                .hasStatusOk();
    }

    @Test
    void findAll_returnsUnauthorized_whenNotAuthenticated() {
        mockMvc.get().uri("/api/notes").assertThat().hasStatus(401);
    }

    @Test
    void create_returnsCreatedNote() {
        NoteResponse note = new NoteResponse(
                1, "雑談", "これはテスト投稿です。", "テスト太郎", LocalDateTime.of(2026, 8, 30, 10, 0), false);
        when(noteService.create(any(NoteCreateRequest.class), eq(1))).thenReturn(note);

        mockMvc.post()
                .uri("/api/notes")
                .with(user(principal(1)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"雑談\",\"content\":\"これはテスト投稿です。\"}")
                .assertThat()
                .hasStatus(201)
                .bodyJson()
                .extractingPath("$.author")
                .isEqualTo("テスト太郎");
    }

    @Test
    void create_returnsBadRequest_whenContentIsBlank() {
        mockMvc.post()
                .uri("/api/notes")
                .with(user(principal(1)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"雑談\",\"content\":\"\"}")
                .assertThat()
                .hasStatus(400);
    }

    @Test
    void update_returnsUpdatedNote() {
        NoteResponse note = new NoteResponse(
                1, "業務連絡", "更新後の内容", "テスト太郎", LocalDateTime.of(2026, 8, 30, 10, 0), false);
        when(noteService.update(eq(1), any(NoteUpdateRequest.class), eq(1))).thenReturn(note);

        mockMvc.put()
                .uri("/api/notes/1")
                .with(user(principal(1)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"業務連絡\",\"content\":\"更新後の内容\"}")
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.content")
                .isEqualTo("更新後の内容");
    }

    @Test
    void update_returnsForbidden_whenServiceThrowsForbidden() {
        when(noteService.update(eq(1), any(NoteUpdateRequest.class), eq(2)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can update this note"));

        mockMvc.put()
                .uri("/api/notes/1")
                .with(user(principal(2)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"雑談\",\"content\":\"更新後の内容\"}")
                .assertThat()
                .hasStatus(403);
    }

    @Test
    void update_returnsBadRequest_whenContentIsBlank() {
        mockMvc.put()
                .uri("/api/notes/1")
                .with(user(principal(1)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"雑談\",\"content\":\"\"}")
                .assertThat()
                .hasStatus(400);
    }

    @Test
    void delete_returnsNoContent_whenSuccessful() {
        mockMvc.delete().uri("/api/notes/1").with(user(principal(1))).assertThat().hasStatus(204);
    }

    @Test
    void delete_returnsForbidden_whenServiceThrowsForbidden() {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author or an admin can delete this note"))
                .when(noteService)
                .delete(eq(1), eq(2));

        mockMvc.delete().uri("/api/notes/1").with(user(principal(2))).assertThat().hasStatus(403);
    }

    @Test
    void delete_returnsUnauthorized_whenNotAuthenticated() {
        mockMvc.delete().uri("/api/notes/1").assertThat().hasStatus(401);
    }
}
