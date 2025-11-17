package com.portfolio.spring_ecommerce.service_test;

import com.portfolio.spring_ecommerce.model.User;
import com.portfolio.spring_ecommerce.repository.UserRepository;
import com.portfolio.spring_ecommerce.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Test
    void testLoadUserByUsernameReturnsAuthorities() {
        // UserRepositoryのモックを作成
        UserRepository userRepository = mock(UserRepository.class);
        // UserServiceにモックを注入
        UserService userService = new UserService(userRepository);

        // テスト用ユーザーを作成し、ロールを設定
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRoles(Set.of("ROLE_USER"));

        // モックの振る舞いを定義：findByUsernameが呼ばれたらuserを返す
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // サービス経由でユーザー情報を取得
        UserDetails userDetails = userService.loadUserByUsername("testuser");
        // 権限リストに"ROLE_USER"が含まれていることを検証
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
}