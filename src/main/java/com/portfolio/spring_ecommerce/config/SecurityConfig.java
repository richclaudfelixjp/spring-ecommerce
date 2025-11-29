package com.portfolio.spring_ecommerce.config;

import com.portfolio.spring_ecommerce.filter.JwtAuthenticationFilter;
import com.portfolio.spring_ecommerce.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(UserService userService, 
                         PasswordEncoder passwordEncoder,
                         JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Spring Securityのフィルタチェーンを定義
     * JWT認証を使用するため、セッションは使わない設定にする
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF保護を無効化（JWT使用時は不要）
            .csrf(csrf -> csrf.disable())
            // 各URLパターンごとにアクセス権限を設定
            .authorizeHttpRequests(auth -> auth
                // 認証不要なエンドポイント
                .requestMatchers("/test", "/auth/login", "/auth/register", "/auth/logout", "/products", "/products/**").permitAll()
                // 管理者エンドポイントはADMINロールが必要
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // ユーザーエンドポイントはUSERロールが必要
                .requestMatchers("/user/**").hasAnyRole("USER","ADMIN")
                // その他のリクエストは認証が必要
                .anyRequest().authenticated()
            )
            // セッションを使わない設定（JWTはステートレス）
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // JWTフィルターをUsernamePasswordAuthenticationFilterの前に追加
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 認証マネージャのBean定義
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder
            .userDetailsService(userService)
            .passwordEncoder(passwordEncoder);
        return authBuilder.build();
    }
}