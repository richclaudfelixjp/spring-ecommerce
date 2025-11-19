package com.portfolio.spring_ecommerce.config;

import com.portfolio.spring_ecommerce.model.User;
import com.portfolio.spring_ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Value("${ADMIN_PASSWORD:defaultAdminPass}")
    private String adminPassword;

    @Value("${USER_PASSWORD:defaultUserPass}")
    private String userPassword;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initializeAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 管理者ユーザーが存在しない場合のみ作成
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode(adminPassword)); // 本番環境では強力なパスワードを使用
                admin.setRoles(Set.of("ROLE_ADMIN", "ROLE_USER"));
                userRepository.save(admin);
            }

            // 通常ユーザーの例
            if (userRepository.findByUsername("user").isEmpty()) {
                User user = new User();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode(userPassword));
                user.setRoles(Set.of("ROLE_USER"));
                userRepository.save(user);
            }
        };
    }
}