package com.portfolio.spring_ecommerce.service_test;

import com.portfolio.spring_ecommerce.model.User;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void testRolesAssignment() {
        // Userインスタンスを生成し、ロールをセット
        User user = new User();
        user.setRoles(Set.of("ROLE_USER", "ROLE_ADMIN"));
        // "ROLE_USER"が含まれていることを検証
        assertTrue(user.getRoles().contains("ROLE_USER"));
        // "ROLE_ADMIN"が含まれていることを検証
        assertTrue(user.getRoles().contains("ROLE_ADMIN"));
    }
}