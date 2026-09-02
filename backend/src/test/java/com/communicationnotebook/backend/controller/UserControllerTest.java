package com.communicationnotebook.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.communicationnotebook.backend.config.SecurityConfig;
import com.communicationnotebook.backend.dto.PasswordResetResponse;
import com.communicationnotebook.backend.dto.UserResponse;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private UserService userService;

    private UserPrincipal principal() {
        User user = new User();
        user.setId(1);
        user.setEmployeeId("E001");
        user.setName("テスト太郎");
        user.setPassword("hashed");
        user.setAdmin(false);
        user.setDeleted(false);
        return new UserPrincipal(user);
    }

    @Test
    void findAll_returnsUserList() {
        when(userService.findAll()).thenReturn(java.util.List.of(new UserResponse(1, "E001", "テスト太郎", false)));

        mockMvc.get().uri("/api/users")
                .with(user(principal()))
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].employeeId")
                .isEqualTo("E001");
    }

    @Test
    void findById_returnsUser() {
        when(userService.findById(1)).thenReturn(new UserResponse(1, "E001", "テスト太郎", false));

        mockMvc.get().uri("/api/users/{id}", 1)
                .with(user(principal()))
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.name")
                .isEqualTo("テスト太郎");
    }

    @Test
    void findById_returnsNotFound_whenUserDoesNotExist() {
        when(userService.findById(999)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.get().uri("/api/users/{id}", 999)
                .with(user(principal()))
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void findAll_returnsUnauthorized_whenNotAuthenticated() {
        mockMvc.get().uri("/api/users").assertThat().hasStatus(401);
    }

    @Test
    void update_returnsUpdatedUser_whenValid() {
        when(userService.updateName(
                        org.mockito.ArgumentMatchers.eq(1),
                        org.mockito.ArgumentMatchers.eq(2),
                        org.mockito.ArgumentMatchers.any()))
                .thenReturn(new UserResponse(2, "E002", "テスト新花子", false));

        mockMvc.put()
                .uri("/api/users/{id}", 2)
                .with(user(principal()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"テスト新花子\"}")
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.name")
                .isEqualTo("テスト新花子");
    }

    @Test
    void update_returnsForbidden_whenRequesterIsNotAdmin() {
        when(userService.updateName(
                        org.mockito.ArgumentMatchers.eq(1),
                        org.mockito.ArgumentMatchers.eq(2),
                        org.mockito.ArgumentMatchers.any()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.put()
                .uri("/api/users/{id}", 2)
                .with(user(principal()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"テスト新花子\"}")
                .assertThat()
                .hasStatus(403);
    }

    @Test
    void update_returnsBadRequest_whenNameIsBlank() {
        mockMvc.put()
                .uri("/api/users/{id}", 2)
                .with(user(principal()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}")
                .assertThat()
                .hasStatus(400);
    }

    @Test
    void delete_returnsNoContent_whenValid() {
        mockMvc.delete()
                .uri("/api/users/{id}", 2)
                .with(user(principal()))
                .assertThat()
                .hasStatus(204);
    }

    @Test
    void delete_returnsForbidden_whenRequesterIsNotAdmin() {
        org.mockito.Mockito.doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(userService)
                .delete(1, 2);

        mockMvc.delete()
                .uri("/api/users/{id}", 2)
                .with(user(principal()))
                .assertThat()
                .hasStatus(403);
    }

    @Test
    void resetPassword_returnsTemporaryPassword_whenValid() {
        when(userService.resetPassword(1, 2)).thenReturn(new PasswordResetResponse("テスト花子", "TempPass1234"));

        mockMvc.post()
                .uri("/api/users/{id}/password-reset", 2)
                .with(user(principal()))
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.temporaryPassword")
                .isEqualTo("TempPass1234");
    }

    @Test
    void resetPassword_returnsForbidden_whenRequesterIsNotAdmin() {
        when(userService.resetPassword(1, 2)).thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.post()
                .uri("/api/users/{id}/password-reset", 2)
                .with(user(principal()))
                .assertThat()
                .hasStatus(403);
    }

    @Test
    void resetPassword_returnsUnauthorized_whenNotAuthenticated() {
        mockMvc.post().uri("/api/users/{id}/password-reset", 2).assertThat().hasStatus(401);
    }
}
