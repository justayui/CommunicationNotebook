package com.communicationnotebook.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.communicationnotebook.backend.dto.NoteCreateRequest;
import com.communicationnotebook.backend.dto.NoteResponse;
import com.communicationnotebook.backend.service.NoteService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(NoteController.class)
class NoteControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private NoteService noteService;

    @Test
    void findAll_returnsNoteList() {
        NoteResponse note = new NoteResponse(
                1, "雑談", "これはテスト投稿です。", "テスト太郎", LocalDateTime.of(2026, 8, 30, 10, 0));
        when(noteService.findAll()).thenReturn(List.of(note));

        mockMvc.get().uri("/api/notes")
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].author")
                .isEqualTo("テスト太郎");
    }

    @Test
    void create_returnsCreatedNote() {
        NoteResponse note = new NoteResponse(
                1, "雑談", "これはテスト投稿です。", "テスト太郎", LocalDateTime.of(2026, 8, 30, 10, 0));
        when(noteService.create(any(NoteCreateRequest.class))).thenReturn(note);

        mockMvc.post()
                .uri("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"category\":\"雑談\",\"content\":\"これはテスト投稿です。\"}")
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
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"category\":\"雑談\",\"content\":\"\"}")
                .assertThat()
                .hasStatus(400);
    }
}
