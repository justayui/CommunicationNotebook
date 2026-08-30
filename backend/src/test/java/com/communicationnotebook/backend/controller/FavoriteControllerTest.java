package com.communicationnotebook.backend.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.communicationnotebook.backend.config.SecurityConfig;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.FavoriteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(FavoriteController.class)
@Import(SecurityConfig.class)
class FavoriteControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private FavoriteService favoriteService;

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
        doNothing().when(favoriteService).register(eq(1), eq(1));

        mockMvc.post().uri("/api/notes/1/favorites").with(user(principal(1))).assertThat().hasStatus(201);
    }

    @Test
    void register_returnsNotFound_whenServiceThrowsNotFound() {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found: 1"))
                .when(favoriteService)
                .register(eq(1), eq(1));

        mockMvc.post().uri("/api/notes/1/favorites").with(user(principal(1))).assertThat().hasStatus(404);
    }

    @Test
    void register_returnsConflict_whenServiceThrowsConflict() {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Note is already favorited: 1"))
                .when(favoriteService)
                .register(eq(1), eq(1));

        mockMvc.post().uri("/api/notes/1/favorites").with(user(principal(1))).assertThat().hasStatus(409);
    }

    @Test
    void register_returnsUnauthorized_whenNotAuthenticated() {
        mockMvc.post().uri("/api/notes/1/favorites").assertThat().hasStatus(401);
    }

    @Test
    void unregister_returnsNoContent_whenSuccessful() {
        doNothing().when(favoriteService).unregister(eq(1), eq(1));

        mockMvc.delete().uri("/api/notes/1/favorites").with(user(principal(1))).assertThat().hasStatus(204);
    }

    @Test
    void unregister_returnsNotFound_whenServiceThrowsNotFound() {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite not found: note 1"))
                .when(favoriteService)
                .unregister(eq(1), eq(1));

        mockMvc.delete().uri("/api/notes/1/favorites").with(user(principal(1))).assertThat().hasStatus(404);
    }

    @Test
    void unregister_returnsUnauthorized_whenNotAuthenticated() {
        mockMvc.delete().uri("/api/notes/1/favorites").assertThat().hasStatus(401);
    }
}
