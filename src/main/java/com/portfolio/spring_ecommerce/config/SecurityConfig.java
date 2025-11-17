package com.portfolio.spring_ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 各URLパターンごとにアクセス権限を設定
            .authorizeHttpRequests(auth -> auth
                // /test へのアクセスは全て許可
                .requestMatchers("/test").permitAll()
                // /admin/** へのアクセスはADMINロールが必要
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // /user/** へのアクセスはUSERロールが必要
                .requestMatchers("/user/**").hasRole("USER")
                // その他のリクエストは全て許可
                .anyRequest().permitAll()
            )
            // フォームログインを有効化
            .formLogin(form -> form.permitAll())
            // ログアウト機能を有効化
            .logout(logout -> logout.permitAll());
        // 設定をビルドして返却
        return http.build();
    }
}