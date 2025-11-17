package com.portfolio.spring_ecommerce.config;

import com.portfolio.spring_ecommerce.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // UserServiceとPasswordEncoderをDIで受け取る
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    // コンストラクタインジェクション
    public SecurityConfig(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Spring Securityのフィルタチェーンを定義
     * 各URLごとの認可設定、ログイン・ログアウトの有効化などを行う
     */
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
            // フォームログインを有効化（デフォルトのログインページを使用）
            .formLogin(form -> form.permitAll())
            // ログアウト機能を有効化（デフォルトのログアウトURLを使用）
            .logout(logout -> logout.permitAll());
        // 設定をビルドして返却
        return http.build();
    }

    /**
     * 認証マネージャのBean定義
     * UserServiceとPasswordEncoderを使って認証処理を構成
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder
            .userDetailsService(userService) // ユーザー情報の取得元を指定
            .passwordEncoder(passwordEncoder); // パスワードのハッシュ化方式を指定
        return authBuilder.build();
    }
}