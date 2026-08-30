package com.communicationnotebook.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_returnsUserPrincipal_whenUserExists() {
        User user = new User();
        user.setId(1);
        user.setEmployeeId("E001");
        user.setName("テスト太郎");
        user.setPassword("hashed");
        user.setAdmin(true);
        user.setDeleted(false);

        when(userRepository.findByEmployeeId("E001")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("E001");

        assertThat(result).isInstanceOf(UserPrincipal.class);
        assertThat(result.getUsername()).isEqualTo("E001");
        assertThat(result.getPassword()).isEqualTo("hashed");
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFound_whenUserDoesNotExist() {
        when(userRepository.findByEmployeeId("E999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("E999"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
