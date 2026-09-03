package com.communicationnotebook.backend.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminUserInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminUserInitializer initializer() {
        return new AdminUserInitializer(userRepository, passwordEncoder, "E001", "管理者", "password123");
    }

    private User newUser(String employeeId, boolean admin, boolean deleted) {
        User user = new User();
        user.setEmployeeId(employeeId);
        user.setAdmin(admin);
        user.setDeleted(deleted);
        return user;
    }

    @Test
    void run_createsAdmin_whenNoAdminExistsAndEmployeeIdIsFree() {
        when(userRepository.existsByAdminTrueAndDeletedFalse()).thenReturn(false);
        when(userRepository.findByEmployeeId("E001")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed");

        initializer().run(new DefaultApplicationArguments());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmployeeId()).isEqualTo("E001");
        assertThat(saved.getName()).isEqualTo("管理者");
        assertThat(saved.getPassword()).isEqualTo("hashed");
        assertThat(saved.isAdmin()).isTrue();
        assertThat(saved.isDeleted()).isFalse();
    }

    @Test
    void run_doesNothing_whenAdminAlreadyExists() {
        when(userRepository.existsByAdminTrueAndDeletedFalse()).thenReturn(true);

        initializer().run(new DefaultApplicationArguments());

        verify(userRepository, never()).save(any());
    }

    @Test
    void run_doesNotPromote_whenEmployeeIdExistsButNotAdmin() {
        when(userRepository.existsByAdminTrueAndDeletedFalse()).thenReturn(false);
        when(userRepository.findByEmployeeId("E001")).thenReturn(Optional.of(newUser("E001", false, false)));

        initializer().run(new DefaultApplicationArguments());

        verify(userRepository, never()).save(any());
    }

    @Test
    void run_doesNotThrow_whenRepositoryFails() {
        when(userRepository.existsByAdminTrueAndDeletedFalse()).thenThrow(new RuntimeException("DB down"));

        assertThatCode(() -> initializer().run(new DefaultApplicationArguments()))
                .doesNotThrowAnyException();
        verify(userRepository, times(1)).existsByAdminTrueAndDeletedFalse();
    }
}
