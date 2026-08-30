package com.communicationnotebook.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.communicationnotebook.backend.config.SecurityConfig;
import com.communicationnotebook.backend.dto.UserResponse;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
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
}
