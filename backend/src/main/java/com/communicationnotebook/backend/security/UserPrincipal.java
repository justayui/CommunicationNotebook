package com.communicationnotebook.backend.security;

import com.communicationnotebook.backend.entity.User;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * ログインユーザーの認証情報と権限を管理するクラスです。
 * Spring Securityの認証処理で利用するUserDetailsを実装しています。
 */
public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    /**
     * 認証対象のユーザー情報です。
     *
     * @return ユーザー情報
     */
    public User getUser() {
        return user;
    }

    /**
     * 主キーとなるIDを取得します。
     *
     * @return ユーザーID（主キー）
     */
    public Integer getId() {
        return user.getId();
    }

    /**
     * ユーザー名を取得します。
     * このアプリケーションでは職員IDをログインIDとして扱うため職員IDを返します。
     *
     * @return 職員ID
     */
    @Override
    public String getUsername() {
        return user.getEmployeeId();
    }

    /**
     * パスワードを取得します。
     *
     * @return ハッシュ化されたパスワード
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 権限を取得します。
     *
     * @return 管理者ならば管理者権限、管理者でなければ一般ユーザー権限
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.isAdmin()
                ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * ユーザーの有効性をチェックします。
     *
     * @return 有効な場合はtrue、無効な場合はfalse
     */
    @Override
    public boolean isEnabled() {
        return !user.isDeleted();
    }
}
