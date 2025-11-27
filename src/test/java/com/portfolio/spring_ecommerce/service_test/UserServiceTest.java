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

/**
 * UserServiceの単体テストクラス。
 * ユーザー認証・権限管理のロジックを検証する。
 */
class UserServiceTest {

    /**
     * ユーザーがROLE_USER権限を持つ場合のloadUserByUsernameメソッドのテスト。
     * 権限が正しく付与されることを検証する。
     */
    @Test
    void testLoadUserByUsernameReturnsAuthorities() {
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRoles(Set.of("ROLE_USER"));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("testuser");

        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    /**
     * ユーザーがROLE_ADMINとROLE_USER権限を持つ場合のloadUserByUsernameメソッドのテスト。
     * 両方の権限が正しく付与されることを検証する。
     */
    @Test
    void testLoadAdminUserHasAdminAuthority() {

        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("encoded_password");
        admin.setRoles(Set.of("ROLE_ADMIN", "ROLE_USER"));

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        UserDetails userDetails = userService.loadUserByUsername("admin");
        
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    /**
     * loadUserByUsernameメソッドがユーザー名とパスワードを正しく返すことを検証するテスト。
     */
    @Test
    void testLoadUserByUsernameReturnsCorrectUsernameAndPassword() {
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        User user = new User();
        user.setUsername("sampleuser");
        user.setPassword("samplepass");
        user.setRoles(Set.of("ROLE_USER"));

        when(userRepository.findByUsername("sampleuser")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("sampleuser");
        assertEquals("sampleuser", userDetails.getUsername());
        assertEquals("samplepass", userDetails.getPassword());
    }

    /**
     * loadUserByUsernameメソッドがユーザー未存在時に例外を投げることを検証するテスト。
     */
    @Test
    void testLoadUserByUsernameThrowsExceptionWhenUserNotFound() {
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        when(userRepository.findByUsername("notfound")).thenReturn(Optional.empty());

        UsernameNotFoundException thrown = assertThrows(
            UsernameNotFoundException.class,
            () -> userService.loadUserByUsername("notfound")
        );
        assertTrue(thrown.getMessage().contains("ユーザー名「notfound」のユーザーが見つかりません。"));
    }

}