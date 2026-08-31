package com.communicationnotebook.backend.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.communicationnotebook.backend.config.SecurityConfig;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.NoteReadService;
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
