package com.communicationnotebook.backend.security;

import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * リクエストごとに、ログイン中のユーザー情報をDBの最新の状態に更新するフィルターです。
 * セッションにはログイン時点のユーザー情報が保存されているため、名前の変更や削除を反映するために使用します。
 * ユーザーが削除済み・存在しない場合は、セッションを無効化して未認証として扱います。
 */
public class CurrentUserRefreshFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final SecurityContextRepository securityContextRepository;

    public CurrentUserRefreshFilter(UserRepository userRepository, SecurityContextRepository securityContextRepository) {
        this.userRepository = userRepository;
        this.securityContextRepository = securityContextRepository;
    }

    /**
     * ログイン中のユーザー情報をDBから取得し直し、認証情報を更新します。
     * 未ログインのリクエストは何もせず次の処理に渡します。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            Optional<User> latestUser = userRepository.findById(principal.getId()).filter(u -> !u.isDeleted());
            if (latestUser.isPresent()) {
                refreshAuthentication(latestUser.get(), request, response);
            } else {
                invalidateAuthentication(request);
            }
        }
        filterChain.doFilter(request, response);
    }

    //最新のユーザー情報で認証情報を作り直し、セッションにも保存
    private void refreshAuthentication(User user, HttpServletRequest request, HttpServletResponse response) {
        UserPrincipal refreshedPrincipal = new UserPrincipal(user);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                refreshedPrincipal, null, refreshedPrincipal.getAuthorities()));
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

    //認証情報を破棄し、セッションを無効化
    private void invalidateAuthentication(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
