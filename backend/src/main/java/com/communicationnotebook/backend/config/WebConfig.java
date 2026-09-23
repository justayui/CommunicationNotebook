package com.communicationnotebook.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORSの設定を行うクラスです。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * CORSの許可設定（対象パス・オリジン・メソッド等）を登録します。
     *
     * @param registry CORS設定を追加・管理するための登録情報
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .allowCredentials(true);
    }
}
