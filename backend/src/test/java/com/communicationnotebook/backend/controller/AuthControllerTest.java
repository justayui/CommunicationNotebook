package com.communicationnotebook.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.communicationnotebook.backend.config.SecurityConfig;
import com.communicationnotebook.backend.dto.PasswordChangeRequest;
import com.communicationnotebook.backend.dto.SignupRequest;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserService userService;

    private User newUser(Integer id, String employeeId, String name, boolean admin) {
        User user = new User();
        user.setId(id);
        user.setEmployeeId(employeeId);
        user.setName(name);
        user.setPassword("hashed");
        user.setAdmin(admin);
        user.setDeleted(false);
        return user;
    }

    @Test
    void login_returnsUser_whenCredentialsAreValid() {
        User user = newUser(1, "E001", "テスト太郎", false);
        UserPrincipal principal = new UserPrincipal(user);
        when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        mockMvc.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"employeeId\":\"E001\",\"password\":\"password123\"}")
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.employeeId")
                .isEqualTo("E001");
    }

    @Test
    void login_returnsUnauthorized_whenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"employeeId\":\"E001\",\"password\":\"wrong\"}")
                .assertThat()
                .hasStatus(401);
    }

    @Test
    void login_returnsBadRequest_whenPasswordIsBlank() {
        mockMvc.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"employeeId\":\"E001\",\"password\":\"\"}")
                .assertThat()
                .hasStatus(400);
    }

    @Test
    void signup_returnsCreatedUser_whenValid() {
        User user = newUser(3, "E003", "テスト花子", false);
        when(userService.signup(org.mockito.ArgumentMatchers.any(SignupRequest.class))).thenReturn(user);

        mockMvc.post()
                .uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"employeeId\":\"E003\",\"name\":\"テスト花子\",\"password\":\"password123\"}")
                .assertThat()
                .hasStatus(201)
                .bodyJson()
                .extractingPath("$.employeeId")
                .isEqualTo("E003");
    }

    @Test
    void signup_returnsConflict_whenEmployeeIdDuplicate() {
        when(userService.signup(org.mockito.ArgumentMatchers.any(SignupRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "職員IDは既に使用されています"));

        mockMvc.post()
                .uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"employeeId\":\"E001\",\"name\":\"テスト太郎\",\"password\":\"password123\"}")
                .assertThat()
                .hasStatus(409);
    }

    @Test
    void signup_returnsBadRequest_whenRequiredFieldIsBlank() {
        mockMvc.post()
                .uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"employeeId\":\"E003\",\"name\":\"テスト花子\",\"password\":\"\"}")
                .assertThat()
                .hasStatus(400);
    }

    @Test
    void me_returnsUnauthorized_whenNotAuthenticated() {
        mockMvc.get().uri("/api/auth/me").assertThat().hasStatus(401);
    }

    @Test
    void me_returnsUser_whenAuthenticated() {
        User user = newUser(1, "E001", "テスト太郎", false);

        mockMvc.get()
                .uri("/api/auth/me")
                .with(user(new UserPrincipal(user)))
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.name")
                .isEqualTo("テスト太郎");
    }

    @Test
    void changePassword_returnsNoContent_whenValid() {
        User user = newUser(1, "E001", "テスト太郎", false);

        mockMvc.put()
                .uri("/api/auth/password")
                .with(user(new UserPrincipal(user)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"oldPassword\",\"newPassword\":\"newPassword\"}")
                .assertThat()
                .hasStatus(204);
    }

    @Test
    void changePassword_returnsUnauthorized_whenCurrentPasswordMismatch() {
        User user = newUser(1, "E001", "テスト太郎", false);
        org.mockito.Mockito.doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "現在のパスワードが正しくありません"))
                .when(userService)
                .changePassword(org.mockito.ArgumentMatchers.eq(1), org.mockito.ArgumentMatchers.any(PasswordChangeRequest.class));

        mockMvc.put()
                .uri("/api/auth/password")
                .with(user(new UserPrincipal(user)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"wrongPassword\",\"newPassword\":\"newPassword\"}")
                .assertThat()
                .hasStatus(401);
    }

    @Test
    void changePassword_returnsBadRequest_whenRequiredFieldIsBlank() {
        User user = newUser(1, "E001", "テスト太郎", false);

        mockMvc.put()
                .uri("/api/auth/password")
                .with(user(new UserPrincipal(user)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"oldPassword\",\"newPassword\":\"\"}")
                .assertThat()
                .hasStatus(400);
    }

    @Test
    void changePassword_returnsUnauthorized_whenNotAuthenticated() {
        mockMvc.put()
                .uri("/api/auth/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"oldPassword\",\"newPassword\":\"newPassword\"}")
                .assertThat()
                .hasStatus(401);
    }
}
