package com.communicationnotebook.backend.security;

import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * SpringSecurityの認証処理において、ユーザーの詳細情報（UserDetails）を取得するサービスです。
 * SpringSecurityのUserDetailsServiceを実装しています。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ユーザー名（従業員ID）に紐づくユーザー情報を検索し、UserPrincipalを生成します。
     * 
     * @param employeeId 従業員ID
     * @return 認証ユーザー情報
     * @throws UsernameNotFoundException 該当する従業員IDのユーザーが存在しない場合
     */
    @Override
    public UserDetails loadUserByUsername(String employeeId) {
        User user = userRepository
                .findByEmployeeId(employeeId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + employeeId));
        return new UserPrincipal(user);
    }
}
