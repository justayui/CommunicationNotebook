package com.communicationnotebook.backend.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;

import com.communicationnotebook.backend.service.FavoriteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(FavoriteController.class)
class FavoriteControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private FavoriteService favoriteService;

    @Test
    void register_returnsCreated_whenSuccessful() {
        doNothing().when(favoriteService).register(eq(1), eq(1));

        mockMvc.post().uri("/api/notes/1/favorites?userId=1").assertThat().hasStatus(201);
    }

    @Test
    void register_returnsNotFound_whenServiceThrowsNotFound() {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found: 1"))
                .when(favoriteService)
                .register(eq(1), eq(1));

        mockMvc.post().uri("/api/notes/1/favorites?userId=1").assertThat().hasStatus(404);
    }

    @Test
    void register_returnsConflict_whenServiceThrowsConflict() {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Note is already favorited: 1"))
                .when(favoriteService)
                .register(eq(1), eq(1));

        mockMvc.post().uri("/api/notes/1/favorites?userId=1").assertThat().hasStatus(409);
    }

    @Test
    void register_returnsBadRequest_whenUserIdIsMissing() {
        mockMvc.post().uri("/api/notes/1/favorites").assertThat().hasStatus(400);
    }

    @Test
    void unregister_returnsNoContent_whenSuccessful() {
        doNothing().when(favoriteService).unregister(eq(1), eq(1));

        mockMvc.delete().uri("/api/notes/1/favorites?userId=1").assertThat().hasStatus(204);
    }

    @Test
    void unregister_returnsNotFound_whenServiceThrowsNotFound() {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite not found: note 1"))
                .when(favoriteService)
                .unregister(eq(1), eq(1));

        mockMvc.delete().uri("/api/notes/1/favorites?userId=1").assertThat().hasStatus(404);
    }

    @Test
    void unregister_returnsBadRequest_whenUserIdIsMissing() {
        mockMvc.delete().uri("/api/notes/1/favorites").assertThat().hasStatus(400);
    }
}
