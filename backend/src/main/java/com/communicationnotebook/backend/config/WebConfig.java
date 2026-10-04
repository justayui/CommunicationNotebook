package com.communicationnotebook.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORSの設定を行うクラスです。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    /**
     * コンストラクタ
     * 
     * @param allowedOrigins CORSの許可オリジン
     */
    public WebConfig(@Value("${app.cors.allowed-origins}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * CORSの許可設定（対象パス・オリジン・メソッド等）を登録します。
     * 値が空の場合、CORSの許可を登録しません。
     * 未指定時はhttp://localhost:5173を許可し、コンテナ使用時は空を指定してCORSを無効にします。
     *
     * @param registry CORS設定を追加・管理するための登録情報
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (allowedOrigins.length == 0) {
            return;
        }
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .allowCredentials(true);
    }
}
