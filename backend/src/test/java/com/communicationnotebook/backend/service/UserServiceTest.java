package com.communicationnotebook.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.communicationnotebook.backend.dto.SignupRequest;
import com.communicationnotebook.backend.dto.UserResponse;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User newUser(Integer id, String employeeId, String name, boolean deleted) {
        User user = new User();
        user.setId(id);
        user.setEmployeeId(employeeId);
        user.setName(name);
        user.setPassword("hashed");
        user.setAdmin(false);
        user.setDeleted(deleted);
        return user;
    }

    @Test
    void findAll_returnsOnlyNonDeletedUsers() {
        User user = newUser(1, "E001", "テスト太郎", false);
        when(userRepository.findByDeletedFalse()).thenReturn(List.of(user));

        List<UserResponse> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).employeeId()).isEqualTo("E001");
    }

    @Test
    void findById_returnsUser_whenExistsAndNotDeleted() {
        User user = newUser(1, "E001", "テスト太郎", false);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        UserResponse result = userService.findById(1);

        assertThat(result.id()).isEqualTo(1);
        assertThat(result.name()).isEqualTo("テスト太郎");
    }

    @Test
    void findById_throwsNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void findById_throwsNotFound_whenUserIsDeleted() {
        User user = newUser(1, "E001", "テスト太郎", true);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.findById(1))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void signup_savesHashedPasswordAndReturnsUser_whenEmployeeIdIsNew() {
        SignupRequest request = new SignupRequest("E003", "テスト花子", "password123");
        when(userRepository.existsByEmployeeId("E003")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.signup(request);

        assertThat(result.getEmployeeId()).isEqualTo("E003");
        assertThat(result.getName()).isEqualTo("テスト花子");
        assertThat(result.getPassword()).isEqualTo("hashed");
        assertThat(result.isAdmin()).isFalse();
        assertThat(result.isDeleted()).isFalse();
    }

    @Test
    void signup_throwsConflict_whenEmployeeIdAlreadyExists() {
        SignupRequest request = new SignupRequest("E001", "テスト太郎", "password123");
        when(userRepository.existsByEmployeeId("E001")).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(ResponseStatusException.class);
    }
}
