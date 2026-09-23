package com.communicationnotebook.backend.config;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Security関連の設定クラスです。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * パスワードをハッシュ化するためのエンコーダーをBeanとして登録します。
     *
     * @return パスワードをハッシュ化するエンコーダー
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 認証用のコンポーネント（AuthenticationManager）をBeanとして登録します。
     *
     * @param config 認証に使用する設定情報
     * @return 認証を実行するコンポーネント（AuthenticationManager）
     * @throws Exception 処理に失敗した場合
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 認証情報をHttpSessionに保存・管理するためのリポジトリをBeanとして登録します。
     *
     * @return 認証情報を保存・管理するリポジトリ
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * Webセキュリティのアクセス制御ルールを装備したオブジェクトをBeanとして登録します。
     *
     * @param http HTTPセキュリティ設定用のオブジェクト
     * @param securityContextRepository 認証情報保存・管理用のリポジトリ
     * @return セキュリティ設定をすべて装備したオブジェクト
     * @throws Exception セキュリティ設定時にエラーが発生した場合
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        //１．基本的なセキュリティ機能の設定
        //CSRF対策の設定を無効化
        http.csrf(AbstractHttpConfigurer::disable)
                //CORSをデフォルト設定で有効化
                .cors(Customizer.withDefaults())

                //２．認証情報の保持・管理の設定
                //認証情報保存用のリポジトリをセット
                .securityContext(sc -> sc.securityContextRepository(securityContextRepository))
                //必要な場合のみセッションを作成する設定
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                //３．URL毎のアクセス権限の設定
                .authorizeHttpRequests(auth -> auth
                        //ログイン・サインアップAPIはログインなしで誰でもアクセス可
                        .requestMatchers("/api/auth/login", "/api/auth/signup")
                        .permitAll()
                        //ヘルスチェックは誰でもアクセス可
                        .requestMatchers("/actuator/health")
                        .permitAll()
                        //エラーページは誰でもアクセス可
                        .requestMatchers("/error")
                        .permitAll()
                        //Swagger(API仕様書)の確認画面は誰でもアクセス可
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .permitAll()
                        //上記以外の全てのリクエストはログイン認証が必要
                        .anyRequest()
                        .authenticated())
                //４．使用しないSpringSecurityの標準認証機能のオフ
                //画面付きの標準ログインフォームを無効化
                .formLogin(AbstractHttpConfigurer::disable)
                //ブラウザ標準のポップアップ認証の無効化
                .httpBasic(AbstractHttpConfigurer::disable)

                //５．ログアウト処理の設定
                .logout(logout -> logout
                        //ログアウトを実行するURLを指定
                        .logoutUrl("/api/auth/logout")
                        //ログアウト時にセッションを無効化
                        .invalidateHttpSession(true)
                        //ログアウト時にセッション用のクッキーを削除
                        .deleteCookies("JSESSIONID")
                        //ログアウト成功時は204 No Contentを返す（レスポンスボディ無し）
                        .logoutSuccessHandler(
                                (req, res, auth) -> res.setStatus(HttpServletResponse.SC_NO_CONTENT)))

                //６．未ログイン時のエラーハンドリング設定
                //未認証状態でアクセス拒否された場合、401 Unauthorizedを返す
                .exceptionHandling(exception -> exception.authenticationEntryPoint((req, res, ex) -> {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.setCharacterEncoding("UTF-8");
                    //エラーメッセージ内のパス（URL）に含まれる特殊文字をエスケープ処理
                    String path = req.getRequestURI().replace("\\", "\\\\").replace("\"", "\\\"");
                    //エラー用JSONメッセージを作成
                    String body =
                            """
                            {"timestamp":"%s","status":401,"error":"Unauthorized","message":"認証が必要です","path":"%s"}"""
                                    .formatted(Instant.now(), path);
                    //JSONを出力
                    res.getWriter().write(body);
                }));
        return http.build();
    }
}
