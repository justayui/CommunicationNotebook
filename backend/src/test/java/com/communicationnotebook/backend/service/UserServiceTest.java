package com.communicationnotebook.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.communicationnotebook.backend.dto.PasswordChangeRequest;
import com.communicationnotebook.backend.dto.PasswordResetResponse;
import com.communicationnotebook.backend.dto.SignupRequest;
import com.communicationnotebook.backend.dto.UserResponse;
import com.communicationnotebook.backend.dto.UserUpdateRequest;
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

    @Test
    void changePassword_updatesHashedPassword_whenCurrentPasswordMatches() {
        User user = newUser(1, "E001", "テスト太郎", false);
        PasswordChangeRequest request = new PasswordChangeRequest("oldPassword", "newPassword");
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashed");

        userService.changePassword(1, request);

        assertThat(user.getPassword()).isEqualTo("newHashed");
    }

    @Test
    void changePassword_throwsUnauthorized_whenCurrentPasswordDoesNotMatch() {
        User user = newUser(1, "E001", "テスト太郎", false);
        PasswordChangeRequest request = new PasswordChangeRequest("wrongPassword", "newPassword");
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1, request))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void updateName_updatesName_whenRequesterIsAdmin() {
        User admin = newUser(1, "E001", "管理太郎", false);
        admin.setAdmin(true);
        User target = newUser(2, "E002", "テスト花子", false);
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(userRepository.findById(2)).thenReturn(Optional.of(target));
        when(userRepository.save(target)).thenReturn(target);

        UserResponse result = userService.updateName(1, 2, new UserUpdateRequest("テスト新花子"));

        assertThat(result.name()).isEqualTo("テスト新花子");
        assertThat(target.getName()).isEqualTo("テスト新花子");
    }

    @Test
    void updateName_throwsForbidden_whenRequesterIsNotAdmin() {
        User requester = newUser(1, "E001", "テスト太郎", false);
        User target = newUser(2, "E002", "テスト花子", false);
        when(userRepository.findById(1)).thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> userService.updateName(1, 2, new UserUpdateRequest("テスト新花子")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("管理者");
    }

    @Test
    void updateName_throwsNotFound_whenTargetUserDoesNotExist() {
        User admin = newUser(1, "E001", "管理太郎", false);
        admin.setAdmin(true);
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateName(1, 999, new UserUpdateRequest("テスト新花子")))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void delete_marksUserAsDeleted_whenRequesterIsAdmin() {
        User admin = newUser(1, "E001", "管理太郎", false);
        admin.setAdmin(true);
        User target = newUser(2, "E002", "テスト花子", false);
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(userRepository.findById(2)).thenReturn(Optional.of(target));

        userService.delete(1, 2);

        assertThat(target.isDeleted()).isTrue();
    }

    @Test
    void delete_throwsForbidden_whenRequesterIsNotAdmin() {
        User requester = newUser(1, "E001", "テスト太郎", false);
        when(userRepository.findById(1)).thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> userService.delete(1, 2))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("管理者");
    }

    @Test
    void delete_throwsNotFound_whenTargetUserDoesNotExist() {
        User admin = newUser(1, "E001", "管理太郎", false);
        admin.setAdmin(true);
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(1, 999))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void resetPassword_updatesHashedPasswordAndReturnsTemporaryPassword_whenRequesterIsAdmin() {
        User admin = newUser(1, "E001", "管理太郎", false);
        admin.setAdmin(true);
        User target = newUser(2, "E002", "テスト花子", false);
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(userRepository.findById(2)).thenReturn(Optional.of(target));
        when(passwordEncoder.encode(org.mockito.ArgumentMatchers.anyString())).thenReturn("newHashed");

        PasswordResetResponse result = userService.resetPassword(1, 2);

        assertThat(result.name()).isEqualTo("テスト花子");
        assertThat(result.temporaryPassword()).hasSize(12);
        assertThat(target.getPassword()).isEqualTo("newHashed");
    }

    @Test
    void resetPassword_throwsForbidden_whenRequesterIsNotAdmin() {
        User requester = newUser(1, "E001", "テスト太郎", false);
        when(userRepository.findById(1)).thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> userService.resetPassword(1, 2))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("管理者");
    }

    @Test
    void resetPassword_throwsNotFound_whenTargetUserDoesNotExist() {
        User admin = newUser(1, "E001", "管理太郎", false);
        admin.setAdmin(true);
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.resetPassword(1, 999))
                .isInstanceOf(ResponseStatusException.class);
    }
}
