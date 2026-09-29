package com.communicationnotebook.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@ExtendWith(MockitoExtension.class)
class CurrentUserRefreshFilterTest {

    @Mock
    private UserRepository userRepository;

    private CurrentUserRefreshFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockHttpSession session;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new CurrentUserRefreshFilter(userRepository, new HttpSessionSecurityContextRepository());
        session = new MockHttpSession();
        request = new MockHttpServletRequest();
        request.setSession(session);
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User newUser(Integer id, String name, boolean deleted) {
        User user = new User();
        user.setId(id);
        user.setEmployeeId("E00" + id);
        user.setName(name);
        user.setPassword("hashed");
        user.setAdmin(false);
        user.setDeleted(deleted);
        return user;
    }

    //セッションから復元したログイン状態をセット
    private void authenticateAs(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    //ログイン中ユーザー情報の最新化に関するテスト
    @Test
    void doFilter_refreshesUser_whenUserWasUpdated() throws Exception {
        authenticateAs(newUser(1, "変更前の名前", false));
        when(userRepository.findById(1)).thenReturn(Optional.of(newUser(1, "変更後の名前", false)));

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        assertThat(principal.getUser().getName()).isEqualTo("変更後の名前");

        //セッションに保存された認証情報も最新化されていること
        SecurityContext savedContext = (SecurityContext)
                session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        UserPrincipal savedPrincipal = (UserPrincipal) savedContext.getAuthentication().getPrincipal();
        assertThat(savedPrincipal.getUser().getName()).isEqualTo("変更後の名前");
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    void doFilter_clearsAuthenticationAndInvalidatesSession_whenUserWasDeleted() throws Exception {
        authenticateAs(newUser(1, "テスト太郎", false));
        when(userRepository.findById(1)).thenReturn(Optional.of(newUser(1, "テスト太郎", true)));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(session.isInvalid()).isTrue();
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    void doFilter_clearsAuthenticationAndInvalidatesSession_whenUserDoesNotExist() throws Exception {
        authenticateAs(newUser(1, "テスト太郎", false));
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(session.isInvalid()).isTrue();
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    void doFilter_doesNothing_whenNotAuthenticated() throws Exception {
        filter.doFilter(request, response, filterChain);

        verifyNoInteractions(userRepository);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(session.isInvalid()).isFalse();
        assertThat(filterChain.getRequest()).isNotNull();
    }
}
