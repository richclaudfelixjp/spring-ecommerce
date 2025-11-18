package com.portfolio.spring_ecommerce.service_test;

import com.portfolio.spring_ecommerce.model.User;
import com.portfolio.spring_ecommerce.repository.UserRepository;
import com.portfolio.spring_ecommerce.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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

    @Test
    void testLoadAdminUserHasAdminAuthority() {
        // UserRepositoryのモックを作成
        UserRepository userRepository = mock(UserRepository.class);
        // UserServiceにモックを注入
        UserService userService = new UserService(userRepository);

        // 管理者ユーザーを作成し、複数ロールを設定
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("encoded_password");
        admin.setRoles(Set.of("ROLE_ADMIN", "ROLE_USER"));

        // モックの振る舞いを定義：findByUsernameが呼ばれたらadminを返す
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        // サービス経由でユーザー情報を取得
        UserDetails userDetails = userService.loadUserByUsername("admin");
        
        // 権限リストに"ROLE_ADMIN"と"ROLE_USER"が含まれていることを検証
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testLoadUserByUsernameReturnsCorrectUsernameAndPassword() {
        // UserRepositoryのモックを作成
        UserRepository userRepository = mock(UserRepository.class);
        // UserServiceにモックを注入
        UserService userService = new UserService(userRepository);

        // テスト用ユーザーを作成し、ユーザー名とパスワードを設定
        User user = new User();
        user.setUsername("sampleuser");
        user.setPassword("samplepass");
        user.setRoles(Set.of("ROLE_USER"));

        // モックの振る舞いを定義：findByUsernameが呼ばれたらuserを返す
        when(userRepository.findByUsername("sampleuser")).thenReturn(Optional.of(user));

        // サービス経由でユーザー情報を取得
        UserDetails userDetails = userService.loadUserByUsername("sampleuser");
        // ユーザー名とパスワードが正しいことを検証
        assertEquals("sampleuser", userDetails.getUsername());
        assertEquals("samplepass", userDetails.getPassword());
    }

    @Test
    void testLoadUserByUsernameThrowsExceptionWhenUserNotFound() {
        // UserRepositoryのモックを作成
        UserRepository userRepository = mock(UserRepository.class);
        // UserServiceにモックを注入
        UserService userService = new UserService(userRepository);

        // モックの振る舞いを定義：findByUsernameが呼ばれたら空を返す
        when(userRepository.findByUsername("notfound")).thenReturn(Optional.empty());

        // ユーザーが見つからない場合に例外がスローされることを検証
        UsernameNotFoundException thrown = assertThrows(
            UsernameNotFoundException.class,
            () -> userService.loadUserByUsername("notfound")
        );
        // 例外メッセージにユーザー名が含まれていることを検証
        assertTrue(thrown.getMessage().contains("ユーザー名「notfound」のユーザーが見つかりません。"));
    }

}