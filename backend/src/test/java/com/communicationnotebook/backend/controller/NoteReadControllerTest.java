package com.communicationnotebook.backend.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.communicationnotebook.backend.config.SecurityConfig;
import com.communicationnotebook.backend.dto.NoteReaderResponse;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.NoteReadService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(NoteReadController.class)
@Import(SecurityConfig.class)
class NoteReadControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private NoteReadService noteReadService;

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
    void findReaders_returnsReaderList() {
        when(noteReadService.findReaders(1)).thenReturn(List.of(new NoteReaderResponse(1, "テスト太郎")));

        mockMvc.get().uri("/api/notes/1/reads")
                .with(user(principal(1)))
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].name")
                .isEqualTo("テスト太郎");
    }

    @Test
    void findReaders_returnsNotFound_whenServiceThrowsNotFound() {
        when(noteReadService.findReaders(99))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found: 99"));

        mockMvc.get().uri("/api/notes/99/reads").with(user(principal(1))).assertThat().hasStatus(404);
    }

    @Test
    void findReaders_returnsUnauthorized_whenNotAuthenticated() {
        mockMvc.get().uri("/api/notes/1/reads").assertThat().hasStatus(401);
    }

    @Test
    void register_returnsCreated_whenSuccessful() {
        doNothing().when(noteReadService).register(eq(1), eq(1));

        mockMvc.post().uri("/api/notes/1/reads").with(user(principal(1))).assertThat().hasStatus(201);
    }

    @Test
    void register_returnsNotFound_whenServiceThrowsNotFound() {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found: 1"))
                .when(noteReadService)
                .register(eq(1), eq(1));

        mockMvc.post().uri("/api/notes/1/reads").with(user(principal(1))).assertThat().hasStatus(404);
    }

    @Test
    void register_returnsUnauthorized_whenNotAuthenticated() {
        mockMvc.post().uri("/api/notes/1/reads").assertThat().hasStatus(401);
    }
}
