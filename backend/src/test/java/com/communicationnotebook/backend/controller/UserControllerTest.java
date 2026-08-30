package com.communicationnotebook.backend.controller;

import static org.mockito.Mockito.when;

import com.communicationnotebook.backend.dto.UserResponse;
import com.communicationnotebook.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void findAll_returnsUserList() {
        when(userService.findAll()).thenReturn(java.util.List.of(new UserResponse(1, "E001", "テスト太郎", false)));

        mockMvc.get().uri("/api/users")
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
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND);
    }
}
